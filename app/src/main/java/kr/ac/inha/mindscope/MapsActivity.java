package kr.ac.inha.mindscope;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import inha.nsl.easytrack.ETServiceGrpc;
import inha.nsl.easytrack.EtService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import kr.ac.inha.mindscope.dialog.FirstMapStartDialog;

import static kr.ac.inha.mindscope.LocationAdapter.EDIT_CODE;
import static kr.ac.inha.mindscope.fragment.StressReportFragment2.setListViewHeightBasedOnChildren;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, OnItemClick, GoogleMap.OnMapClickListener {

    public static final String TAG = "MapsActivity";
    //region variable
    public static final String ID_HOME = "HOME";
    public static final String ID_DORM = "DORM";
    public static final String ID_UNIV = "UNIV";
    public static final String ID_LIBRARY = "LIBRARY";
    public static final String ID_ADDITIONAL = "ADDITIONAL";
    public static final int GEOFENCE_RADIUS_DEFAULT = 100;
    private static final String ACTION_SELECT_PLACE = "SELECT_PLACE";
    private static final String ACTION_CLICK_SAVE_SELECT_PLACE = "CLICK_SAVE_SELECT_PLACE";
    private static final Float ZOOM_LEVLE = 16f;
    private static int AUTOCOMPLETE_REQUEST_CODE = 1;
    public boolean checkForFirstStartStep1;
    LocationManager locationManager;
    Toolbar toolbar;
    ListView listView;
    ScrollView scrollView;
    ImageButton currentLocationBtn;
    Button mapSaveBtn;
    LatLng selectedLatLng;
    String selectedName;
    String selectedAddress;
    ArrayList<StoreLocation> locationArrayList;
    FirstMapStartDialog firstMapStartDialog;
    LinearLayout loadingLayout;
    private GoogleMap mMap;
    private Marker currentGeofenceMarker;
    private Circle geoLimits;
    private boolean checkForFirstHomeSetting = false;
    private String TITLE_HOME;
    private String TITLE_DORM;
    private String TITLE_UNIV;
    private String TITLE_LIBRARY;
    private String TITLE_ADDITIONAL;
    private FusedLocationProviderClient fusedLocationProviderClient;
    //endregion
    private View.OnClickListener dialogClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            firstMapStartDialog.dismiss();
            checkForFirstHomeSetting = true;
            checkForFirstStartStep1 = true;
        }
    };
    private View.OnClickListener mapSaveClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (selectedLatLng != null) {
                Intent intent = new Intent(MapsActivity.this, SelectedPlaceSaveActivity.class);
                if (checkForFirstHomeSetting) {
                    intent.putExtra("name", selectedName);
                    intent.putExtra("address", selectedAddress);
                    intent.putExtra("lat", selectedLatLng.latitude);
                    intent.putExtra("lng", selectedLatLng.longitude);
                    intent.putExtra("home_setting", true);
                    checkForFirstHomeSetting = false;
                } else {
                    intent.putExtra("name", selectedName);
                    intent.putExtra("address", selectedAddress);
                    intent.putExtra("lat", selectedLatLng.latitude);
                    intent.putExtra("lng", selectedLatLng.longitude);
                    intent.putExtra("home_setting", false);
                }
                startActivity(intent);
                Tools.saveApplicationLog(getApplicationContext(), TAG, ACTION_CLICK_SAVE_SELECT_PLACE);
            } else {
                Toast.makeText(getApplicationContext(), "장소 검색 후 사용해주세요!", Toast.LENGTH_SHORT).show();
            }
        }
    };

    static StoreLocation getLocationData(Context con, String locationId) {
        SharedPreferences locationPrefs = con.getSharedPreferences("UserLocations", MODE_PRIVATE);
        float lat = locationPrefs.getFloat(locationId + "_LAT", 0);
        float lng = locationPrefs.getFloat(locationId + "_LNG", 0);
        String name = locationPrefs.getString(locationId + "_NAME", "");
        String address = locationPrefs.getString(locationId + "_ADDRESS", "");
        boolean isDeleted = locationPrefs.getBoolean(locationId + "_isDeleted", false);
        if (lat != 0 && lng != 0) {
            return new StoreLocation(locationId, new LatLng(lat, lng), name, address, isDeleted);
        }
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if (DbMgr.getDB() == null)
            DbMgr.init(getApplicationContext());


        checkForFirstStartStep1 = false;
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        //첫번째 실행의 경우, INTRO 및 장소설정
        SharedPreferences firstStartMap = getSharedPreferences("firstStartMap", MODE_PRIVATE);
        int firstMap = firstStartMap.getInt("FirstMap", 0);

        if (firstMap != 1) {

            firstMapStartDialog = new FirstMapStartDialog(this, dialogClickListener);
            firstMapStartDialog.setCancelable(false);
            firstMapStartDialog.show();

            int infoFirst = 1;
            SharedPreferences a = getSharedPreferences("firstStartMap", MODE_PRIVATE);
            SharedPreferences.Editor editor = a.edit();
            editor.putInt("FirstMap", infoFirst);
            editor.apply();
        }

        TITLE_HOME = getString(R.string.set_home_location);


        updateListView();

        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            StoreLocation storeLocation = (StoreLocation) listView.getItemAtPosition(i);
            changeMarkerPlaceInfo(storeLocation);
        });
        scrollView = findViewById(R.id.map_scrollview);
        currentLocationBtn = findViewById(R.id.current_location_btn);
        loadingLayout = findViewById(R.id.loading_frame);
        loadingLayout.setVisibility(View.VISIBLE);
        loadingLayout.bringToFront();
        Tools.disable_touch(this);
        mapSaveBtn = findViewById(R.id.map_save_btn);
        mapSaveBtn.setOnClickListener(mapSaveClickListener);

        currentLocationBtn.setOnClickListener(view -> getCurrentLocation());

        toolbar = (Toolbar) findViewById(R.id.toolbar_map);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
        }


        ScrollableMapFragment mapFragment = (ScrollableMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
            mapFragment.setListener(new ScrollableMapFragment.OnTouchListener() {
                @Override
                public void onActionDown() {
                    scrollView.requestDisallowInterceptTouchEvent(true);
                }

                @Override
                public void onActionUp() {
                    scrollView.requestDisallowInterceptTouchEvent(false);
                }
            });
        }


        String apiKey = getResources().getString(R.string.google_maps_key);
        // Place API
        Places.initialize(getApplicationContext(), apiKey);
        PlacesClient placesclient = Places.createClient(this);

        AutocompleteSupportFragment autocompleteSupportFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.frg_places_autocomplete);

        assert autocompleteSupportFragment != null;
        autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG));

        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {

                Log.d(TAG, "Place: " + place.getName() + ", " + place.getAddress() + ", " + Objects.requireNonNull(place.getLatLng()).latitude + ", " + place.getLatLng().longitude);
                selectedLatLng = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
                selectedAddress = place.getAddress();
                selectedName = place.getName();
                mMap.animateCamera(CameraUpdateFactory.newLatLng(selectedLatLng));
                Toast.makeText(getApplicationContext(), String.format(Locale.KOREA, "Lat, Lng: %.2f, %.2f", selectedLatLng.latitude, selectedLatLng.longitude), Toast.LENGTH_LONG).show();
                Tools.saveApplicationLog(getApplicationContext(), TAG, ACTION_SELECT_PLACE);
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.d(TAG, "An error occurred: " + status);
            }
        });

        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);

//        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
//                .build(this);
//        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);


    }

    public void updateListView() {
        locationArrayList = new ArrayList<>();
        SharedPreferences locationPrefs = getSharedPreferences("UserLocations", MODE_PRIVATE);
        String locationList = locationPrefs.getString("locationList", "");
        if (locationList.equals("")) {
            locationList = getLocationListFromServer();
        }
        String[] locations = locationList.split(" ");
        Log.d(TAG, "locationList " + Arrays.toString(locations));
        for (String location : locations) {
            if (getLocationData(getApplicationContext(), location) != null) {
                locationArrayList.add(getLocationData(getApplicationContext(), location));
            }
        }
        LocationAdapter adapter = new LocationAdapter(this, R.layout.item_place_list, locationArrayList, this);
        listView = findViewById(R.id.place_list);
        listView.setEmptyView(findViewById(android.R.id.empty));
        listView.setAdapter(adapter);
        setListViewHeightBasedOnChildren(listView);
    }

    @Override
    protected void onResume() {
        updateListView();
        Tools.saveApplicationLog(getApplicationContext(), TAG, Tools.ACTION_OPEN_PAGE);
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (requestCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
            } else if (resultCode == RESULT_CANCELED) {
                Status status = Autocomplete.getStatusFromIntent(data);
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.clear();
        mMap.setOnMapClickListener(this);

        locationArrayList = new ArrayList<>();

        SharedPreferences locationPrefs = getSharedPreferences("UserLocations", MODE_PRIVATE);
        String[] locations = locationPrefs.getString("locationList", "").split(" ");
        for (String location : locations) {
            if (getLocationData(getApplicationContext(), location) != null) {
                addLocationMarker(Objects.requireNonNull(getLocationData(getApplicationContext(), location)));
                locationArrayList.add(getLocationData(getApplicationContext(), location));
            }
        }

        try {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (locationManager != null) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 20, this);
            }
        } catch (SecurityException e) {
            e.printStackTrace();

//            prfs.get("locationList").split(" ");
//            []
        }

    }

    @Override
    public void onMapClick(LatLng latLng) {
        selectedLatLng = latLng;
        Context context = this;

        new Thread(new Runnable() {
            @Override
            public void run() {
                // reverse geocoding
                final Geocoder geocoder = new Geocoder(context);
                List<Address> addressesList = null;
                try {
                    addressesList = geocoder.getFromLocation(selectedLatLng.latitude, selectedLatLng.longitude, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (addressesList != null) {
                    if (addressesList.size() == 0) {
                        Toast.makeText(getApplicationContext(), "현재 장소와 매치되는 주소를 찾지 못했습니다.", Toast.LENGTH_LONG).show();
                    } else {
                        selectedAddress = addressesList.get(0).getAddressLine(0);
                        selectedName = addressesList.get(0).getFeatureName();
                    }
                }

                MarkerOptions optionsMarker = new MarkerOptions()
                        .position(latLng);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mMap != null) {
                            if (currentGeofenceMarker != null) {
                                currentGeofenceMarker.remove();
                            }
                            currentGeofenceMarker = mMap.addMarker(optionsMarker);
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_LEVLE));
                        }
                    }
                });
            }
        }).start();

        Log.d(TAG, "onMapClick" + selectedLatLng.toString());

    }

    @Override
    public void onLocationChanged(Location location) {
        loadingLayout.setVisibility(View.GONE);
        Tools.enable_touch(this);

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_LEVLE));

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

//    private void markerForGeofence(LatLng latLng) {
//        MarkerOptions optionsMarker = new MarkerOptions()
//                .snippet(String.valueOf(GEOFENCE_RADIUS_DEFAULT))
//                .position(latLng)
//                .title("Current Location test");
//        if (mMap != null) {
//            if (currentGeofenceMarker != null) {
//                currentGeofenceMarker.remove();
//            }
//            currentGeofenceMarker = mMap.addMarker(optionsMarker);
//            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
//            drawGeofence(currentGeofenceMarker, GEOFENCE_RADIUS_DEFAULT);
//        }
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void drawGeofence(Marker marker, int radius) {
        if (geoLimits != null) {
            geoLimits.remove();
        }

        CircleOptions circleOptions = new CircleOptions()
                .center(marker.getPosition())
                .strokeColor(Color.argb(50, 70, 70, 70))
                .strokeWidth(1)
                .fillColor(Color.argb(100, 150, 150, 150))
                .radius(radius);

        geoLimits = mMap.addCircle(circleOptions);
    }

    private void addLocationMarker(StoreLocation location) {
        Drawable iconDrawable;
        String location_title;
        iconDrawable = ContextCompat.getDrawable(this, R.drawable.additional);
        location_title = location.getmId();

        assert iconDrawable != null;
        Bitmap iconBmp = ((BitmapDrawable) iconDrawable).getBitmap();
//        mMap.addMarker(new MarkerOptions()
//                .title(location_title)
//                .snippet(String.valueOf(GEOFENCE_RADIUS_DEFAULT))
//                .position(location.getmLatLng())
//                .icon(BitmapDescriptorFactory.fromBitmap(iconBmp)));
        mMap.addMarker(new MarkerOptions()
                .title(location_title)
                .position(location.getmLatLng()));
    }

    // place info marker
    private void changeMarkerPlaceInfo(StoreLocation storeLocation) {
        // 누른 listview 에 따라서 마커 그리기
        LatLng latLng = new LatLng(storeLocation.getmLatLng().latitude, storeLocation.getmLatLng().longitude);
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(storeLocation.getmId()));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_LEVLE));
    }

    @Override
    public void onClick(MapsActivity.StoreLocation storeLocation, int code) {
        if (storeLocation != null && code == EDIT_CODE) {
            Intent intent = new Intent(MapsActivity.this, SelectedPlaceSaveActivity.class);
            intent.putExtra("name", storeLocation.getmName());
            intent.putExtra("address", storeLocation.getmAddress());
            intent.putExtra("lat", storeLocation.getmLatLng().latitude);
            intent.putExtra("lng", storeLocation.getmLatLng().longitude);
            intent.putExtra("placeusername", storeLocation.getmId());
            intent.putExtra("editcode", EDIT_CODE);
            startActivity(intent);
        } else {
            Log.d(TAG, String.format("code %d", code));
        }
        updateListView();
    }

    @Override
    public void onBackPressed() {
        //
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.toolbar_map_current_save_btn) {
            // Call 장소 저장 액티비티
            Intent intent = new Intent(MapsActivity.this, MainActivity.class);
            intent.putExtra("first_start_step1", checkForFirstStartStep1);
            Log.d(TAG, "first start stpe1 : " + checkForFirstStartStep1);
            finish();
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.toolbar_map_current_save_btn, menu);
        return true;
    }

    private String getLocationListFromServer() {
        String locationList = "";
        if (Tools.isNetworkAvailable()) {

            SharedPreferences loginPrefs = getSharedPreferences("UserLogin", Context.MODE_PRIVATE);
            SharedPreferences configPrefs = getSharedPreferences("Configurations", Context.MODE_PRIVATE);

            ManagedChannel channel = ManagedChannelBuilder.forAddress(getString(R.string.grpc_host), Integer.parseInt(getString(R.string.grpc_port))).usePlaintext().build();
            ETServiceGrpc.ETServiceBlockingStub stub = ETServiceGrpc.newBlockingStub(channel);

            Calendar tillCal = Calendar.getInstance();

            EtService.RetrieveFilteredDataRecords.Request retrieveFilteredDataRecordsRequestMessage = EtService.RetrieveFilteredDataRecords.Request.newBuilder()
                    .setUserId(loginPrefs.getInt(AuthenticationActivity.user_id, -1))
                    .setEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                    .setTargetEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                    .setTargetCampaignId(Integer.parseInt(getString(R.string.stress_campaign_id)))
                    .setTargetDataSourceId(configPrefs.getInt("LOCATIONS_LIST", -1))
                    .setFromTimestamp(0)
                    .setTillTimestamp(tillCal.getTimeInMillis())
                    .build();
            try {
                final EtService.RetrieveFilteredDataRecords.Response responseMessage = stub.retrieveFilteredDataRecords(retrieveFilteredDataRecordsRequestMessage);
                if (responseMessage.getSuccess()) {
                    // checkByteString
                    List<ByteString> values = responseMessage.getValueList();
                    final SharedPreferences locationPrefs = getSharedPreferences("UserLocations", MODE_PRIVATE);
                    SharedPreferences.Editor editor = locationPrefs.edit();
                    for (ByteString value : values) {
                        Log.d(TAG, "location list from server : " + value.toString() + " " + value.toString().length());
                        String[] splitValue = value.toString().split(" ");
                        if (splitValue.length == 5) {

                            String placeUserName = splitValue[0];
                            editor.putString(placeUserName + "_ADDRESS", splitValue[1].replace('_', ' '));
                            editor.putFloat(placeUserName + "_LAT", Float.parseFloat(splitValue[2]));
                            editor.putFloat(placeUserName + "_LNG", Float.parseFloat(splitValue[3]));
                            editor.putBoolean(placeUserName + "_isDeleted", Boolean.parseBoolean(splitValue[4]));

                            if (Boolean.parseBoolean(splitValue[4])) {
                                String curLocationList = locationPrefs.getString("locationList", "");
                                if (curLocationList.contains(placeUserName)) {
                                    String newLocationList = curLocationList.replace((" " + placeUserName), "");
                                    editor.putString("locationList", newLocationList);
                                }
                            }
                            else
                                editor.putString("locationList", String.format("%s %s", locationPrefs.getString("locationList", ""), placeUserName));

                            editor.apply();

                            LatLng position = new LatLng(Float.parseFloat(splitValue[2]), Float.parseFloat(splitValue[3]));

                            GeofenceHelper.startGeofence(getApplicationContext(), placeUserName, position, GEOFENCE_RADIUS_DEFAULT);
                        }
                    }
                    locationList = locationPrefs.getString("locationList", "");
                }
            } catch (StatusRuntimeException e) {
                Log.d("Tools", "DataCollectorService.setUpHeartbeatSubmissionThread() exception: " + e.getMessage());
                e.printStackTrace();
            } finally {
                channel.shutdown();
            }
        } else {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.when_network_unable), Toast.LENGTH_LONG).show();
        }

        return locationList;
    }

    private void getCurrentLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    onLocationChanged(location);

                    LatLng latLng = selectedLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    // reverse geocoding
                    final Geocoder geocoder = new Geocoder(getApplicationContext());
                    List<Address> addressesList = null;
                    try {
                        addressesList = geocoder.getFromLocation(selectedLatLng.latitude, selectedLatLng.longitude, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (addressesList != null) {
                        if (addressesList.size() == 0) {
                            Toast.makeText(getApplicationContext(), "현재 장소와 매치되는 주소를 찾지 못했습니다.", Toast.LENGTH_LONG).show();
                        } else {
                            selectedAddress = addressesList.get(0).getAddressLine(0);
                            selectedName = addressesList.get(0).getFeatureName();
                        }
                    }

                    MarkerOptions optionsMarker = new MarkerOptions()
                            .position(latLng);

                    if (mMap != null) {
                        if (currentGeofenceMarker != null) {
                            currentGeofenceMarker.remove();
                        }
                        currentGeofenceMarker = mMap.addMarker(optionsMarker);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_LEVLE));
                    }

                });
    }

    //region Internal classes
    static class StoreLocation {
        LatLng mLatLng;
        String mId;
        String mName;
        String mAddress;
        boolean isDeleted;

        StoreLocation(String id, LatLng latlng, String name, String address, boolean isDeleted) {
            mLatLng = latlng;
            mId = id;
            mName = name;
            mAddress = address;
            this.isDeleted = isDeleted;
        }

        LatLng getmLatLng() {
            return mLatLng;
        }

        String getmId() {
            return mId;
        }

        String getmName() {
            return mName;
        }

        String getmAddress() {
            return mAddress;
        }

        boolean getIsDeleted() {
            return this.isDeleted;
        }


    }

}


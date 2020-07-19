package kr.ac.inha.mindscope;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import static kr.ac.inha.mindscope.MapsActivity.GEOFENCE_RADIUS_DEFAULT;
import static kr.ac.inha.mindscope.MapsActivity.ID_HOME;

public class SelectedPlaceSaveActivity extends AppCompatActivity {

    private static final String TAG = "SelectedPlaceSave";
    private Toolbar toolbar;

    private TextView placeNameTextView;
    private TextView placeAddressTextView;

    private String selectedPlaceName;
    private String selectedPlaceAddress;
    private String selectedPlaceUserName;
    private Double selectedLat;
    private Double selectedLng;
    PlaceDbHelper dbHelper;
    EditText editText;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_palce_save);

        dbHelper = new PlaceDbHelper(getApplicationContext());


        placeNameTextView= (TextView) findViewById(R.id.current_place_name);

        placeAddressTextView = (TextView) findViewById(R.id.current_place_address);
        editText = (EditText) findViewById(R.id.current_place_user_name);

        Intent intent = getIntent();

        selectedPlaceName = intent.getExtras().getString("name");
        placeNameTextView.setText(selectedPlaceName);
        selectedPlaceAddress = intent.getExtras().getString("address");
        placeAddressTextView.setText(selectedPlaceAddress);
        selectedLat = intent.getExtras().getDouble("lat");
        selectedLng = intent.getExtras().getDouble("lng");

        if(intent.getExtras().getInt("editcode") == 2){
            selectedPlaceUserName = intent.getExtras().getString("placeusername");
            editText.setText(selectedPlaceUserName);
        }


        LatLng currentLatLng = new LatLng(selectedLat, selectedLng);

        toolbar = (Toolbar) findViewById(R.id.toolbar_map_current_save);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.toolbar_save:
                if(editText.getText().toString().equals("")){
                    Toast.makeText(this, "장소를 입력해주세요!", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this, "장소 저장", Toast.LENGTH_SHORT).show();
                    dbHelper.deletePlaceData(selectedPlaceName);
                    dbHelper.insertPlaceData(selectedPlaceName, selectedPlaceAddress, editText.getText().toString(), selectedLat, selectedLng);

                    // setLocation
                    setLocation(editText.getText().toString(), selectedLat, selectedLng);
                    Log.i(TAG, "placeUserName, double lat lng, float lat lng: " + editText.getText().toString() + ", " + selectedLat + ", " + selectedLng + ", " + selectedLat.floatValue() + ", " + selectedLng.floatValue());
                    finish();
                    // TODO EasyTrack server에 upload
                }
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.toolbar_btn_save, menu);
        return true;
    }

    private void setLocation(String placeUserName, Double lat, Double lng){
        final SharedPreferences locationPrefs = getSharedPreferences("UserLocations", MODE_PRIVATE);
        SharedPreferences.Editor editor = locationPrefs.edit();

        SharedPreferences confPrefs = getSharedPreferences("Configurations", Context.MODE_PRIVATE);
        int dataSourceId = confPrefs.getInt("LOCATIONS_MANUAL", -1);
        assert dataSourceId != -1;
        long nowTime = System.currentTimeMillis();


        if(placeUserName.equals("집")){
            editor.putFloat(ID_HOME + "_LAT", lat.floatValue());
            editor.putFloat(ID_HOME + "_LNG", lng.floatValue());
            DbMgr.saveMixedData(dataSourceId, nowTime, 1.0f, nowTime, ID_HOME, selectedLat.floatValue(), selectedLng.floatValue());
        }else{
            editor.putFloat(placeUserName + "_LAT", lat.floatValue());
            editor.putFloat(placeUserName + "_LNG", lng.floatValue());
            DbMgr.saveMixedData(dataSourceId, nowTime, 1.0f, nowTime, ID_HOME, selectedLat.floatValue(), selectedLng.floatValue());
        }
        editor.apply();

        String location_id = placeUserName;
        LatLng position = new LatLng(lat, lng);
        int radius = GEOFENCE_RADIUS_DEFAULT;

        GeofenceHelper.startGeofence(getApplicationContext(), location_id, position, radius);




        Toast.makeText(getApplicationContext(), placeUserName + getString(R.string.location_set), Toast.LENGTH_SHORT).show();

    }
}
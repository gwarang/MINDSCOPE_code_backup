package kr.ac.inha.mindscope;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static android.content.Context.MODE_PRIVATE;
import static kr.ac.inha.mindscope.MapsActivity.ID_HOME;

public class PlaceDBAdapter extends ArrayAdapter<PlaceInfo> {

    private static final String TAG = "PlaceDBAdapter";
    public static final int DELETE_CODE = 1;
    public static final int EDIT_CODE = 2;

    public String selectedPlaceName;
    public String selectedPlaceAddress;
    public String selectedPlaceUserName;
    public Double selectedLat;
    public Double selectedLng;
    PlaceInfo placeInfo;
    int pos;

    PlaceDbHelper dbHelper;

    private OnItemClick mCallback;
    private Context context;

    public PlaceDBAdapter(@NonNull Context context, int resource, @NonNull List<PlaceInfo> objects, OnItemClick onItemClick) {
        super(context, resource, objects);

        this.mCallback = onItemClick;
        this.context = context;
    }


    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        pos = position;
        placeInfo = getItem(position);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_place_list, null);
        }

        LinearLayout container = convertView.findViewById(R.id.container_place_info);

        TextView placeNameView = convertView.findViewById(R.id.listview_place_name);
        TextView placeAddressView = convertView.findViewById(R.id.listview_place_address);
        TextView placeUserNameView = convertView.findViewById(R.id.listview_place_user_name);

        placeNameView.setText(placeInfo.placeName);
        placeAddressView.setText(placeInfo.placeAddress);
        placeUserNameView.setText(placeInfo.placeUserName);

        Button editButton = convertView.findViewById(R.id.btn_place_info_edit);
        Button deleteButton = convertView.findViewById(R.id.btn_place_info_delete);


//        container.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(getContext(), "listview click", Toast.LENGTH_SHORT).show();
//                // activity로 placeinfo 전달
//                PlaceInfo placeInfo = getItem(pos);
//                Log.i(TAG, placeInfo.placeName + " here");
////                mCallback.onClick(placeInfo);
//
//            }
//        });

        editButton.setOnClickListener(view -> {
//                Toast.makeText(getContext(), "수정", Toast.LENGTH_SHORT).show();
            int pos = position;
            PlaceInfo deletePlaceInfo = getItem(pos);
            Log.i(TAG, deletePlaceInfo.placeName);
            if (mCallback == null)
                Log.e(TAG, "mcallback is null");
            else
                mCallback.onClick(deletePlaceInfo, EDIT_CODE);
        });

        deleteButton.setOnClickListener(view -> {
//                Toast.makeText(getContext(), "삭제", Toast.LENGTH_SHORT).show();
            int pos = position;

            String locationId;

            if (placeUserNameView.getText().toString().equals("집")) {
                locationId = ID_HOME;
            } else {
                locationId = placeUserNameView.getText().toString();
            }
            displayRemoveDialog(locationId);

            PlaceInfo deletePlaceInfo = getItem(pos);
            Log.i(TAG, deletePlaceInfo.placeName + " by delete btn");
            if (mCallback == null) {
                Log.e(TAG, "mcallback is null");
            } else {
                mCallback.onClick(deletePlaceInfo, DELETE_CODE);
            }


//                dbHelper = new PlaceDbHelper(getContext());
//                dbHelper.deletePlaceData(deletePlaceInfo.placeName);
        });


        return convertView;
    }

    public void displayRemoveDialog(final String locationId) {
        final SharedPreferences locationPrefs = context.getSharedPreferences("UserLocations", MODE_PRIVATE);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getString(R.string.location_remove_confirmation));
        builder.setPositiveButton("OK", (dialog, which) -> {
            GeofenceHelper.removeGeofence(context, locationId);
            SharedPreferences.Editor editor = locationPrefs.edit();
            editor.remove(locationId + "_LAT");
            editor.remove(locationId + "_LNG");
            editor.remove(locationId + "_ENTERED_TIME");
            editor.remove(locationId + "_ADDRESS");
            editor.remove(locationId + "_NAME");
            String newLocationList = locationPrefs.getString("locationList", "").replace(" " + locationId, "");
            editor.putString("locationList", newLocationList);

            editor.apply();
            Toast.makeText(context, context.getString(R.string.location_removed), Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}

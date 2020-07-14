package kr.ac.inha.mindscope;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

    public PlaceDBAdapter(@NonNull Context context, int resource, @NonNull List<PlaceInfo> objects, OnItemClick onItemClick) {
        super(context, resource, objects);

        this.mCallback = onItemClick;
    }


    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        pos = position;
        placeInfo = getItem(position);

        if(convertView == null){
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

        Button editButton = (Button) convertView.findViewById(R.id.btn_place_info_edit);
        Button deleteButton = (Button) convertView.findViewById(R.id.btn_place_info_delete);


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

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(getContext(), "수정", Toast.LENGTH_SHORT).show();
                int pos = position;
                PlaceInfo deletePlaceInfo = getItem(pos);
                Log.i(TAG, deletePlaceInfo.placeName);
                if(mCallback == null)
                    Log.e(TAG, "mcallback is null");
                else
                    mCallback.onClick(deletePlaceInfo, EDIT_CODE);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(getContext(), "삭제", Toast.LENGTH_SHORT).show();
                int pos = position;
                PlaceInfo deletePlaceInfo = getItem(pos);
                Log.i(TAG, deletePlaceInfo.placeName + " by delete btn");
                if(mCallback == null)
                    Log.e(TAG, "mcallback is null");
                else
                    mCallback.onClick(deletePlaceInfo, DELETE_CODE);

//                dbHelper = new PlaceDbHelper(getContext());
//                dbHelper.deletePlaceData(deletePlaceInfo.placeName);
            }
        });


        return convertView;
    }
}

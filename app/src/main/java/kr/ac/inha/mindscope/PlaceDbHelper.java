package kr.ac.inha.mindscope;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class PlaceDbHelper extends SQLiteOpenHelper {


    private static final String DB_NAME = "PlaceList.db";
    private static final String PLACE_TABLE = "place_list";

    private static final String PLACE_COL_1 = "PLACE_NAME";
    private static final String PLACE_COL_2 = "PLACE_ADDRESS";
    private static final String PLACE_COL_3 = "PLACE_USER_NAME";
    private static final String PLACE_COL_4 = "PLACE_LAT";
    private static final String PLACE_COL_5 = "PLACE_LNG";

    public PlaceDbHelper(Context context){
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table " + PLACE_TABLE +
                "(" +
                PLACE_COL_1 + " TEXT DEFAULT(0), " +
                PLACE_COL_2 + " TEXT DEFAULT(0), " +
                PLACE_COL_3 + " TEXT DEFAULT(0), " +
                PLACE_COL_4 + " REAL DEFAULT(0), " +
                PLACE_COL_5 + " REAL DEFAULT(0) " +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PLACE_TABLE);
        onCreate(sqLiteDatabase);
    }

    //region DB operations with place data

    public synchronized void insertPlaceData(String placeName, String placeAddress, String placeUserName, Double placeLat, Double placeLng) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(PLACE_COL_1, placeName);
        contentValues.put(PLACE_COL_2, placeAddress);
        contentValues.put(PLACE_COL_3, placeUserName);
        contentValues.put(PLACE_COL_4, placeLat);
        contentValues.put(PLACE_COL_5, placeLng);
        db.insert(PLACE_TABLE, null, contentValues);
    }

    public Integer deletePlaceData(String placeName) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(PLACE_TABLE, PLACE_COL_1 + " = ?", new String[]{placeName});
    }

    public ArrayList getPlaceData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + PLACE_TABLE, null);

        ArrayList placeInfo = new ArrayList<PlaceInfo>();

        if(res != null && res.moveToFirst()){
            do{
                placeInfo.add(new PlaceInfo(res.getString(0), res.getString(1), res.getString(2), res.getDouble(3), res.getDouble(4)));
            }while(res.moveToNext());
        }
        return placeInfo;

//        List<String[]> dataResultList = new ArrayList<>();
//        if (res.moveToFirst()) {
//            do {
//                String[] data = new String[5];
//                data[0] = res.getString(0);
//                data[1] = res.getString(1);
//                data[2] = res.getString(2);
//                data[3] = res.getString(3);
//                data[4] = res.getString(4);
//                dataResultList.add(data);
//            } while (res.moveToNext());
//        }
//        res.close();
//        return dataResultList;
    }
    //endregion
}

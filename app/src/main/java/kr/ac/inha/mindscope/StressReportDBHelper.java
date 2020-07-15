package kr.ac.inha.mindscope;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Locale;

public class StressReportDBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "StressReport.db";
    private static final String STRESS_REPORT_TABLE = "stress_report_list";

    private static final String STRESS_REPORT_COL_1 = "STRESS_LEVEL";
    private static final String STRESS_REPORT_COL_2 = "DAY_NUM";
    private static final String STRESS_REPORT_COL_3 = "REPORT_ORDER";
    private static final String STRESS_REPORT_COL_4 = "ACCURACY";
    private static final String STRESS_REPORT_COL_5 = "FEATURE_IDS";

    public StressReportDBHelper(Context context){
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table " + STRESS_REPORT_TABLE +
                "(" +
                STRESS_REPORT_COL_1 + " INTEGER DEFAULT(0), " +
                STRESS_REPORT_COL_2 + " INTEGER DEFAULT(0), " +
                STRESS_REPORT_COL_3 + " INTEGER DEFAULT(0), " +
                STRESS_REPORT_COL_4 + " INTEGER DEFAULT(0), " +
                STRESS_REPORT_COL_5 + " TEXT DEFAULT(0) " +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + STRESS_REPORT_TABLE);
        onCreate(sqLiteDatabase);
    }

    public synchronized void insertStressReportData(int stress_level, int day_num, int report_order, int accuracy, String feature_ids){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(STRESS_REPORT_COL_1, stress_level);
        contentValues.put(STRESS_REPORT_COL_2, day_num);
        contentValues.put(STRESS_REPORT_COL_3, report_order);
        contentValues.put(STRESS_REPORT_COL_4, accuracy);
        contentValues.put(STRESS_REPORT_COL_5, feature_ids);
        db.insert(STRESS_REPORT_TABLE, null, contentValues);
    }

    public ArrayList getStressReportData(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + STRESS_REPORT_TABLE, null);
//        Cursor res = db.rawQuery("select  from " + STRESS_REPORT_TABLE, null);

        ArrayList stressReportDataArray = new ArrayList<StressReportData>();

        if(res != null && res.moveToFirst()){
            do{
                stressReportDataArray.add(new StressReportData(res.getInt(res.getColumnIndex(STRESS_REPORT_COL_1)),
                        res.getInt(res.getColumnIndex(STRESS_REPORT_COL_2)),
                        res.getInt(res.getColumnIndex(STRESS_REPORT_COL_3)),
                        res.getInt(res.getColumnIndex(STRESS_REPORT_COL_4)),
                        res.getString(res.getColumnIndex(STRESS_REPORT_COL_5))));
            }while(res.moveToNext());
        }
        return stressReportDataArray;
    }

    public class StressReportData{
        int stress_level;
        int day_num;
        int report_order;
        int accuracy;
        String featrue_ids;

        public StressReportData(int stress_level, int day_num, int report_order, int accuracy, String featrue_ids){
            this.stress_level = stress_level;
            this.day_num = day_num;
            this.report_order = report_order;
            this.accuracy = accuracy;
            this.featrue_ids = featrue_ids;
        }

        public String toString(){
            return String.format(Locale.KOREA, "%d %d %d %d %s", stress_level, day_num, report_order, accuracy, featrue_ids);
        }


    }
}



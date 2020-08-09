package kr.ac.inha.mindscope;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class SplashActivity extends Activity {

    private final int SPLASH_DISPLAY_TIME = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Log.d("SplashActivity", "onCreate");

        SharedPreferences lastPagePrefs = getSharedPreferences("LastPage", MODE_PRIVATE);
        SharedPreferences.Editor lastPagePrefsEditor = lastPagePrefs.edit();
        lastPagePrefsEditor.putString("last_open_nav_frg", "me");
        lastPagePrefsEditor.putInt("last_open_tab_position", 0);
        lastPagePrefsEditor.apply();

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            startActivity(new Intent(getApplication(), AuthenticationActivity.class));
            SplashActivity.this.finish();
        }, SPLASH_DISPLAY_TIME);
    }

    @Override
    public void onBackPressed() {
        // remove back pressed
    }
}
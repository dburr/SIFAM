package com.caraxian.sifam;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class OverlayService extends Service {

    private WindowManager windowManager;
    private ImageView forceQuitButton;
    private TextView accountNameTextView;
    @Override public IBinder onBind(Intent intent) {
        // Not used
        return null;
    }

    @Override public void onCreate() {
        super.onCreate();
        SIFAM.log("OverlayService > onCreate");
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 0;

        if (SIFAM.CLOSE_BUTTON) {
            forceQuitButton = new ImageView(this);
            forceQuitButton.setImageResource(R.drawable.ic_close_red_24dp);

            forceQuitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent sifamActivity = new Intent(SIFAM.getContext(), MainActivity.class);
                    sifamActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(sifamActivity);
                }
            });
            windowManager.addView(forceQuitButton, params);
        }
        if (SIFAM.OVERLAY_NAME){
            accountNameTextView = new TextView(this);
            accountNameTextView.setText(SIFAM.lastLoadedAccountName);
            accountNameTextView.setBackgroundColor(Color.BLACK);
            accountNameTextView.setTextColor(Color.WHITE);
            accountNameTextView.setTextSize(18);
            accountNameTextView.setPadding(3,0,3,3);
            params.x = 24;
            windowManager.addView(accountNameTextView,params);
        }






    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (forceQuitButton != null) windowManager.removeView(forceQuitButton);
        if (accountNameTextView != null) windowManager.removeView(accountNameTextView);
    }
}


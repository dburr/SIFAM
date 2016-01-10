package com.caraxian.sifam;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ExportAccounts extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SIFAM.log("ExportAccounts > onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_accounts);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("SIFAM: Backup Accounts");
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        SIFAM.log("ExportAccounts > onOptionsMenuSelected");
        switch (item.getItemId()) {
            case android.R.id.home: {
                onBackPressed();
                return true;
            }
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        SIFAM.log("ExportAccounts > onBackPressed");
        super.onBackPressed();
        finish();
    }

    public void doBackup(View v){
        findViewById(R.id.backupSuccessful).setVisibility(View.GONE);
        findViewById(R.id.backupFailed).setVisibility(View.GONE);
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String timestamp = sdf.format(cal.getTime());
        SIFAM.exportDatabse("SIFAM.db",timestamp + ".db");
    }




}

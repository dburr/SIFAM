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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = sdf.format(cal.getTime());
        exportDatabse("SIFAM.db",timestamp + ".db");
    }

    public void exportDatabse(String databaseName, String backup) {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();
            if (sd.canWrite()) {
                if (new File(sd,"SIFAM Backup").exists() == false){
                    new File(sd,"SIFAM Backup").mkdir();
                }
                String currentDBPath = "//data//"+getPackageName()+"//databases//"+databaseName+"";
                String backupDBPath = "SIFAM Backup//" +backup;
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);
                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    findViewById(R.id.backupSuccessful).setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            findViewById(R.id.backupFailed).setVisibility(View.VISIBLE);
            SIFAM.log(e);
        }
    }


}

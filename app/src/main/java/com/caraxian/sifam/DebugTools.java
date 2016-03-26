package com.caraxian.sifam;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

public class DebugTools extends AppCompatActivity {

    private boolean RUNNING = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug_tools);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        SIFAM.log("DebugTools > onOptionsMenuSelected");
        switch (item.getItemId()) {
            case android.R.id.home: {
                onBackPressed();
                return true;
            }
        }
        return false;
    }

    public void onBackPressed() {
        if (RUNNING == false) {
            SIFAM.log("DebugTools > onBackPressed");
            super.onBackPressed();
            finish();
        } else {
            SIFAM.Toast("Please wait for process to finish.");
        }
    }


    public void fixOrphanAccounts(View v) {
        Thread thread = new Thread(){
            @Override
            public void run(){
                SIFAM.log("Start: Fix Orphan Accounts");
                long folderID = -1;
                Database db = new Database();
                if (!db.folderExistsIn("OrphanRecovery", -1)) {
                    folderID = db.createFolder("OrphanRecovery", -1);
                } else {
                    folderID = db.findFolder("OrphanRecovery", -1);
                }


                final ArrayList<Account> allAccount = db.getAccounts(-1,null,0,SIFAM.SORT_BY,SIFAM.REVERSE_SORT,"",true);
                final ArrayList<Account> allFolders = db.getAllFolders();
                SIFAM.log("Total Accounts: " + allAccount.size());
                SIFAM.log("Total Folders: " + allFolders.size());

                ArrayList<Long> folderIds = new ArrayList<>();
                for (Account f : allFolders){
                    folderIds.add(f.id);
                }
                long done = 0;

                final TextView pl = (TextView)findViewById(R.id.debug_progress);

                //Find Orphan Folders
                for (Account f : allFolders){
                    done++;
                    SIFAM.log("Checking Folder: " + f.id + "(" + f.name + ") in " + f.parentFolder);
                    if (((!folderIds.contains(f.parentFolder)) || f.parentFolder==f.id) && f.parentFolder!=-1){
                        SIFAM.log("Orphan Folder: " + f.name);
                        db.moveFolder(f,folderID);
                    }else{
                        SIFAM.log("Folder seems good");
                    }
                    final long tDone = done;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pl.setText("Checking Folders\n" + tDone + "/" + allFolders.size());
                        }
                    });




                }


                //Find Orphan Accounts
                done = 0;
                for (Account a : allAccount){
                    SIFAM.log("Checking Account: " + a.id + "(" + a.name + ") in " + a.parentFolder);

                    if (a.parentFolder!=-1 && (!folderIds.contains(a.parentFolder))){
                        SIFAM.log("Orphan Account");
                        db.moveAccount(a,folderID);
                    }



                    done++;
                    final long tDone = done;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pl.setText("Checking Accounts\n" + tDone + "/" + allAccount.size());
                        }
                    });
                }
            }
        };

        thread.start();












    }


}

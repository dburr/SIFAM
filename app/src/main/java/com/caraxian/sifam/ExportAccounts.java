package com.caraxian.sifam;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

public class ExportAccounts extends AppCompatActivity {
    private ListView backupListView;
    private ArrayList<String> backupListArray;
    private ArrayAdapter<String> adapter;
    private int selectedItem = -1;
    private boolean RESTORING = false;

    private class RestoreFolder {
        long originalID;
        long newID;
        public RestoreFolder(long o, long n){
            originalID = o;
            newID = n;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SIFAM.log("ExportAccounts > onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_accounts);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("SIFAM: Backup Accounts");
        backupListView = (ListView)findViewById(R.id.backupList);
        backupListArray = new ArrayList<>();
        adapter = new ArrayAdapter<>(this,android.R.layout.simple_selectable_list_item, backupListArray);
        backupListView.setAdapter(adapter);
        backupListView.setSelector(android.R.color.holo_blue_light);
        updateBackupList();
        backupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                selectedItem = position;
            }
        });
    }

    private void updateBackupList(){
        backupListArray.clear();
        File dir = new File(Environment.getExternalStorageDirectory(),"SIFAM//Backups");
        if (dir.isDirectory()){
            for (File f : dir.listFiles()){
                if (f.isFile() && f.getName().endsWith(".db")){
                    backupListArray.add(0,f.getName().substring(0,f.getName().length()-3));
                }
            }
        }
        adapter.notifyDataSetChanged();
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
        if (RESTORING == false) {
            SIFAM.log("ExportAccounts > onBackPressed");
            super.onBackPressed();
            finish();
        }else{
            SIFAM.Toast("Please wait for backup to restore.");
        }
    }

    public void doBackup(View v){
        findViewById(R.id.backupSuccessful).setVisibility(View.GONE);
        findViewById(R.id.backupFailed).setVisibility(View.GONE);
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String timestamp = sdf.format(cal.getTime());
        SIFAM.exportDatabse("SIFAM.db", timestamp + ".db", findViewById(R.id.backupActivity));
        SIFAM.delayAction(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.backupFailed).setVisibility(View.GONE);
                        findViewById(R.id.backupSuccessful).setVisibility(View.GONE);
                        updateBackupList();
                    }
                });
            }
        }, 1000);
    }

    public void restoreAccounts(BackupDatabase restoreDB, Database mainDB, long old_folder, long new_folder){
        ArrayList<Account> accounts = restoreDB.getAccounts(old_folder);
        for (Account a : accounts){
            mainDB.saveNewAccount(a.name,a.userKey,a.userPass,a.server,new_folder);
        }
    }

    public void restoreFolders(BackupDatabase restoreDB, Database mainDB,long id,long newid){
        ArrayList<Account> folders = restoreDB.getFolders(id);
        for (Account f : folders){
            RestoreFolder r = new RestoreFolder(f.id,mainDB.createFolder(f.name, newid));
            restoreAccounts(restoreDB,mainDB,r.originalID,r.newID);
        }
    }

    public void restoreSelected(View v){
        if (selectedItem >= 0){
                findViewById(R.id.backupActivity).setVisibility(View.GONE);
                findViewById(R.id.restoreProgress).setVisibility(View.VISIBLE);
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                RESTORING = true;
                Thread restoreThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        BackupDatabase restoreDatabase;
                        try {
                            restoreDatabase = new BackupDatabase(backupListArray.get(selectedItem));
                            Database mainDB = new Database();
                            SIFAM.log(restoreDatabase.databaseName);
                            RestoreFolder rootFolder = new RestoreFolder(-1,mainDB.createFolder("Restore: " +  backupListArray.get(selectedItem),-1));
                            restoreAccounts(restoreDatabase, mainDB, rootFolder.originalID, rootFolder.newID);
                            restoreFolders(restoreDatabase, mainDB, -1, rootFolder.newID);
                        } catch (Exception e){
                            SIFAM.Toast("Error in Restore.\n Restoration may be incomplete.");
                            SIFAM.log(e);
                        } finally {
                                restoreDatabase = null;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        findViewById(R.id.backupActivity).setVisibility(View.VISIBLE);
                                        findViewById(R.id.restoreProgress).setVisibility(View.GONE);
                                        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                                        RESTORING = false;
                                    }
                                });
                        }
                    }
                });
                restoreThread.run();
            SIFAM.Toast("Started Restore");
        }else{
            SIFAM.Toast("No backup selected");
        }
    }

    public void deleteSelected(View v){
        if (selectedItem >= 0){
            final AlertDialog.Builder confirmDelete = new AlertDialog.Builder(this);
            confirmDelete.setTitle("Delete Account");
            confirmDelete.setMessage("Do you really want to delete backup '" + backupListArray.get(selectedItem) + "'?");
            confirmDelete.setIcon(android.R.drawable.ic_dialog_alert);
            confirmDelete.setPositiveButton("Delete It!", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    File delete = new File(Environment.getExternalStorageDirectory(),"SIFAM//Backups//" + backupListArray.get(selectedItem) + ".db");
                    if (delete.exists()){
                        delete.delete();
                        SIFAM.delayAction(new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        updateBackupList();
                                    }
                                });
                            }
                        }, 500);
                    }
                }
            });
            confirmDelete.setNegativeButton("Keep It", null);
            confirmDelete.show();
        }else{
            SIFAM.Toast("No backup selected");
        }
    }

}

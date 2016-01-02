package com.caraxian.sifam;

import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class ImportAccounts extends AppCompatActivity {
    TextView importInfo;
    boolean importing = false;
    long importCount = 0;
    Database db = new Database();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_accounts);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        SIFAM.log("MainActivity.java > onOptionsMenuSelected");
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
        if (!importing) {
            super.onBackPressed();
            finish();
        } else {
            SIFAM.Toast("Please wait for import to finish");
        }
    }

    private ArrayList<File> importFolder(File folder, long parentFolder, Server s) {
        SIFAM.log("Import > importFolder");
        ArrayList<File> fl = new ArrayList<>();
        if (folder.isDirectory()) {
            File[] listOfFiles = folder.listFiles();
            long iFolder;
            if (folder.getPath().equals(new File(s.gameEngineActivity.getParentFile(), "Accounts").getPath())) {
                iFolder = parentFolder;
            } else {
                iFolder = db.createFolder(folder.getName(), parentFolder);
            }
            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    SIFAM.log(" Import > importFolder > importFile > " + listOfFiles[i].toString());
                    String iName = listOfFiles[i].getName().replaceFirst(".xml$", "");
                    SIFAM.log(" Import > importFolder > importFile > name > " + iName);
                    try {
                        String text = new Scanner(listOfFiles[i], "UTF-8").useDelimiter("\\A").next();
                        final File fFile = listOfFiles[i];
                        String s1[] = text.split("<string name=\\\"\\[LOVELIVE_ID\\]user_id\\\">");
                        String s2[] = s1[1].split("<\\/string>");
                        String iUser = s2[0];
                        String s3[] = text.split("<string name=\\\"\\[LOVELIVE_PW\\]passwd\">");
                        String s4[] = s3[1].split("<\\/string>");
                        String iPass = s4[0];
                        db.saveNewAccount(iName, iUser, iPass, s.code, iFolder);
                        importCount++;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                importInfo.setText("Imported Accounts:\n" + importCount + "\n\nLast File:\n" + fFile);
                            }
                        });
                    } catch (Exception e) {
                        SIFAM.log(e);
                    }

                } else {
                    importFolder(listOfFiles[i], iFolder, s);
                }
            }
        }
        return fl;
    }

    public void startImport(final View v) {
        if (importing == false) {
            importInfo = new TextView(this);
            importing = true;
            setContentView(importInfo);
            importInfo.setGravity(Gravity.CENTER_HORIZONTAL);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Looper.prepare();
                    SIFAM.Toast("Starting Import");
                    SIFAM.log("Import > Start");
                    for (Server s : SIFAM.serverList) {
                        if (s.code.equals(v.getTag())) {
                            File oldAccountsDirectory = new File(s.gameEngineActivity.getParentFile(), "Accounts");
                            if (oldAccountsDirectory.exists()) {
                                SIFAM.log("Import > Start " + s.name);
                                String fName = "Import " + s.code;
                                if (db.folderExistsIn(fName, -1)) {
                                    int iter = 1;
                                    while (db.folderExistsIn("Import " + s.code + "(" + iter + ")", -1)) {
                                        iter++;
                                    }
                                    fName = "Import " + s.code + "(" + iter + ")";
                                }
                                long importFolder = db.createFolder(fName, -1);
                                importFolder(oldAccountsDirectory, importFolder, s);

                                SIFAM.sharedPreferences.edit().putLong("CURRENT_FOLDER", importFolder).commit();
                                SIFAM.Toast("Import Finished");
                                SIFAM.log("Import > Complete");
                                finish();
                                return;
                            }
                        }
                    }
                    SIFAM.log("Import > No Data");
                    SIFAM.Toast("No data to import.");
                    importing = false;
                }
            });
            thread.start();
        }
    }
}

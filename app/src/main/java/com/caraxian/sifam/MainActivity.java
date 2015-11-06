package com.caraxian.sifam;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.regex.Pattern;


public class MainActivity extends ActionBarActivity {
    public AlertDialog alertDialog;
    public ArrayList<Account> accountlist = new ArrayList<Account>();
    public ArrayList<Account> fullaccountlist = new ArrayList<Account>();
    String dir = "/data/data/klb.android.lovelive_en/shared_prefs/";
    String app = "klb.android.lovelive_en";
    String version = "0.5.0";
    String latestVersion = null;
    Integer oneClickReroll = 0;
    static final public Process shell = getShell();
    Integer sortMethod = 0;
    Boolean reverseSort = true;
    Account moving = null;
    public static SharedPreferences sharedPrefs;


    public static String currentFolder = "";

    public static SharedPreferences lastLoaded;


    public static Process getShell() {
        try {
            Process s = Runtime.getRuntime().exec("sh");
            DataOutputStream o = new DataOutputStream(s.getOutputStream());
            o.writeBytes("su\n");
            o.flush();
            return s;
        } catch (Exception IOException) {
            return null;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        lastLoaded = getSharedPreferences("lastLoaded", 0);
        SharedPreferences settings = getSharedPreferences("prefs", 0);

        if (sharedPrefs.getBoolean("remember_folder",true)){
            currentFolder = settings.getString("last_folder","");
        }





        MainActivity.this.setTitle("[EN] SIFAM");


        oneClickReroll = settings.getInt("oneClickReroll", 0);

        String server = settings.getString("server", "EN");

        sortMethod = settings.getInt("sortMethod", 0);
        reverseSort = settings.getBoolean("reverseSort", true);


        switch (server) {
            case "JP": {
                MainActivity.this.setTitle("[JP] SIFAM");
                dir = "/data/data/klb.android.lovelive/shared_prefs/";
                app = "klb.android.lovelive";
                break;
            }
            case "TW": {
                MainActivity.this.setTitle("[TW] SIFAM");
                dir = "/data/data/net.gamon.loveliveTW/shared_prefs/";
                app = "net.gamon.loveliveTW";
                break;
            }
            case "KR": {
                MainActivity.this.setTitle("[KR] SIFAM");
                dir = "/data/data/com.nhnent.SKLOVELIVE/shared_prefs/";
                app = "com.nhnent.SKLOVELIVE";
                break;
            }
            case "CN": {
                MainActivity.this.setTitle("[CN] SIFAM");
                dir = "/data/data/klb.android.lovelivecn/shared_prefs/";
                app = "klb.android.lovelivecn";
                break;
            }
			default: {
				MainActivity.this.setTitle("[EN] SIFAM");
                dir = "/data/data/klb.android.lovelive_en/shared_prefs/";
                app = "klb.android.lovelive_en";
                break;
			}
        }


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        try {
            Process superm = Runtime.getRuntime().exec("su -c ls");
            int exitCode = superm.waitFor();
        } catch (Exception e2) {
            errorMessage("Failed to get SuperUser Permissions");
        }


        final ListView listview = (ListView) findViewById(R.id.accounts);

        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return false;
            }
        });


        

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Account sAcc = accountlist.get(position);


                if (sAcc.isFolder) {
                    if (currentFolder.equals("")) {
                        currentFolder = sAcc.name;
                    } else {
                        currentFolder = currentFolder + "/" + sAcc.name;
                    }
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);


                    SharedPreferences settings = getSharedPreferences("prefs", 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("last_folder", currentFolder);
                    editor.commit();

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            TextView folderText = (TextView) findViewById(R.id.currentFolder);
                            if (currentFolder == "") {
                                folderText.setVisibility(View.GONE);

                            } else {
                                folderText.setVisibility(View.VISIBLE);
                                folderText.setText("Current Folder: " + currentFolder);
                            }
                            buildList();
                        }
                    }, 200);
                } else {
                    boolean canLoad = true;
                    final Account cAcc = new Account(dir + "GameEngineActivity.xml");
                    if (sAcc.exists) {
                        if (cAcc.exists) {
                            if (sAcc.isCurrent) {
                                shortToast("Already Loaded. Starting SIF");
                                startApp();
                                canLoad = false;
                            } else {
                                if (accountDuplicate(cAcc) == null) {
                                    shortToast("Current Account is not Saved!\nLoad Canceled!");
                                    canLoad = false;
                                }
                            }
                        }
                    } else {
                        shortToast("Load Failed. Invalid File?");
                        canLoad = false;
                    }


                    if (canLoad) {

                        loadAccount(sAcc);

                    }


                }

            }
        });






        registerForContextMenu(listview);

        Button b = (Button) findViewById(R.id.moveHere);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveHere();
            }
        });


        TextView folderText = (TextView) findViewById(R.id.currentFolder);
        if (currentFolder == "") {
            folderText.setVisibility(View.GONE);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        } else {
            folderText.setVisibility(View.VISIBLE);
            folderText.setText("Current Folder: " + currentFolder);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


    }


    public void loadAccount(final Account sAcc){
        loadAccount(sAcc,false);
    }
    public void loadAccount(final Account sAcc, Boolean ignoreInvalid){
        final Account cAcc = new Account(dir + "GameEngineActivity.xml");
        if (sAcc.isValid || ignoreInvalid) {
            SharedPreferences.Editor editor = lastLoaded.edit();
            String llname = "";
            if (currentFolder.equals("")) {
                llname = sAcc.name;
            } else {
                llname = currentFolder + "/" + sAcc.name;
            }

            editor.putLong(llname, System.currentTimeMillis());
            editor.commit();
            sAcc.lastLoaded = System.currentTimeMillis();

            forceCloseApp();
            final Handler delay1 = new Handler();
            if (sharedPrefs.getBoolean("auto_start", true)) {
                longToast("Loading Account '" + sAcc.name + "'");
            }
            delay1.postDelayed(new Runnable() {
                @Override
                public void run() {
                    cAcc.file.delete();
                    final Handler delay2 = new Handler();
                    delay2.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            copyFile(sAcc.file, cAcc.file);
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (sharedPrefs.getBoolean("auto_start", true)) {
                                        startApp();
                                    } else {
                                        shortToast("Loaded Account '" + sAcc.name + "'\nTap again to start SIF");
                                    }
                                    buildList();
                                }
                            }, 500);

                        }
                    }, 200);
                }
            }, 500);

        }else{
            final AlertDialog.Builder al = new AlertDialog.Builder(this);
            al.setTitle("Invalid Account Detected");
            al.setMessage(sAcc.name + " has been flagged as Invalid.\nThis means that SIF is unlikely to load it, and will most likely start a new game.\n\nAttempt to load it anyway?");
            al.setIcon(android.R.drawable.ic_dialog_alert);
            al.setPositiveButton("Load Account!", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    loadAccount(sAcc, true);
                }
            });
            al.setNegativeButton("Don't Load", null);
            al.show();
        }
    }

    public void moveHere() {

        String h = dir + "Accounts/";
        if (!currentFolder.equals("")) {
            h += currentFolder + "/";
        }
        h += moving.name + ".xml";
        final String here = h;

        if (here.equals(moving.file.toString())) {
            shortToast("Account Not Moved");
            moving = null;
            Button b = (Button) findViewById(R.id.moveHere);
            b.setVisibility(View.GONE);

        } else {

            if (!Exists(here)) {
                copyFile(moving.file, new File(here));
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        filePermissions(here);
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Account moveCheck = new Account(here);
                                if (moveCheck.exists) {
                                    if (moveCheck.hash.equals(moving.hash)) {
                                        moving.file.delete();
                                        shortToast("Account Moved");
                                        moving = null;
                                        buildList();
                                    }
                                }
                            }
                        }, 200);
                    }
                }, 200);
            } else {
                shortToast("Error. Account with same name exists here");
            }

            Button b = (Button) findViewById(R.id.moveHere);
            b.setVisibility(View.GONE);

        }


    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        if (accountlist.get(info.position).isFolder) {
            String folderName = accountlist.get(info.position).name;
            menu.setHeaderTitle("Folder: " + folderName);
            super.onCreateContextMenu(menu, v, menuInfo);
            getMenuInflater().inflate(R.menu.menu_context_folder, menu);
        } else {
            String accountName = accountlist.get(info.position).name;
            menu.setHeaderTitle(accountName);
            super.onCreateContextMenu(menu, v, menuInfo);
            getMenuInflater().inflate(R.menu.menu_context, menu);
        }
    }

    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Account acc = accountlist.get(info.position);
        String accName = accountlist.get(info.position).name;
        if (accName.startsWith("> ")) {
            accName = accName.substring(2);
        }

        final String accountName = accName;

        switch (item.getItemId()) {
            case R.id.context_rename:
                shortToast("Rename " + accountName);


                String renameAccountPath = dir + "Accounts/";
                if (currentFolder.equals("")) {
                    renameAccountPath = renameAccountPath + accountName + ".xml";
                } else {
                    renameAccountPath = renameAccountPath + currentFolder + "/" + accountName + ".xml";
                }
                final Account renameAccount = new Account(new File(renameAccountPath));

                final EditText saveName = new EditText(MainActivity.this);
                saveName.setSingleLine(true);

                saveName.setText(accountName.replace("\n", "-"));

                saveName.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void afterTextChanged(Editable s) {
                        if (isValidAccountName(s.toString())) {
                            if (s.toString().equals(renameAccount.name) || s.toString().length() == 0) {
                                MainActivity.this.alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                            } else {
                                MainActivity.this.alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                            }
                        } else {
                            MainActivity.this.alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }
                });

                saveName.setHint("Account Name");
                InputFilter[] FilterArray = new InputFilter[1];
                FilterArray[0] = new InputFilter.LengthFilter(64);
                saveName.setFilters(FilterArray);

                final AlertDialog.Builder getSaveName = new AlertDialog.Builder(MainActivity.this);

                getSaveName.setTitle("Rename Account");
                getSaveName.setView(saveName);
                getSaveName.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
                getSaveName.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String name = saveName.getText().toString();

                        if (isValidAccountName(name)) {
                            if (name.length() == 0 || name.equals(renameAccount.name)) {
                                shortToast("No Name Given");
                            } else {
                                if (!new File(dir + "Accounts/" + name + ".xml").exists()) {

                                    final String newName = name;
                                    final String newNamePath;
                                    if (currentFolder.equals("")) {
                                        newNamePath = dir + "Accounts/" + newName + ".xml";
                                    } else {
                                        newNamePath = dir + "Accounts/" + currentFolder + "/" + newName + ".xml";
                                    }

                                    debug(renameAccount.file.toString() + " -> " + newNamePath);
                                    copyFile(renameAccount.file, new File(newNamePath));
                                    shortToast("Renaming...");

                                    final Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {

                                            Account checkAccount = new Account(new File(newNamePath));
                                            if (checkAccount.exists) {
                                                if (renameAccount.hash.equals(checkAccount.hash)) {

                                                    SharedPreferences.Editor editor = lastLoaded.edit();
                                                    if (currentFolder.equals("")) {
                                                        editor.putLong(newName, renameAccount.lastLoaded);
                                                    } else {
                                                        editor.putLong(currentFolder + "/" + newName, renameAccount.lastLoaded);
                                                    }
                                                    editor.remove(renameAccount.name);
                                                    editor.commit();

                                                    renameAccount.file.delete();


                                                    final Handler handler2 = new Handler();
                                                    handler2.postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            buildList();
                                                        }
                                                    }, 500);

                                                } else {
                                                    shortToast("Raname Failed");
                                                }
                                            } else {
                                                shortToast("Rename Failed");
                                            }

                                        }
                                    }, 500);


                                } else {
                                    shortToast("Name in Use!");
                                }
                            }

                        } else {
                            shortToast("Invalid Account Name");
                        }
                    }
                });

                alertDialog = getSaveName.show();


                break;
            case R.id.context_move:

                Button b = (Button) findViewById(R.id.moveHere);
                b.setVisibility(View.VISIBLE);
                moving = acc;

                break;
            case R.id.context_delete:

                final AlertDialog.Builder al = new AlertDialog.Builder(this);
                al.setTitle("Delete Account");
                al.setMessage("Do you really want to delete '" + accountName + "'?");
                al.setIcon(android.R.drawable.ic_dialog_alert);
                al.setPositiveButton("Delete It!", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        al.setMessage("Absolutely Sure? Delete '" + accountName + "'?");
                        al.setPositiveButton("Keep It", null);
                        al.setNegativeButton("Delete It!", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {


                                SharedPreferences.Editor editor = lastLoaded.edit();
                                editor.remove(accountName);
                                editor.commit();

                                String deletePath = dir + "Accounts/";
                                if (currentFolder.equals("")) {
                                    deletePath = deletePath + accountName + ".xml";
                                } else {
                                    deletePath = deletePath + currentFolder + "/" + accountName + ".xml";
                                }

                                new Account(deletePath).file.delete();
                                final Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        shortToast("Account Deleted");
                                        buildList();
                                    }
                                }, 500);


                            }
                        });
                        al.show();
                    }
                });
                al.setNegativeButton("Keep It", null);
                al.show();


                break;
            case R.id.context_folder_delete:
                if (acc.isFolder) {
                    if (acc.file.listFiles().length == 0) {
                        acc.file.delete();
                        buildList();
                        shortToast("Folder Deleted");
                    } else {
                        shortToast("Folder Not Empty!");
                    }
                }
                break;


        }
        return true;
    }


    public void startApp() {
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(app);
        startActivity(launchIntent);
    }

    public void forceCloseApp() {
        
        String[] cmds = {"am force-stop " + app};
        runShell(cmds);
    }

    public Account accountDuplicate(Account a) {
        if (a.exists) {
            for (Account b : fullaccountlist) {
                if (!b.path.equals(a.path)) {
                    if (b.exists) {
                        if (a.hash.equals(b.hash)) {
                            return b;
                        }
                    }
                }
            }
        }
        return null;
    }

    public void saveCurrentAccountAs(String name) {
        debug("SaveCurrentAccount: '" + name + "'");
        if (isValidAccountName(name)) {
            if (name.length() == 0) {

                
                String nameMethod = sharedPrefs.getString("auto_name", "date_and_time");
                debug(nameMethod);


                if (nameMethod.equals("incremental")){

                    Integer i = 1;

                    while (name.equals("")){
                        String tn = i.toString();
                        while (tn.length()<4){tn="0"+tn;}
                        String saveAs = dir + "Accounts/";
                        if (currentFolder.equals("")) {
                            saveAs = saveAs + tn + ".xml";
                        } else {
                            saveAs = saveAs + currentFolder + "/" + tn + ".xml";
                        }
                        if (!Exists(saveAs)){
                            name = tn;
                        }

                        ++i;
                    }

                }else {
                    Calendar cal = Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy MM dd 'at' HH:mm:ss");
                    name = sdf.format(cal.getTime());
                }


            }
            debug("SaveCurrentAccount: '" + name + "'");
            

            String saveAs = dir + "Accounts/";
            if (currentFolder.equals("")) {
                saveAs = saveAs + name + ".xml";
            } else {
                saveAs = saveAs + currentFolder + "/" + name + ".xml";
            }

            if (!new File(saveAs).exists()) {

                copyFile(new File(dir + "GameEngineActivity.xml"), new File(saveAs));
                shortToast("Saving...");

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        buildList();
                    }
                }, 500);


            } else {
                shortToast("Name in Use!");
            }


        } else {
            shortToast("Invalid Account Name");
        }
    }


    public void saveCurrentAccount() {
        saveCurrentAccount(false);
    }

    public void saveCurrentAccount(Boolean ignoreInvalid) {
        Account current = new Account(dir + "GameEngineActivity.xml");

        if (current.exists) {


            Account match = accountDuplicate(current);

            if (match == null || sharedPrefs.getBoolean("debug_allowDuplicateSaves",false)) {

                if (current.isValid || ignoreInvalid) {

                    if (sharedPrefs.getBoolean("quick_save", false)) {
                        saveCurrentAccountAs("");
                    } else {
                        final EditText saveName = new EditText(MainActivity.this);
                        saveName.setSingleLine(true);
                        saveName.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void afterTextChanged(Editable s) {
                                if (isValidAccountName(s.toString())) {
                                    MainActivity.this.alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                                } else {
                                    MainActivity.this.alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                                }
                            }

                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {

                            }
                        });


                        saveName.setHint("Account Name");
                        
                        InputFilter[] FilterArray = new InputFilter[1];
                        FilterArray[0] = new InputFilter.LengthFilter(64);
                        saveName.setFilters(FilterArray);

                        final AlertDialog.Builder getSaveName = new AlertDialog.Builder(MainActivity.this);

                        getSaveName.setTitle("Save Account As");
                        
                        getSaveName.setView(saveName);
                        getSaveName.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        });
                        getSaveName.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String name = saveName.getText().toString();
                                saveCurrentAccountAs(name);
                            }
                        });

                        alertDialog = getSaveName.show();
                        


                    }
                } else {


                    final AlertDialog.Builder al = new AlertDialog.Builder(this);
                    al.setTitle("Invalid Account Detected");
                    al.setMessage("The current data has been detected as invalid.\nThis usually happens when saving data before  you have selected your starting card.\n\nDo you want to save the data anyway?\n(Use this if you think it was flagged invalid incorrectly)");
                    al.setIcon(android.R.drawable.ic_dialog_alert);
                    al.setPositiveButton("Save It!", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            saveCurrentAccount(true);
                        }
                    });
                    al.setNegativeButton("Don't Save", null);
                    al.show();


                }
            } else {
                String asaName = match.file.toString();
                asaName = asaName.replaceFirst(dir + "Accounts/", "");
                asaName = asaName.substring(0, asaName.length() - 4);
                shortToast("Already Saved As '" + asaName + "'");
            }


        } else {
            shortToast("No Data to Save!");
        }

    }

    public boolean isValidFolderName(String s) {
        if (s.length() < 3) return false;
        if (s.length() > 16) return false;
        if (s.startsWith(" ")) return false;
        if (s.endsWith(" ")) return false;
        Pattern p = Pattern.compile("[^a-zA-Z0-9 \\-]");
        boolean hasSpecialChar = p.matcher(s).find();
        if (hasSpecialChar) {
            return false;
        }
        return true;
    }

    public boolean isValidAccountName(String s) {
        if (s.length() == 0) return true;
        if (s.length() < 3) return false;
        if (s.length() > 64) return false;
        if (s.contains("/")) return false;
        if (s.contains("\\")) return false;
        if (s.startsWith(".")) return false;
        if (s.endsWith(".")) return false;
        if (s.startsWith(" ")) return false;
        if (s.endsWith(" ")) return false;
        if (s.endsWith(".xml")) return false;
        if (s.contains("  ")) return false;
        if (s.contains("..")) return false;
        if (s.contains("\n")) return false;
        if (s.startsWith(">")) return false;
        if (s.startsWith("<")) return false;
        return true;
    }

    public void errorMessage(String error) {
        TextView errorText = (TextView) findViewById(R.id.textView);
        errorText.setText(error);
    }


    public void onResume() {
        super.onResume();
        mainFiles(0);
        buildList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public void onBackPressed() {
        if (!currentFolder.equals("")) {
            if (currentFolder.contains("/")) {
                int index = currentFolder.lastIndexOf('/');
                currentFolder = currentFolder.substring(0, index);
            } else {
                currentFolder = "";
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }

        } else {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        SharedPreferences settings = getSharedPreferences("prefs", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("last_folder", currentFolder);
        editor.commit();

        TextView folderText = (TextView) findViewById(R.id.currentFolder);
        ListView listview = (ListView) findViewById(R.id.accounts);
        if (currentFolder == "") {
            folderText.setVisibility(View.GONE);

        } else {
            folderText.setVisibility(View.VISIBLE);
            folderText.setText("Current Folder: " + currentFolder);
        }

        buildList();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        switch (id) {
            case android.R.id.home: {
                onBackPressed();
                break;
            }
            case R.id.action_save: {
                saveCurrentAccount();
                break;
            }
            case R.id.action_about: {
                final AlertDialog.Builder about = new AlertDialog.Builder(this);
                about.setTitle("SIF Account Manager");
                about.setMessage("Version: " + version + "\nCreated by Caraxian");
                about.setNeutralButton("Close", null);
                about.setPositiveButton("Website", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://caraxian.com/android/SIFAM"));
                        startActivity(browserIntent);
                    }
                });
                about.show();
                break;
            }
            case R.id.action_settings: {
                startActivity(new Intent(this, Preferences.class));
                break;
            }
            case R.id.action_refresh: {
                buildList();
                break;
            }
            case R.id.action_forceclose: {
                forceCloseApp();
                break;
            }
            case R.id.action_delete: {
                Account current = new Account(dir + "GameEngineActivity.xml");

                if (current.exists) {
                    final Account check = accountDuplicate(current);

                    final AlertDialog.Builder al = new AlertDialog.Builder(this);
                    al.setTitle("Delete Account");
                    if (check == null) {
                        al.setMessage("Do you really want to delete the currently loaded account?\n\nIt DOES NOT have a backup, and will be deleted FOREVER");
                    } else {
                        al.setMessage("Do you really want to delete the currently loaded account?\n\nIt DOES have a backup called '" + check.name + "' which WILL NOT be deleted.");
                    }
                    al.setIcon(android.R.drawable.ic_dialog_alert);
                    al.setPositiveButton("Delete It!", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            if (check == null) {
                                al.setMessage("Do you really want to delete the currently loaded account?\n\nIt DOES NOT have a backup, and will be deleted FOREVER\n\nAbsolutely Sure? Last Chance.");
                                al.setPositiveButton("Keep It", null);
                                al.setNegativeButton("Delete It!", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        new Account(dir + "GameEngineActivity.xml").file.delete();
                                        final Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                shortToast("Account Deleted");
                                                buildList();
                                            }
                                        }, 500);

                                    }
                                });
                                al.show();
                            } else {
                                new Account(dir + "GameEngineActivity.xml").file.delete();
                                shortToast("Account Deleted");


                            }
                        }
                    });
                    al.setNegativeButton("Keep It", null);
                    al.show();
                } else {
                    shortToast("No account to delete");
                }
                break;
            }
            case R.id.action_sort: {
                String[] sorts = {"By Date (Oldest First)", "By Date (Newest First)", "By Name (A-Z)", "By Name (Z-A)", "Last Loaded (Recent First)", "Last Loaded (Recent Last)"};

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Change Sort")
                        .setItems(sorts, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0: {
                                        
                                        sortMethod = 0;
                                        reverseSort = true;
                                        break;
                                    }
                                    case 1: {
                                        sortMethod = 0;
                                        reverseSort = false;
                                        
                                        break;
                                    }
                                    case 2: {
                                        sortMethod = 1;
                                        reverseSort = false;
                                        
                                        break;
                                    }
                                    case 3: {
                                        sortMethod = 1;
                                        reverseSort = true;
                                        
                                        break;
                                    }
                                    case 4: {
                                        
                                        sortMethod = 2;
                                        reverseSort = false;
                                        break;
                                    }
                                    case 5: {
                                        
                                        sortMethod = 2;
                                        reverseSort = true;
                                        break;
                                    }
                                    default: {
                                        break;
                                    }
                                }
                                SharedPreferences settings = getSharedPreferences("prefs", 0);
                                SharedPreferences.Editor editor = settings.edit();
                                editor.putInt("sortMethod", sortMethod);
                                editor.putBoolean("reverseSort", reverseSort);
                                editor.commit();
                                buildList();
                            }
                        });

                builder.create().show();
                break;
            }
            case R.id.action_switch: {
                String[] servers = {"EN", "JP", "TW", "KR", "CN"};
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Select Server")
                        .setItems(servers, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                try {
                                    Process superm = Runtime.getRuntime().exec("su -c ls");
                                    int exitCode = superm.waitFor();
                                } catch (Exception e2) {
                                    errorMessage("Failed to get SuperUser Permissions");
                                }

                                switch (which) {
                                    case 0: {
                                        shortToast("Switching to EN");
                                        dir = "/data/data/klb.android.lovelive_en/shared_prefs/";
                                        app = "klb.android.lovelive_en";
                                        SharedPreferences settings = getSharedPreferences("prefs", 0);
                                        SharedPreferences.Editor editor = settings.edit();
                                        editor.putString("server", "EN");
                                        editor.remove("jpServer");
                                        MainActivity.this.setTitle("[EN] SIFAM");
                                        
                                        editor.commit();
                                        mainFiles(0);
                                        break;
                                    }

                                    case 1: {
                                        shortToast("Switching to JP");
                                        dir = "/data/data/klb.android.lovelive/shared_prefs/";
                                        app = "klb.android.lovelive";
                                        SharedPreferences settings = getSharedPreferences("prefs", 0);
                                        SharedPreferences.Editor editor = settings.edit();
                                        editor.putString("server", "JP");
                                        editor.remove("jpServer");
                                        MainActivity.this.setTitle("[JP] SIFAM");
                                        
                                        editor.commit();
                                        mainFiles(0);
                                        break;
                                    }

                                    case 2: {
                                        shortToast("Switching to TW");
                                        dir = "/data/data/net.gamon.loveliveTW/shared_prefs/";
                                        app = "net.gamon.loveliveTW";
                                        SharedPreferences settings = getSharedPreferences("prefs", 0);
                                        SharedPreferences.Editor editor = settings.edit();
                                        editor.putString("server", "TW");
                                        editor.remove("jpServer");
                                        MainActivity.this.setTitle("[TW] SIFAM");
                                        
                                        editor.commit();
                                        mainFiles(0);
                                        break;
                                    }

                                    case 3: {
                                        shortToast("Switching to KR");
                                        dir = "/data/data/com.nhnent.SKLOVELIVE/shared_prefs/";
                                        app = "com.nhnent.SKLOVELIVE";
                                        SharedPreferences settings = getSharedPreferences("prefs", 0);
                                        SharedPreferences.Editor editor = settings.edit();
                                        editor.putString("server", "KR");
                                        editor.remove("jpServer");
                                        MainActivity.this.setTitle("[KR] SIFAM");
                                        
                                        editor.commit();
                                        mainFiles(0);


                                        break;
                                    }
                                    case 4: {
                                        shortToast("Switching to CN");
                                        dir = "/data/data/klb.android.lovelivecn/shared_prefs/";
                                        app = "klb.android.lovelivecn";
                                        SharedPreferences settings = getSharedPreferences("prefs", 0);
                                        SharedPreferences.Editor editor = settings.edit();
                                        editor.putString("server", "CN");
                                        editor.remove("jpServer");
                                        MainActivity.this.setTitle("[CN] SIFAM");
                                        
                                        editor.commit();


                                        final AlertDialog.Builder al = new AlertDialog.Builder(MainActivity.this);
                                        al.setTitle("CN Server Warning");
                                        al.setMessage("This app is completely untested with the CN server.\nUse with caution.");
                                        al.setIcon(android.R.drawable.ic_dialog_alert);
                                        al.setPositiveButton("Okay", null);
                                        al.show();


                                        break;

                                    }


                                }
                                currentFolder = "";


                                TextView folderText = (TextView) findViewById(R.id.currentFolder);
                                if (currentFolder == "") {
                                    folderText.setVisibility(View.GONE);
                                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                                } else {
                                    folderText.setVisibility(View.VISIBLE);
                                    folderText.setText("Current Folder: " + currentFolder);
                                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                                }

                                SharedPreferences settings = getSharedPreferences("prefs", 0);
                                SharedPreferences.Editor editor = settings.edit();
                                editor.putString("last_folder", currentFolder);
                                editor.commit();



                                final Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        buildList();
                                    }
                                }, 500);


                            }
                        });
                builder.create().show();
                break;
            }
            case R.id.action_new: {
                Account current = new Account(dir + "GameEngineActivity.xml");
                if (current.exists) {

                    Account match = accountDuplicate(current);
                    if (match != null) {
                        if (match.exists) {
                            forceCloseApp();
                            current.file.delete();
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    startApp();
                                }
                            }, 500);
                        } else {
                            shortToast("Current account has not been saved!");
                        }
                    } else {

                        shortToast("Current account has not been saved!");


                    }


                } else {
                    startApp();
                }
                break;
            }
            case R.id.action_new_folder: {
                final EditText folderName = new EditText(MainActivity.this);
                folderName.setSingleLine(true);
                folderName.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void afterTextChanged(Editable s) {
                        if (isValidFolderName(s.toString())) {
                            MainActivity.this.alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        } else {
                            MainActivity.this.alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }
                });
                folderName.setHint("Folder Name");
                InputFilter[] FilterArray = new InputFilter[1];
                FilterArray[0] = new InputFilter.LengthFilter(16);
                folderName.setFilters(FilterArray);
                final AlertDialog.Builder getFolderName = new AlertDialog.Builder(MainActivity.this);
                getFolderName.setTitle("New Folder");
                getFolderName.setView(folderName);
                getFolderName.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
                getFolderName.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {


                        String name = folderName.getText().toString();
                        final String nfPath;
                        if (currentFolder.equals("")) {
                            nfPath = dir + "Accounts/" + name;
                        } else {
                            nfPath = dir + "Accounts/" + currentFolder + "/" + name;
                        }
                        if (!Exists(nfPath)) {
                            new File(nfPath).mkdir();
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    filePermissions(nfPath);
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            shortToast("Folder Created");
                                            buildList();
                                        }
                                    }, 250);
                                }
                            }, 250);
                        } else {
                            shortToast("Name in Use!");
                        }


                    }
                });

                alertDialog = getFolderName.show();
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);


            }
        }


        return super.onOptionsItemSelected(item);
    }

    public void doOneClickReroll() {
        
        Account c = new Account(dir + "GameEngineActivity.xml");
        if (c.exists) {
            
            c.file.delete();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                
                forceCloseApp();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        
                        startApp();
                    }
                }, 800);
            }
        }, 200);


    }

    public void copyFile(File from, File to) {
        debug("Copy File: " + from.toString() + " > " + to.toString());
        if (from.exists() && !to.exists()) {
            
            String[] cmds = {"cp '" + from.toString() + "' '" + to.toString() + "'", "chmod 777 '" + to.toString() + "'"};
            runShell(cmds);
        } else {
            if (from.exists()) {
                debug(to.toString() + "Already Exists");
            } else {
                debug(from.toString() + "Doesn't Exist");
            }

        }
    }

    public boolean mainFiles(Integer FailCount) {
        if (FailCount > 5) {

            
            return false;
        }

        

        if (Exists(dir)) {
            filePermissions(dir);
            
            if (Exists(dir + "GameEngineActivity.xml")) {
                
                filePermissions(dir + "GameEngineActivity.xml");
            }

            if (Exists(dir + "Accounts/")) {
                
                filePermissions(dir + "Accounts/");
                return true;
            } else {
                
                
                String[] cmd = {"mkdir " + dir + "Accounts/"};
                runShell(cmd);
                return mainFiles(FailCount + 1);
            }
        }

        
        return mainFiles(FailCount + 1);

    }





    public void buildFullList() {
        fullaccountlist.clear();
        buildFullList(dir + "Accounts/", 0);
    }

    public void buildFullList(String d, Integer failCount) {

        try {
            if (Exists(d)) {
                File[] files = new File(d).listFiles();
                for (int i = 0; i < files.length; ++i) {

                    if (files[i].isDirectory()) {
                        buildFullList(d + files[i].getName() + "/", 0);
                    }
                    fullaccountlist.add(new Account(files[i]));

                }
            }
        } catch (Exception NullPointerException) {
            if (failCount < 5) {
                filePermissions(d);
                final String dx = d;
                final Integer fx = failCount;
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        buildFullList(dx, fx + 1);
                    }
                }, 100);
            }
        }
    }


    public void buildList() {
        buildFullList();
        errorMessage("");
        ArrayList<Account> items = new ArrayList<Account>();
        AccountAdapter adapter = new AccountAdapter(this, R.layout.account_list, items);
        ListView listView = (ListView) findViewById(R.id.accounts);
        if (!new File(dir).exists()) {
            errorMessage("Unable to find SIF Install for selected server.\n\nSwitch Server in menu.");
            listView.setAdapter(adapter);
            return;
        }
        accountlist.clear();
        if (Exists(dir + "/Accounts")) {
            String p = "";
            if (currentFolder.equals("")) {
                p = dir + "Accounts/";
            } else {
                p = dir + "Accounts/" + currentFolder + "/";
            }

            try {
                if (Exists(p)) {
                    File accdir = new File(p);
                    File[] files = accdir.listFiles();
                    for (int i = 0; i < files.length; ++i) {
                        accountlist.add(new Account(files[i]));
                    }
                    accountlist = sortAccounts(accountlist, sortMethod, reverseSort);
                    String checkHash = "";
                    Account cAcc = new Account(dir + "GameEngineActivity.xml");
                    if (cAcc.exists) {
                        checkHash = cAcc.hash;
                    }
                    for (Account acc : accountlist) {
                        if (!checkHash.equals("")) {
                            if (!acc.isFolder) {
                                if (acc.hash.equals(checkHash)) {
                                    acc.isCurrent = true;
                                }
                            }
                        }
                        items.add(acc);
                    }
                    listView.setAdapter(adapter);

                }
            } catch (Exception e) {
                filePermissions(p);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        buildList();
                    }
                }, 200);

            }


        } else {
            mainFiles(0);
        }


    }

    public ArrayList<Account> sortAccounts(ArrayList<Account> unsorted) {
        return sortAccounts(unsorted, 0, false);
    }

    public ArrayList<Account> sortAccounts(ArrayList<Account> unsorted, Integer method) {
        return sortAccounts(unsorted, method, false);
    }

    public ArrayList<Account> sortAccounts(ArrayList<Account> unsorted, Integer method, Boolean reverse) {
        
        ArrayList<Account> sorted = new ArrayList<>();


        if (unsorted.size() >= 2) {

            Boolean containsFolder = false;
            Integer FnextIndex = null;
            do {
                containsFolder = false;
                FnextIndex = null;
                for (Integer i = 0; i < unsorted.size(); ++i) {
                    if (unsorted.get(i).isFolder) {
                        containsFolder = true;
                        if (FnextIndex == null) {
                            FnextIndex = i;
                            continue;
                        } else {
                            String a = unsorted.get(i).name;
                            String b = unsorted.get(FnextIndex).name;
                            debug(a + ":" + b + " - " + String.valueOf(a.compareTo(b)));
                            if (a.compareTo(b) < 0) {
                                FnextIndex = i;
                            }

                        }

                    }
                }


                if (FnextIndex == null) {
                    containsFolder = false;
                } else {
                    sorted.add(unsorted.get(FnextIndex));
                    unsorted.remove(unsorted.get(FnextIndex));
                }
            } while (containsFolder);


            if (method == 0) {
                while (unsorted.size() > 0) {

                    Integer nextIndex = 0;
                    for (Integer i = 0; i < unsorted.size(); ++i) {
                        if (!i.equals(nextIndex)) {
                            if ((unsorted.get(i).lastModified > unsorted.get(nextIndex).lastModified && reverse == false) || (unsorted.get(i).lastModified < unsorted.get(nextIndex).lastModified && reverse == true)) {
                                nextIndex = i;
                            }
                        }
                    }


                    sorted.add(unsorted.get(nextIndex));
                    unsorted.remove(unsorted.get(nextIndex));
                }
            }
            if (method == 1) { 
                while (unsorted.size() > 0) {
                    Integer nextIndex = 0;

                    for (Integer i = 0; i < unsorted.size(); ++i) {
                        if (!i.equals(nextIndex)) {
                            String a = unsorted.get(i).name.toLowerCase();
                            String b = unsorted.get(nextIndex).name.toLowerCase();
                            if ((a.compareTo(b) < 0 && reverse == false) || (a.compareTo(b) > 0 && reverse == true)) {
                                nextIndex = i;
                            }

                        }


                    }

                    sorted.add(unsorted.get(nextIndex));
                    unsorted.remove(unsorted.get(nextIndex));
                }
            }
            if (method == 2) { 
                while (unsorted.size() > 0) {
                    Integer nextIndex = 0;
                    for (Integer i = 0; i < unsorted.size(); ++i) {
                        Long a = unsorted.get(i).lastLoaded;
                        Long b = unsorted.get(nextIndex).lastLoaded;
                        if ((a > b && reverse == false) || (b > a && reverse == true)) {
                            nextIndex = i;
                        }
                    }
                    sorted.add(unsorted.get(nextIndex));
                    unsorted.remove(unsorted.get(nextIndex));
                }
            }


        } else {
            sorted = unsorted;
        }

        
        return sorted;
    }

    public static void debug(String msg) {
        System.out.println(" >> " + msg);
    }

    public boolean Exists(String path) {
        return new File(path).exists();
    }

    public void longToast(String txt) {
        Toast.makeText(getApplicationContext(), txt, Toast.LENGTH_LONG).show();
    }

    public void shortToast(String txt) {
        Toast.makeText(getApplicationContext(), txt, Toast.LENGTH_SHORT).show();
    }

    public static void filePermissions(String path) {
        File file = new File(path);
        System.out.println("Repairing File Permissions");
        
        String[] cmds = {"chmod 777 '" + file.toString() + "'"};
        runShell(cmds);
    }


    public static void runShell(String[] c) {
        debug("runShell");
        if (shell != null) {
            try {
                Process p = shell;
                DataOutputStream o = new DataOutputStream(p.getOutputStream());
                for (String t : c) {
                    System.out.println(t);
                    o.writeBytes(t + "\n");
                }
                o.flush();
            } catch (Exception IOEXception) {
                debug("!! IO Exception in runShell");
            }
        } else {
            debug("Failed to find Shell");
        }
    }


}

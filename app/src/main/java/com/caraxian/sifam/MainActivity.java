package com.caraxian.sifam;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private static Context mContext;
    private final long SEARCH_DELAY = 100;
    public AlertDialog alertDialog;
    Database db = new Database();
    private Timer searchDelayTimer = new Timer();
    private ListView ACCOUNT_LIST;
    private ArrayList<Account> ACCOUNT_LIST_DATA = new ArrayList<>();
    private AccountListAdapter ACCOUNT_LIST_ADAPTER;
    private int CURRENT_OFFSET = 0;
    private long CURRENT_FOLDER = -1;
    private Account MOVE_ACCOUNT;
    public static ArrayList<Long> selectedAccounts = new ArrayList<>();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        SIFAM.log("MainActivity.java > onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.manu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SIFAM.log("MainActivity.java > onOptionsMenuSelected");
        switch (item.getItemId()) {
            case android.R.id.home: {
                onBackPressed();
                return true;
            }
            case R.id.menu_save: {
                ArrayList<Server> saveServersDyn = new ArrayList<Server>();
                ArrayList<String> saveServerTitles = new ArrayList<String>();
                for (Server s : SIFAM.serverList) {
                    if (s.enabled && s.installed) {
                        saveServersDyn.add(s);
                        saveServerTitles.add(s.name);
                    }
                }
                final ArrayList<Server> saveServers = saveServersDyn;
                if (saveServers.size() != 0) {
                    if (saveServers.size() > 1) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        CharSequence[] items = saveServerTitles.toArray(new CharSequence[saveServerTitles.size()]);
                        builder.setTitle("Save Account")
                                .setItems(items, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        saveAccount(saveServers.get(which));
                                    }
                                });
                        builder.create().show();
                    } else {
                        saveAccount(saveServers.get(0));
                    }
                } else {
                    SIFAM.Toast("No Servers Enabled");
                }
                return true;
            }
            case R.id.menu_search: {
                LinearLayout l = (LinearLayout) findViewById(R.id.view_search);
                EditText e = (EditText) findViewById(R.id.search_editText);
                if (l.getVisibility() == View.VISIBLE) {
                    l.setVisibility(View.GONE);
                    e.setText("");
                } else {
                    l.setVisibility(View.VISIBLE);
                }
                return true;
            }
            case R.id.menu_sort: {
                createSortMenu();
                return true;
            }
            case R.id.menu_options: {
                openSettings(null);
                return true;
            }
            case R.id.menu_refresh: {
                getCurrentPage();
                return true;
            }
            case R.id.menu_newFolder: {
                createNewFolder();
                return true;
            }
            case R.id.menu_import: {
                startActivity(new Intent(this, ImportAccounts.class));
                return true;
            }
            case R.id.menu_export: {
                startActivity(new Intent(this, ExportAccounts.class));
                return true;
            }
            case R.id.menu_new: {
                ArrayList<Server> saveServersDyn = new ArrayList<Server>();
                ArrayList<String> saveServerTitles = new ArrayList<String>();
                for (Server s : SIFAM.serverList) {
                    if (s.enabled && s.installed) {
                        saveServersDyn.add(s);
                        saveServerTitles.add(s.name);
                    }
                }
                final ArrayList<Server> saveServers = saveServersDyn;
                if (saveServers.size() != 0) {
                    if (saveServers.size() > 1) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        CharSequence[] items = saveServerTitles.toArray(new CharSequence[saveServerTitles.size()]);
                        builder.setTitle("Create New Account")
                                .setItems(items, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        createBlankAccount(saveServers.get(which));
                                    }
                                });
                        builder.create().show();
                    } else {
                        createBlankAccount(saveServers.get(0));
                    }
                } else {
                    SIFAM.Toast("No Servers Enabled");
                }
                return true;
            }
            case R.id.menu_deleteCurrent: {
                ArrayList<Server> saveServersDyn = new ArrayList<Server>();
                ArrayList<String> saveServerTitles = new ArrayList<String>();
                for (Server s : SIFAM.serverList) {
                    if (s.enabled && s.installed) {
                        saveServersDyn.add(s);
                        saveServerTitles.add(s.name);
                    }
                }
                final ArrayList<Server> saveServers = saveServersDyn;
                if (saveServers.size() != 0) {
                    if (saveServers.size() > 1) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        CharSequence[] items = saveServerTitles.toArray(new CharSequence[saveServerTitles.size()]);
                        builder.setTitle("Delete GameEngineActivity")
                                .setItems(items, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        deleteCurrentAccount(saveServers.get(which));
                                    }
                                });
                        builder.create().show();
                    } else {
                        createBlankAccount(saveServers.get(0));
                    }
                } else {
                    SIFAM.Toast("No Servers Enabled");
                }
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    public void deleteCurrentAccount(final Server server) {
        SIFAM.log("MainActivity > deleteCurrentAccount(server) " + server.name);
        SIFAM.lastLoadedAccountName = "New Account";
        server.updateFromGameEngineActivity();
        if (server.error == false) {
            if (server.currentUser.equals("")) {
                SIFAM.Toast("No Data to Delete");
            } else {
                final Runnable doDelete = new Runnable() {
                    @Override
                    public void run() {
                        server.deleteGameEngineActivity();
                        SIFAM.delayAction(new Runnable() {
                            @Override
                            public void run() {
                                getCurrentPage();
                            }
                        }, 1000);
                    }
                };
                final Runnable deleteConfirm = new Runnable() {
                    @Override
                    public void run() {
                        final AlertDialog.Builder confirmDelete = new AlertDialog.Builder(mContext);
                        confirmDelete.setTitle("Delete Account");
                        confirmDelete.setMessage("Do you really want to delete the GameEngineActivity file?\nIt doesn't appear to be saved.");
                        confirmDelete.setIcon(android.R.drawable.ic_dialog_alert);
                        confirmDelete.setPositiveButton("Delete It!", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                confirmDelete.setMessage("Absolutely Sure? Delete unsaved GameEngineActivity?");
                                confirmDelete.setPositiveButton("Keep It", null);
                                confirmDelete.setNegativeButton("Delete It!", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        doDelete.run();
                                    }
                                });
                                confirmDelete.show();
                            }
                        });
                        confirmDelete.setNegativeButton("Keep It", null);
                        confirmDelete.show();
                    }
                };
                Account currentUser = db.findAccountByUser(server.currentUser, server.code);
                if (currentUser == null && SIFAM.NO_WARNINGS == false) {
                    deleteConfirm.run();
                } else {
                    if (SIFAM.NO_DELETE_WARNINGS || currentUser.userPass.equals(server.currentPass)) {
                        doDelete.run();
                    } else {
                        deleteConfirm.run();
                    }
                }
            }
        } else {
            SIFAM.Toast("Error");
        }
    }

    public void createBlankAccount(final Server server) {
        SIFAM.log("createBlankAccount(server)" + server.name);
        SIFAM.lastLoadedAccountName = "New Account";
        server.updateFromGameEngineActivity();
        if (server.error == false) {
            SIFAM.log("Good So Far");
            if (server.currentUser.equals("")) {
                server.forceCloseApp();
                SIFAM.Toast("Starting new account.");
                SIFAM.delayAction(new Runnable() {
                    public void run() {
                        server.openApp();
                    }
                }, 1500);
            } else {
                Account currentUser = db.findAccountByUser(server.currentUser, server.code);
                if (currentUser == null && SIFAM.NO_WARNINGS == false) {
                    SIFAM.log("Not Saved!");
                    SIFAM.Toast("Current Account not Saved!");
                } else {
                    if (SIFAM.NO_WARNINGS == true || currentUser.userPass.equals(server.currentPass)) {
                        SIFAM.log("UserPass Match");
                        server.forceCloseApp();
                        if (server.writeToGameEngineActivity(0, "", "")) {
                            SIFAM.Toast("Starting new account.");
                            SIFAM.delayAction(new Runnable() {
                                public void run() {
                                    server.openApp();
                                }
                            }, 1500);
                        } else {
                            SIFAM.log("Failed to write");
                            SIFAM.Toast("Failed");
                        }
                    } else {
                        SIFAM.Toast("Current Account not Saved!");
                    }
                }
            }
        } else {
            SIFAM.log("Error verifying current data");

        }
    }

    private void createSortMenu() {
        AlertDialog.Builder sortMenu = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.sort_menu, null);
        final Spinner spinner = (Spinner) dialogView.findViewById(R.id.sort_bySpinner);
        final Switch reverseSwitch = (Switch) dialogView.findViewById(R.id.sort_reverseSwitch);
        String[] spinnerArray = new String[]{"Account Name", "Time Loaded", "Save Order"};
        final String[] sortValues = new String[]{Database.Accounts.cName, Database.Accounts.cUsed, Database.Accounts._ID};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerArray);
        spinner.setAdapter(adapter);
        for (int i = 0; i < sortValues.length; i++) {
            if (SIFAM.SORT_BY.equals(sortValues[i])) {
                spinner.setSelection(i);
                break;
            }
        }
        reverseSwitch.setChecked(SIFAM.REVERSE_SORT);
        sortMenu.setView(dialogView);
        sortMenu.setTitle("Sort Settings");
        sortMenu.setNeutralButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int selected = (int) spinner.getSelectedItemId();
                boolean reverse = reverseSwitch.isChecked();
                SIFAM.sharedPreferences.edit().putString("sort_by", sortValues[selected]).putBoolean("reverse_sort", reverse).commit();
                SIFAM.updateSettings();
                CURRENT_OFFSET = 0;
                getCurrentPage();
            }
        });
        sortMenu.create().show();
    }

    private void saveAccount(final Server server, String name) {
        SIFAM.log("MainActivity.java > saveAccount(server,name)");
        if (name.length() == 0) {
            if (SIFAM.ALT_QS_NAME == false) {
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy MM dd 'at' HH:mm:ss");
                name = sdf.format(cal.getTime());
            } else {
                int nameInt = db.countAccounts(CURRENT_FOLDER, "", false) + 1;
                while (db.accountExistsIn("" + nameInt, CURRENT_FOLDER)) {
                    nameInt++;
                }
                name = "" + nameInt;
            }
        }
        db.saveNewAccount(name, server.currentUser, server.currentPass, server.code, CURRENT_FOLDER);
        getCurrentPage();
        Toast.makeText(SIFAM.getContext(), "Saved as '" + name + "'", Toast.LENGTH_SHORT).show();
    }

    private void createNewFolder() {
        SIFAM.log("MainActivity.java > createNewFolder");
        final EditText nameInput = new EditText(this);
        nameInput.setSingleLine(true);
        nameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                MainActivity.this.alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                MainActivity.this.alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(isSaveNameValid(s.toString()));
                if (db.folderExistsIn(s.toString(), CURRENT_FOLDER)) {
                    MainActivity.this.alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
            }
        });
        nameInput.setHint("New Folder Name");
        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(64);
        nameInput.setFilters(FilterArray);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create Folder").setView(nameInput).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        }).setPositiveButton("Create", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String name = nameInput.getText().toString();
                db.createFolder(name, CURRENT_FOLDER);
                getCurrentPage();
            }
        });
        alertDialog = builder.show();
        MainActivity.this.alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

    private void saveAccount(final Server server) {
        server.updateFromGameEngineActivity();
        SIFAM.log("MainActivity.java > saveAccount(server)");
        if (server.currentUser == null || server.currentUser.length() == 0) {
            SIFAM.Toast("No data to save.");
            return;
        }
        final Account currentAccount = db.findAccountByUser(server.currentUser, server.code);
        if (currentAccount == null || SIFAM.ALLOW_DUPLICATE_SAVES) {
            if (SIFAM.QUICK_SAVE == true) {
                saveAccount(server, "");
            } else {
                final EditText nameInput = new EditText(this);
                nameInput.setSingleLine(true);
                nameInput.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        MainActivity.this.alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(isSaveNameValid(s.toString()));
                        if (s.length() == 0) {
                            MainActivity.this.alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        }
                    }
                });
                nameInput.setHint("Account Name");
                InputFilter[] FilterArray = new InputFilter[1];
                FilterArray[0] = new InputFilter.LengthFilter(64);
                nameInput.setFilters(FilterArray);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Save As").
                        setView(nameInput).
                        setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        }).
                        setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        String name = nameInput.getText().toString();
                                        saveAccount(server, name);
                                    }
                                }

                        );
                alertDialog = builder.show();
            }
        } else {
            if (currentAccount.userPass.equals(server.currentPass)) {
                Toast.makeText(SIFAM.getContext(), "Account already saved", Toast.LENGTH_SHORT).show();
            } else {
                AlertDialog.Builder message = new AlertDialog.Builder(this);
                message.setTitle("Account Modified");
                message.setMessage("This account is already saved, but has a different token. This happens after transferring the account using transfer passcode from in game.\n\nDo you wish to update the stored token?");
                message.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db.updatePassCode(currentAccount, server.currentPass);
                    }
                });
                message.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                message.show();
            }
        }
    }


    public boolean isSaveNameValid(String s) {
        SIFAM.log("MainActivity.java > isSaveNameValid");
        if (s.length() == 0) return false;
        if (s.length() < 1) return false;
        if (s.length() > 64) return false;
        return true;
    }

    protected void onCreate(Bundle savedInstanceState) {
        SIFAM.log("MainActivity.java > onCreate");
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        setContentView(R.layout.activity_main);
        ACCOUNT_LIST = (ListView) findViewById(R.id.accountList);
        ACCOUNT_LIST_DATA = new ArrayList<>();
        ACCOUNT_LIST_ADAPTER = new AccountListAdapter(MainActivity.this, R.layout.account_list, ACCOUNT_LIST_DATA);
        ACCOUNT_LIST.setAdapter(ACCOUNT_LIST_ADAPTER);
        EditText searchEditText = (EditText) findViewById(R.id.search_editText);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchDelayTimer != null) {
                    searchDelayTimer.cancel();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                searchDelayTimer = new Timer();
                searchDelayTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getCurrentPage();
                            }
                        });
                    }
                }, SEARCH_DELAY);
            }
        });
        ACCOUNT_LIST.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (selectedAccounts.size() == 0 || ACCOUNT_LIST_DATA.get(position).isFolder) {
                    startLoadAccount(ACCOUNT_LIST_DATA.get(position));
                } else {
                    if (ACCOUNT_LIST_DATA.get(position).locked) {
                        SIFAM.Toast("Cannot select locked account.");
                    } else {
                        toggleSelect(ACCOUNT_LIST_DATA.get(position));
                    }
                }
            }
        });
        ACCOUNT_LIST.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return false;
            }
        });
        Switch allFoldersSwitch = (Switch) findViewById(R.id.allFolders_Switch);
        allFoldersSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                getCurrentPage();
            }
        });
        registerForContextMenu(ACCOUNT_LIST);
    }

    public void openSettings(View v) {
        startActivity(new Intent(this, AppPreferences.class));
    }

    public void moveSelected(View v) {
        for (Long i : selectedAccounts) {
            db.moveAccount(i, CURRENT_FOLDER);
        }
        getCurrentPage();
    }

    public void toggleSelect(Account account) {
        abortMove(null);
        if (selectedAccounts.contains(account.id)) {
            selectedAccounts.remove(account.id);
        } else {
            selectedAccounts.add(account.id);
        }
        ACCOUNT_LIST_ADAPTER.notifyDataSetChanged();
        if (selectedAccounts.size() == 0) {
            clearSelection(null);
        } else {
            findViewById(R.id.sifamVersion).setVisibility(View.GONE);
            findViewById(R.id.bottomBar).setVisibility(View.VISIBLE);
            findViewById(R.id.bulk_move).setVisibility(View.VISIBLE);
            findViewById(R.id.bulk_clear).setVisibility(View.VISIBLE);
            TextView t = (TextView) findViewById(R.id.bulk_count);
            t.setVisibility(View.VISIBLE);
            t.setText("Selected Accounts: " + selectedAccounts.size());
        }
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        SIFAM.log("MainActivity.java > onCreateContextMenu");
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        Account contextAccount = ACCOUNT_LIST_DATA.get(info.position);
        if (contextAccount.isFolder) {
            menu.setHeaderTitle(contextAccount.name);
            super.onCreateContextMenu(menu, v, menuInfo);
            getMenuInflater().inflate(R.menu.menu_context_folder, menu);
            menu.setHeaderIcon(R.drawable.ic_folder_black_24dp);
        } else {
            menu.setHeaderTitle(contextAccount.name);
            super.onCreateContextMenu(menu, v, menuInfo);
            if (contextAccount.locked) {
                getMenuInflater().inflate(R.menu.menu_context_account_locked, menu);
                menu.setHeaderIcon(R.drawable.ic_lock_outline_black_24dp);
            } else {
                getMenuInflater().inflate(R.menu.menu_context_account, menu);
            }
        }
    }

    public boolean onContextItemSelected(MenuItem item) {
        SIFAM.log("MainActivity.java > onContextItemSelected");
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final Account contextAccount = ACCOUNT_LIST_DATA.get(info.position);
        switch (item.getItemId()) {
            case R.id.context_rename: {
                renameAccount(contextAccount);
                break;
            }
            case R.id.context_move: {
                startMove(contextAccount);
                break;
            }
            case R.id.context_delete: {
                deleteAccount(contextAccount);
                break;
            }
            case R.id.context_lock: {
                db.setLock(contextAccount, true);
                getCurrentPage();
                break;
            }
            case R.id.context_unlock: {
                db.setLock(contextAccount, false);
                getCurrentPage();
                break;
            }
            case R.id.context_select: {
                toggleSelect(contextAccount);
                break;
            }
            case R.id.context_delete_folder: {
                deleteFolder(contextAccount);
                break;
            }
            case R.id.context_move_folder: {
                startMove(contextAccount);
                break;
            }
            case R.id.context_rename_folder: {
                renameFolder(contextAccount);
                break;
            }
        }
        return true;
    }

    public void renameAccount(final Account account) {
        SIFAM.log("MainActivity.java > renameAccount(account)");
        final EditText nameInput = new EditText(this);
        nameInput.setSingleLine(true);
        nameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                MainActivity.this.alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(isSaveNameValid(s.toString()));
            }
        });
        nameInput.setHint("New Account Name");
        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(64);
        nameInput.setFilters(FilterArray);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename Account").setMessage("Enter new name for '" + account.name + "' on " + account.server)
                .setView(nameInput).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        }).setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String name = nameInput.getText().toString();
                if (db.renameAccount(account, name) >= 1) {
                    SIFAM.Toast("Renamed Account");
                    getCurrentPage();
                } else {
                    SIFAM.Toast("Rename Failed");
                }
            }
        });
        alertDialog = builder.show();
    }

    public void renameFolder(final Account folder) {
        SIFAM.log("MainActivity.java > renameFolder(folder)");
        final EditText nameInput = new EditText(this);
        nameInput.setSingleLine(true);
        nameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                MainActivity.this.alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                MainActivity.this.alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(isSaveNameValid(s.toString()));
                if (db.folderExistsIn(s.toString(), CURRENT_FOLDER)) {
                    MainActivity.this.alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
            }
        });
        nameInput.setHint("New Folder Name");
        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(64);
        nameInput.setFilters(FilterArray);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename Folder").setMessage("Enter new name for '" + folder.name + "'")
                .setView(nameInput).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        }).setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String name = nameInput.getText().toString();
                if (db.renameFolder(folder, name) >= 1) {
                    SIFAM.Toast("Renamed Folder");
                    getCurrentPage();
                } else {
                    SIFAM.Toast("Rename Failed");
                }
            }
        });
        alertDialog = builder.show();
    }

    public void startMove(final Account account) {
        SIFAM.log("MainActivity.java > startMove(account)");
        MOVE_ACCOUNT = account;
        clearSelection(null);
        findViewById(R.id.moveHere_Button).setVisibility(View.VISIBLE);
        findViewById(R.id.cancelMove_Button).setVisibility(View.VISIBLE);
        findViewById(R.id.sifamVersion).setVisibility(View.GONE);
        findViewById(R.id.bottomBar).setVisibility(View.VISIBLE);
    }

    public void clearSelection(View v) {
        selectedAccounts.clear();
        ACCOUNT_LIST_ADAPTER.notifyDataSetChanged();
        findViewById(R.id.bottomBar).setVisibility(View.GONE);
        findViewById(R.id.moveHere_Button).setVisibility(View.GONE);
        findViewById(R.id.bulk_clear).setVisibility(View.GONE);
        findViewById(R.id.bulk_count).setVisibility(View.GONE);
    }

    public void abortMove(View v) {
        SIFAM.log("MainActivity.java > abortMove(v)");
        MOVE_ACCOUNT = null;
        findViewById(R.id.moveHere_Button).setVisibility(View.GONE);
        ;
        findViewById(R.id.cancelMove_Button).setVisibility(View.GONE);
        ;
        findViewById(R.id.sifamVersion).setVisibility(View.VISIBLE);
        findViewById(R.id.bottomBar).setVisibility(View.GONE);
    }

    public void confirmMove(View v) {
        SIFAM.log("MainActivity.java > confirmView(v)");
        if (MOVE_ACCOUNT.isFolder) {
            long lastID = CURRENT_FOLDER;
            ArrayList<Long> folderPath = new ArrayList<>();
            boolean failed = false;
            folderPath.add(CURRENT_FOLDER);
            if (lastID == MOVE_ACCOUNT.id) {
                failed = true;
            }
            while (lastID != -1 && failed == false) {
                lastID = db.getFolderParent(lastID);
                folderPath.add(lastID);
                if (lastID == MOVE_ACCOUNT.id) {
                    failed = true;
                }
            }
            if (failed == false) {
                db.moveFolder(MOVE_ACCOUNT, CURRENT_FOLDER);

            } else {
                SIFAM.Toast("Can not put folder into itself");
            }
        } else {
            db.moveAccount(MOVE_ACCOUNT, CURRENT_FOLDER);
        }
        abortMove(null);
        getCurrentPage();
    }

    public void deleteAccount(final Account account) {
        SIFAM.log("MainActivity.java > deleteAccount(account)");
        if (account.locked == false) {
            if (SIFAM.NO_DELETE_WARNINGS && SIFAM.NO_WARNINGS) {
                db.deleteAccount(account);
                getCurrentPage();
            } else {
                final AlertDialog.Builder confirmDelete = new AlertDialog.Builder(this);
                confirmDelete.setTitle("Delete Account");
                confirmDelete.setMessage("Do you really want to delete '" + account.name + "' on " + account.server + "?");
                confirmDelete.setIcon(android.R.drawable.ic_dialog_alert);
                confirmDelete.setPositiveButton("Delete It!", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        confirmDelete.setMessage("Absolutely Sure? Delete '" + account.name + "' on " + account.server + "?");
                        confirmDelete.setPositiveButton("Keep It", null);
                        confirmDelete.setNegativeButton("Delete It!", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                db.deleteAccount(account);
                                getCurrentPage();
                            }
                        });
                        confirmDelete.show();
                    }
                });
                confirmDelete.setNegativeButton("Keep It", null);
                confirmDelete.show();
            }
        } else {
            SIFAM.Toast("Can not delete a locked account");
        }
    }

    public void deleteFolder(final Account folder) {
        SIFAM.log("MainActivity.java > deleteFolder(folder)");
        ArrayList<Account> foldersInFolder = db.getFolders(folder.id);
        if (foldersInFolder.size() == 0) {
            final int count = db.countAccounts(folder.id, "", false);
            if (count == 0) {
                final AlertDialog.Builder confirmDelete = new AlertDialog.Builder(this);
                confirmDelete.setTitle("Delete Account");
                confirmDelete.setMessage("Do you really want to delete '" + folder.name + "'\n\nIt is empty.");
                confirmDelete.setIcon(android.R.drawable.ic_dialog_alert);
                confirmDelete.setPositiveButton("Delete It!", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        db.deleteFolder(folder);
                        getCurrentPage();
                    }
                });
                confirmDelete.setNegativeButton("Keep It", null);
                confirmDelete.show();
            } else {
                boolean hasLockedAccount = false;
                final ArrayList<Account> accountsToDelete = db.getAccounts(folder.id, count, 0, "name", false, "", false);
                for (Account a : accountsToDelete) {
                    if (a.locked == true) {
                        hasLockedAccount = true;
                    }
                }
                if (hasLockedAccount == false) {
                    final String confirmString = "delete " + count + " accounts";
                    final EditText confirmInput = new EditText(this);
                    confirmInput.setSingleLine(true);
                    confirmInput.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            MainActivity.this.alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            MainActivity.this.alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(s.toString().equals(confirmString));
                        }
                    });
                    confirmInput.setHint("Confirm Text");
                    InputFilter[] FilterArray = new InputFilter[1];
                    FilterArray[0] = new InputFilter.LengthFilter(64);
                    confirmInput.setFilters(FilterArray);
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Delete Folder with Accounts").setMessage("The Folder '" + folder.name + "' has " + count
                            + " accounts in it.\nTo prevent accidental deletion you must confirm your intent to delete it."
                            + " \nPlease enter the following into the text box (without quotes)\n\n\"delete "
                            + count + " accounts\"")
                            .setView(confirmInput).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    }).setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (confirmInput.getText().toString().equals(confirmString)) {
                                for (Account a : accountsToDelete) {
                                    db.deleteAccount(a);
                                }
                                if (db.countAccounts(folder.id, "", false) == 0) {
                                    db.deleteFolder(folder);
                                    getCurrentPage();
                                    SIFAM.Toast("Deleted Folder");
                                } else {
                                    SIFAM.Toast("Failed to delete all accounts in folder");
                                    getCurrentPage();
                                }
                            }
                        }
                    });
                    alertDialog = builder.show();
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    SIFAM.Toast("Can not delete folder with locked account!");
                }
            }
        } else {
            SIFAM.Toast("Can not delete folder with sub folders.");
        }
    }

    @Override
    public void onBackPressed() {
        SIFAM.log("MainActivity.java > onBackPressed");
        if (CURRENT_FOLDER == -1) {
            finish();
        } else {
            changeFolder(db.getFolderParent(CURRENT_FOLDER));
        }
    }

    public void changeFolder(long id) {
        SIFAM.log("MainActivity.java > changeFolder");
        CURRENT_FOLDER = id;
        SIFAM.sharedPreferences.edit().putLong("CURRENT_FOLDER", CURRENT_FOLDER).commit();
        CURRENT_OFFSET = 0;
        TextView currentFolder_textView = (TextView) findViewById(R.id.folder_textView);
        if (CURRENT_FOLDER == -1) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            currentFolder_textView.setVisibility(View.GONE);
        } else {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            currentFolder_textView.setVisibility(View.VISIBLE);
            currentFolder_textView.setText("Folder:   " + getPathToFolder(CURRENT_FOLDER));
        }
        LinearLayout searchBar = (LinearLayout) findViewById(R.id.view_search);
        searchBar.setVisibility(View.GONE);
        getCurrentPage();
    }

    public String getPathToFolder(long folder) {
        SIFAM.log("MainActivity.java > getPathToFolder(folder)");
        ArrayList<Long> idPath = new ArrayList<>();
        String pathString = "";
        while (folder != -1) {
            if (!idPath.contains(folder)) {
                idPath.add(folder);
                folder = db.getFolderParent(folder);
            } else {
                folder = -1;
                return "Invalid Folder Path";
            }
        }
        for (int j = idPath.size() - 1; j >= 0; j--) {
            pathString += "  >  " + db.getFolderName(idPath.get(j));
        }
        pathString = pathString.replaceFirst("  >  ", "");
        return pathString;
    }

    public void startLoadAccount(final Account account) {
        SIFAM.log("MainActivity.java > startLoadAccount > " + account.id + "(" + account.name + ")");
        SIFAM.lastLoadedAccountName = getPathToFolder(CURRENT_FOLDER) + " > " + account.name;
        if (account.isFolder) {
            changeFolder(account.id);
        } else {
            for (final Server s : SIFAM.serverList) {
                if (s.code.equals(account.server)) {
                    Account currentAccount = db.findAccountByUser(s.currentUser, s.code);
                    final Runnable doLoad = new Runnable() {
                        @Override
                        public void run() {
                            boolean successfulLoad = s.writeToGameEngineActivity(0, account.userKey, account.userPass);
                            if (successfulLoad) {


                                db.updateAccessTime(account);
                                if (SIFAM.AUTO_START) {
                                    SIFAM.delayAction(new Runnable() {
                                        public void run() {
                                            s.openApp();
                                        }
                                    }, 1000);
                                } else {
                                    getCurrentPage();
                                }
                            } else {
                                SIFAM.Toast("Failed to load account!");
                            }
                        }
                    };
                    if (currentAccount != null || SIFAM.NO_WARNINGS || s.currentUser.length() == 0) {
                        s.forceCloseApp();
                        SIFAM.delayAction(doLoad, 500);
                    } else {
                        SIFAM.Toast("Current account not saved!");

                    }
                    break;
                }
            }
        }
    }


    @Override
    public void onResume() {
        SIFAM.log("MainActivity.java > onResume");
        super.onResume();
        SIFAM.updateServerInfos();
        SIFAM.updateSettings();
        LinearLayout l = (LinearLayout) findViewById(R.id.view_search);
        EditText e = (EditText) findViewById(R.id.search_editText);
        l.setVisibility(View.GONE);
        e.setText("");
        abortMove(null);
        clearSelection(null);
        changeFolder(SIFAM.sharedPreferences.getLong("CURRENT_FOLDER", -1));
        getCurrentPage();
        stopService(new Intent(SIFAM.getContext(), OverlayService.class));
    }

    public void getCurrentPage() {
        LinearLayout noServersError = (LinearLayout) findViewById(R.id.errorMessage_noEnabledServers);
        noServersError.setVisibility(View.GONE);
        SIFAM.log("MainActivity.java > getCurrentPage");
        TextView searchBox = (TextView) findViewById(R.id.search_editText);
        Switch searchAll = (Switch) findViewById(R.id.allFolders_Switch);
        ImageButton nextPButton = (ImageButton) findViewById(R.id.nextPage_Button);
        ImageButton prevPButton = (ImageButton) findViewById(R.id.prevPage_Button);
        TextView pageInfo = (TextView) findViewById(R.id.pageInfo_textView);
        int accountTotal = db.countAccounts(CURRENT_FOLDER, searchBox.getText().toString(), searchAll.isChecked());
        int pageTotal = (accountTotal - 1) / SIFAM.MAX_DISPLAY + 1;
        int displayTop = (1 + CURRENT_OFFSET + SIFAM.MAX_DISPLAY);
        if (displayTop >= accountTotal + 1) {
            displayTop = accountTotal + 1;
            nextPButton.setEnabled(false);
        } else {
            nextPButton.setEnabled(true);
        }
        if (CURRENT_OFFSET <= 0) {
            CURRENT_OFFSET = 0;
            prevPButton.setEnabled(false);
        } else {
            prevPButton.setEnabled(true);
        }
        pageInfo.setText("Page " + ((CURRENT_OFFSET / SIFAM.MAX_DISPLAY) + 1) + " of " + pageTotal + "\n" + (1 + CURRENT_OFFSET) + "- " + (displayTop - 1) + " of " + accountTotal);

        ACCOUNT_LIST_DATA = db.getAccounts(CURRENT_FOLDER, SIFAM.MAX_DISPLAY, CURRENT_OFFSET, SIFAM.SORT_BY, SIFAM.REVERSE_SORT, searchBox.getText().toString(), searchAll.isChecked());
        if (ACCOUNT_LIST_DATA == null) {
            noServersError.setVisibility(View.VISIBLE);
            if (CURRENT_FOLDER != -1) {
                changeFolder(-1);
            }
        } else {
            ACCOUNT_LIST_ADAPTER = new AccountListAdapter(MainActivity.this, R.layout.account_list, ACCOUNT_LIST_DATA);
            ACCOUNT_LIST.setAdapter(ACCOUNT_LIST_ADAPTER);
        }
    }

    public void getNextPage(View v) {
        SIFAM.log("MainActivity.java > getNextPage");
        CURRENT_OFFSET += SIFAM.MAX_DISPLAY;
        getCurrentPage();
    }

    public void getPrevPage(View v) {
        SIFAM.log("MainActivity.java > getPrevPage");
        CURRENT_OFFSET -= SIFAM.MAX_DISPLAY;
        getCurrentPage();
    }

}

package com.caraxian.sifam;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class SIFAM extends Application {
    public static boolean skipRootCheck = false;
    public static Integer MAX_DISPLAY = 200000;
    public static String SORT_BY = "name";
    public static boolean REVERSE_SORT = false;
    public static boolean ALLOW_DUPLICATE_SAVES = false;
    public static boolean QUICK_SAVE = false;
    public static boolean NO_WARNINGS = false;
    public static boolean AUTO_START = true;
    public static boolean FOLDERS_ON_BOTTOM = false;
    public static SharedPreferences sharedPreferences;
    public static String errorMessage = "";
    public static String fullLog = "";
    public static List<Server> serverList = new ArrayList<Server>();
    private static Context context;
    static private Process shell = getShell();

    public static Context getContext() {
        return context;
    }

    public static void deviceInformation() {
        log("Bluestacks: " + new File("/data/.bluestacks.prop").exists());
        log("Device Manufacturer: " + Build.MANUFACTURER);
        log("Device: " + Build.MODEL);
    }

    public static void log(String... log) {
        for (String logText : log) {
            Log.w("SIFAM", logText);
            try {
                logText += "\n";
                fullLog += logText;
                FileOutputStream fos = context.openFileOutput("SIFAM.log", Context.MODE_APPEND);
                fos.write(logText.getBytes());
                fos.close();
            } catch (Exception e) {
                Log.w("SIFAM", e);
            }
        }
    }

    public static void log(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        log(sw.toString());
    }

    public static void log(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        log(sw.toString());
    }

    public static void updateServerInfos() {
        for (Server s : serverList) {
            s.updateEnabled();
            s.updateInstalled();
            s.updateFromGameEngineActivity();
        }
    }

    private static boolean canExecuteCommand(String command) {
        boolean executedSuccesfully;
        try {
            Runtime.getRuntime().exec(command);
            executedSuccesfully = true;
        } catch (Exception e) {
            executedSuccesfully = false;
        }
        return executedSuccesfully;
    }

    public static void updateSettings() {
        skipRootCheck = sharedPreferences.getBoolean("skipRootCheck", false);
        QUICK_SAVE = sharedPreferences.getBoolean("quick_save", false);
        AUTO_START = sharedPreferences.getBoolean("auto_start", true);
        FOLDERS_ON_BOTTOM = sharedPreferences.getBoolean("folders_on_bottom", false);
        SORT_BY = sharedPreferences.getString("sort_by", "name");
        REVERSE_SORT = sharedPreferences.getBoolean("reverse_sort", false);
        NO_WARNINGS = sharedPreferences.getBoolean("no_warnings", false);
    }

    public static void executeRootCommand(String... command) {
        if (shell == null) {
            SIFAM.log("No Shell");
        } else {
            try {
                Process p = shell;
                DataOutputStream o = new DataOutputStream(p.getOutputStream());
                for (String c : command) {
                    o.writeBytes(c + "\n");
                }
                o.flush();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private static Process getShell() {
        try {
            Process shell = Runtime.getRuntime().exec("sh");
            DataOutputStream o = new DataOutputStream(shell.getOutputStream());
            o.writeBytes("su\n");
            o.flush();
            return shell;
        } catch (Exception ex) {
            SIFAM.log(ex);
            return null;
        }
    }

    public static String exceptionToString(Exception ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    public static void forcePermission(File file) {
        if (file == null) {
            return;
        }
        SIFAM.executeRootCommand("chmod 777 " + file.getParentFile().getAbsolutePath());
        SIFAM.executeRootCommand("chmod 777 " + file.getAbsolutePath());
    }

    public static void Toast(String text) {
        Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
    }

    public static void delayAction(Runnable action, int delay) {
        final Handler handle = new Handler();
        handle.postDelayed(action, delay);
    }

    @Override
    public void onCreate() {
        log("SIFAM:onCreate");
        final Thread.UncaughtExceptionHandler androidErrorHandle = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread paramThread, Throwable paramThrowable) {
                SIFAM.log(paramThrowable);
                SIFAM.errorMessage = paramThrowable.getMessage();
                Intent i = new Intent(getContext(), ErrorDetected.class);
                startActivity(i);
                if (androidErrorHandle != null) {
                    androidErrorHandle.uncaughtException(paramThread, paramThrowable);
                } else {
                    System.exit(2);
                }
            }
        });
        super.onCreate();
        context = getApplicationContext();
        SIFAM.log("Initiating SIFAM " + getResources().getString(R.string.version) + " (" + getResources().getString(R.string.build) + ")");
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        deviceInformation();
        serverList.add(new Server("EN", "English", "klb.android.lovelive_en"));
        serverList.add(new Server("JP", "Japan", "klb.android.lovelive"));
        serverList.add(new Server("TW", "Taiwan", "net.gamon.loveliveTW"));
        serverList.add(new Server("KR", "Korea", "com.nhnent.SKLOVELIVE"));
        serverList.add(new Server("CN", "China", "klb.android.lovelivecn"));
        updateSettings();
    }
}
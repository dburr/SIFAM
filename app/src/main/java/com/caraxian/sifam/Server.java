package com.caraxian.sifam;

import android.content.Intent;
import android.content.pm.PackageManager;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class Server {
    public String code;
    public String name;
    public String className;
    public boolean enabled = false;
    public boolean installed = false;
    public int failCheckCount = 0;
    public String currentUser;
    public String currentPass;
    public String assetKey;
    public File gameEngineActivity;
    public boolean error = false;

    public static Server getServer(String code){
        for (Server s : SIFAM.serverList){
            if (s.code.equals(code)){
                return s;
            }
        }
        return null;
    }
    
    public Server(String nCode, String nName, String nClassName) {
        code = nCode;
        name = nName;
        className = nClassName;
        gameEngineActivity = new File("/data/data/" + className + "/shared_prefs/GameEngineActivity.xml");
        updateEnabled();
        updateInstalled();
        updateFromGameEngineActivity();
    }

    public void updateEnabled() {
        enabled = SIFAM.sharedPreferences.getBoolean(code, false);
    }

    public void updateInstalled() {
        PackageManager pm = SIFAM.getContext().getPackageManager();
        try {
            pm.getPackageInfo(className, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }

    }

    public void deleteGameEngineActivity(){
        if (gameEngineActivity.exists()){
            gameEngineActivity.delete();
            updateFromGameEngineActivity();
            SIFAM.delayAction(new Runnable() {
                @Override
                public void run() {
                    if (currentUser.equals("")){
                        SIFAM.Toast(code + " GameEngineActivity Deleted");
                    }
                }
            },500);
        }
    }

    public void updateFromGameEngineActivity() {
        updateFromGameEngineActivity(1);
    }

    public void updateFromGameEngineActivity(int attempt) {
        error = false;
        if (attempt > 2) {
            failCheckCount++;
            if (failCheckCount > 5) {

                SIFAM.log(code + " > Failed to read GameEngineActivity > Abort");
                SIFAM.Toast("Failed to access '" + code + "' GameEngineActivity.\nDid you allow SIFAM root access?");
                return;
            }
            SIFAM.delayAction(new Runnable() {
                @Override
                public void run() {
                    updateFromGameEngineActivity();
                }
            }, 200*failCheckCount);
            error = true;
            return;
        }
        if (installed == true) {
            if (gameEngineActivity.exists()) {
                try {
                    String text = new Scanner(gameEngineActivity, "UTF-8").useDelimiter("\\A").next();
                    String s1[] = text.split("<string name=\"\\[LOVELIVE_ID\\]user_id\">");
                    if (s1.length > 1) {
                        String s2[] = s1[1].split("</string>");
                        currentUser = s2[0];
                    } else {
                        currentUser = "";
                    }
                    String s3[] = text.split("<string name=\"\\[LOVELIVE_PW\\]passwd\">");
                    if (s3.length > 1) {
                        String s4[] = s3[1].split("</string>");
                        currentPass = s4[0];
                    } else {
                        currentPass = "";
                    }
                    String s5[] = text.split("<string name=\"\\[assets\\]version\">");
                    if (s5.length > 1) {
                        String s6[] = s5[1].split("</string>");
                        assetKey = s6[0];
                    } else {
                        assetKey = "";
                    }
                    SIFAM.log(code + " > Updated from GameEngineActivity");
                    failCheckCount = 0;
                } catch (Exception ex) {
                    SIFAM.log(ex);
                    SIFAM.log(code + " > Failed to Read GameEngineActivity - Attempting Fix");
                    SIFAM.forcePermission(gameEngineActivity);
                    SIFAM.forcePermission(gameEngineActivity.getParentFile());
                    updateFromGameEngineActivity(attempt + 1);
                }
            } else {
                SIFAM.log(code + " > No GameEngineActivity exists");
                currentUser = "";
                currentPass = "";
                assetKey = "";
            }
        } else {
            currentUser = "";
            currentPass = "";
        }
    }

    public boolean writeToGameEngineActivity(int attempt, String user, String pass) {
        if (attempt > 5) {
            SIFAM.Toast("Failed to write to '" + code + "' GameEngineActivity.\nHave you allowed SIFAM to have root access?");
            return false;
        }
        if (installed == true) {
            ArrayList<String> lines = new ArrayList<String>();
            lines.add("<?xml version='1.0' encoding='utf-8' standalone='yes' ?>");
            lines.add("<map>");
            lines.add("    <string name=\"[assets]version\">" + assetKey + "</string>");
            lines.add("    <string name=\"[LOVELIVE_ID]user_id\">" + user + "</string>");
            lines.add("    <string name=\"[LOVELIVE_PW]passwd\">" + pass + "</string>");
            lines.add("</map>");
            try {
                if (gameEngineActivity.exists()) {
                    FileWriter writer = new FileWriter(gameEngineActivity.getAbsolutePath());
                    for (String l : lines) {
                        writer.write(l + "\n");
                    }
                    writer.close();
                } else {
                    gameEngineActivity.createNewFile();
                    SIFAM.forcePermission(gameEngineActivity);
                    writeToGameEngineActivity(attempt + 1, user, pass);
                }
            } catch (Exception ex) {
                SIFAM.log(ex.getMessage());
            }
            updateFromGameEngineActivity();
            if (currentUser.equals(user) && currentPass.equals(pass)) {
                return true;
            } else {
                SIFAM.forcePermission(gameEngineActivity);
                SIFAM.forcePermission(gameEngineActivity.getParentFile());
                return writeToGameEngineActivity(attempt + 1, user, pass);
            }
        } else {
            SIFAM.Toast(code + " not installed");
            return false;
        }
    }

    public void forceCloseApp() {
        SIFAM.executeRootCommand("am force-stop " + className);
    }

    public void openApp() {
        SIFAM.log("Server[" + code + "] > openApp");
        if (SIFAM.CLOSE_BUTTON || SIFAM.OVERLAY_NAME) {
            SIFAM.getContext().startService(new Intent(SIFAM.getContext(), OverlayService.class));
        }
        Intent launchIntent = SIFAM.getContext().getPackageManager().getLaunchIntentForPackage(className);
        SIFAM.getContext().startActivity(launchIntent);
    }
}
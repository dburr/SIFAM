package com.caraxian.sifam;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.text.InputType;

public class AppPreferences extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(this);
        PreferenceCategory generalSettings = addCategory("General Settings", screen, "general", null, this);
        addCheckBox("Folders on Bottom", generalSettings, "folders_on_bottom", null, "Folders will be under accounts in the list.", "Folders will be above accounts in the list.", false, this);
        PreferenceCategory savingSettings = addCategory("Save & Load Options", screen, "saving", null, this);
        addCheckBox("Quick Save", savingSettings, "quick_save", null, "Account will use the current time as a name.", "Account will ask for name before saving.", false, this);
        addCheckBox("Auto Start SIF", savingSettings, "auto_start", null, "SIF will launch as soon as account has been loaded.", "SIF will not open after an account has been loaded.", true, this);
        addCheckBox("Disable Warnings and Alerts", savingSettings, "no_warnings", null,
                "No alerts or confirmations will be displayed when performing actions.\nThis feature is intended to be used with macros and should not be used with your main accounts.",
                "Alerts will be displayed when performing an action that may not be ideal.\nEnabling this feature is generally a bad idea unless you know what you're doing.", false, this);
        PreferenceCategory enabledServers = addCategory("Enabled Servers", screen, "enabled", null, this);
        for (Server s : SIFAM.serverList) {
            String extraMessage = "";
            if (s.installed == false) {
                extraMessage = "\nThis version of School Idol Festival is not installed!";
            }
            CheckBoxPreference c = addCheckBox(s.name, enabledServers, s.code, null, "Accounts from this server will be displayed." + extraMessage, "Accounts from this server will not be displayed." + extraMessage, false, this);
            if (s.installed == false) {
                c.setChecked(false);
                c.setEnabled(false);
            }
            ;
        }
        setPreferenceScreen(screen);
    }

    private PreferenceCategory addCategory(String title, PreferenceScreen screen, String key, Drawable icon, Context context) {
        PreferenceCategory c = new PreferenceCategory(context);
        c.setTitle(title);
        if (null != key) c.setKey(key);
        if (null != icon) c.setIcon(icon);
        screen.addPreference(c);
        return c;
    }

    private CheckBoxPreference addCheckBox(String title, PreferenceCategory category, String key, Drawable icon, String onText, String offText, boolean defaultValue, Context context) {
        CheckBoxPreference c = new CheckBoxPreference(context);
        c.setTitle(title);
        c.setKey(key);
        if (null != icon) c.setIcon(icon);
        if (offText.equals(null) || onText.equals(null)) {
            if (offText.equals(null)) {
                c.setSummary(onText);
            } else if (onText.equals(null)) {
                c.setSummary(offText);
            }
        } else {
            c.setSummaryOn(onText);
            c.setSummaryOff(offText);
        }
        c.setDefaultValue(defaultValue);
        c.setChecked(SIFAM.sharedPreferences.getBoolean(key, defaultValue));
        category.addPreference(c);
        return c;
    }

    private SwitchPreference addSwitch(String title, PreferenceCategory category, String key, Drawable icon, String onText, String offText, String onLabel, String offLabel, boolean defaultValue, Context context) {
        SwitchPreference c = new SwitchPreference(context);
        c.setTitle(title);
        c.setKey(key);
        c.setSwitchTextOn(onLabel);
        c.setSwitchTextOff(offLabel);
        if (null != icon) c.setIcon(icon);
        if (offText.equals(null) || onText.equals(null)) {
            if (offText.equals(null)) {
                c.setSummary(onText);
            } else if (onText.equals(null)) {
                c.setSummary(offText);
            }
        } else {
            c.setSummaryOn(onText);
            c.setSummaryOff(offText);
        }
        c.setDefaultValue(defaultValue);
        c.setChecked(SIFAM.sharedPreferences.getBoolean(key, defaultValue));
        category.addPreference(c);
        return c;
    }

    private EditTextPreference addEditText(String title, PreferenceCategory category, String key, Drawable icon, String summary, boolean number, Object defaultValue, Context context) {
        EditTextPreference c = new EditTextPreference(context);
        c.setTitle(title);
        c.setKey(key);
        c.setDefaultValue(defaultValue.toString());
        c.setSummary(summary);
        if (null != icon) c.setIcon(icon);
        if (number) {
            c.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
        }
        category.addPreference(c);
        return c;
    }
}

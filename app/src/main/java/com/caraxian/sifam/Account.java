package com.caraxian.sifam;
import java.io.File;
import java.util.Scanner;
public class Account {
    String path;
    String name;
    String displayName;
    Boolean isCurrent = false;
    Boolean isValid = false;
    long lastModified;
    long lastLoaded = 0;
    boolean isFolder = false;
    private String user_id = "";
    private String user_key = "";
    String hash;
    File file;
    Boolean exists = false;
    private Integer failCount = 0;
    public Account(String path) {
        this.path = path;
        this.file = new File(path);
        init();
    }
    public Account(File file) {
        this.file = file;
        this.path = file.toString();
        init();
    }
    private void init() {
        if (this.file.exists()) {
            if (this.file.getName().endsWith(".xml")) {
                try {
                    this.name = this.file.getName().substring(0, this.file.getName().length() - 4);
                    this.displayName = this.name;
                    this.lastModified = this.file.lastModified();
                    String text = new Scanner( this.file, "UTF-8" ).useDelimiter("\\A").next();
                    String s1[] = text.split("<string name=\\\"\\[LOVELIVE_ID\\]user_id\\\">");
                    if (s1.length == 2){
                        String s2[] = s1[1].split("<\\/string>");
                        this.user_id = s2[0].replace("-","");
                    }
                    String s3[] = text.split("<string name=\\\"\\[LOVELIVE_PW\\]passwd\">");
                    if (s3.length == 2){
                        String s4[] = s3[1].split("<\\/string>");
                        this.user_key = s4[0];
                    }
                    this.hash = user_id + user_key;
                    if (user_id.length() == 32 && user_key.length() == 128){
                        isValid = true;
                    }
                    this.exists = true;
                    if (MainActivity.currentFolder.equals("")) {
                        this.lastLoaded = MainActivity.lastLoaded.getLong(name, 0);
                    }else{
                        this.lastLoaded = MainActivity.lastLoaded.getLong(MainActivity.currentFolder + "/" + name,0);
                    }
                } catch (Exception IOException) {
                    if (this.failCount < 5) {
                        MainActivity.filePermissions(this.path);
                        this.failCount += 1;
                        this.init();
                    }
                }
            }else{
                if (this.file.isDirectory()){
                    this.isFolder = true;
                    this.exists = true;
                    this.name = this.file.getName();
                    this.displayName = this.name;
                }
            }
        }
    }

}

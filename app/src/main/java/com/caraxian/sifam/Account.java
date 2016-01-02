package com.caraxian.sifam;

public class Account {
    public String name;
    public long id;
    public long loaded;
    public boolean isFolder;
    public String server;
    public long parentFolder;
    public boolean locked = false;
    public String userKey;
    public String userPass;
    public boolean isCurrent() {
        for (Server s : SIFAM.serverList) {
            if (s.code.equals(server)) {
                if (s.currentUser == null || s.currentPass == null) {
                    return false;
                }
                if (s.currentUser.equals(userKey) && s.currentPass.equals(userPass)) {
                    return true;
                }
            }
        }
        return false;
    }
}

package edu.buffalo.cse.cse486586.simpledht;

import android.database.MatrixCursor;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by shahid on 4/4/15.
 */
public class CursorClass implements Serializable{

    String origPort;
    HashMap<String,String> curs;
    String thisPort;
    String stat;

    public String getStat() {
        return stat;
    }

    public void setStat(String stat) {
        this.stat = stat;
    }

    public String getOrigPort() {
        return origPort;
    }

    public void setOrigPort(String origPort) {
        this.origPort = origPort;
    }

    public HashMap<String,String> getCurs() {
        return curs;
    }

    public void setCurs(HashMap<String,String> curs) {
        this.curs = curs;
    }

    public String getThisPort() {
        return thisPort;
    }

    public void setThisPort(String thisPort) {
        this.thisPort = thisPort;
    }
}

package edu.buffalo.cse.cse486586.simpledht;

import java.io.Serializable;

/**
 * Created by shahid on 4/6/15.
 */
public class SingleDelete implements Serializable {

    String origPort;
    int curs;
    String thisPort;
    String stat;

    public String getOrigPort() {
        return origPort;
    }

    public void setOrigPort(String origPort) {
        this.origPort = origPort;
    }

    public int getCurs() {
        return curs;
    }

    public void setCurs(int curs) {
        this.curs = curs;
    }

    public String getThisPort() {
        return thisPort;
    }

    public void setThisPort(String thisPort) {
        this.thisPort = thisPort;
    }

    public String getStat() {
        return stat;
    }

    public void setStat(String stat) {
        this.stat = stat;
    }
}

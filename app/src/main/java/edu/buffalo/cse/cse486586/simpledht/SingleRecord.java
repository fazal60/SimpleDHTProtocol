package edu.buffalo.cse.cse486586.simpledht;

import android.database.MatrixCursor;

import java.io.Serializable;

/**
 * Created by shahid on 4/4/15.
 */
public class SingleRecord implements Serializable {

    String origPort;
    String curs;
    String thisPort;
    String selection;

    public String getSelection() {
        return selection;
    }

    public void setSelection(String selection) {
        this.selection = selection;
    }

    public String getOrigPort() {
        return origPort;
    }

    public void setOrigPort(String origPort) {
        this.origPort = origPort;
    }

    public String getCurs() {
        return curs;
    }

    public void setCurs(String curs) {
        this.curs = curs;
    }

    public String getThisPort() {
        return thisPort;
    }

    public void setThisPort(String thisPort) {
        this.thisPort = thisPort;
    }
}

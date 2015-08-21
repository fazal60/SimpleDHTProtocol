package edu.buffalo.cse.cse486586.simpledht;

import java.io.Serializable;

/**
 * Created by shahid on 4/3/15.
 */
public class LeastPort implements Serializable {

    String port;
    String key;
    String value;
    String origPort;

    public String getOrigPort() {
        return origPort;
    }

    public void setOrigPort(String origPort) {
        this.origPort = origPort;
    }


    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

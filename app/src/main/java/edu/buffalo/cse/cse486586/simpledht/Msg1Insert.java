package edu.buffalo.cse.cse486586.simpledht;

import java.io.Serializable;

/**
 * Created by shahid on 4/4/15.
 */
public class Msg1Insert implements Serializable{

    String key;
    String msg;
    String port;
    String origPort;

    public String getOrigPort() {
        return origPort;
    }

    public void setOrigPort(String origPort) {
        this.origPort = origPort;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}

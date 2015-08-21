package edu.buffalo.cse.cse486586.simpledht;

import java.io.Serializable;

/**
 * Created by shahid on 4/5/15.
 */
public class QueryClass1 implements Serializable
{

    String msg;
    String port;
    String origPort;
    String kvp;

    public String getKvp() {
        return kvp;
    }

    public void setKvp(String kvp) {
        this.kvp = kvp;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getOrigPort() {
        return origPort;
    }

    public void setOrigPort(String origPort) {
        this.origPort = origPort;
    }
}

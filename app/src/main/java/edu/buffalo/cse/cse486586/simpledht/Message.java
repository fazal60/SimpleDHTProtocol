package edu.buffalo.cse.cse486586.simpledht;

import java.io.Serializable;

/**
 * Created by shahid on 4/1/15.
 */
public class Message implements Serializable {

    String msg;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}

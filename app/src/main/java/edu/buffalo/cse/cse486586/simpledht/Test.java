package edu.buffalo.cse.cse486586.simpledht;

import java.io.Serializable;

/**
 * Created by shahid on 4/1/15.
 */
public class Test implements Serializable {

    String msg;
    String port;
    int ringCnt;

    public int getRingCnt() {
        return ringCnt;
    }

    public void setRingCnt(int ringCnt) {
        this.ringCnt = ringCnt;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}

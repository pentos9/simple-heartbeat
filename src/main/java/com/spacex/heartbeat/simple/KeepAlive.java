package com.spacex.heartbeat.simple;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class KeepAlive implements Serializable {
    @Override
    public String toString() {
        Date current = new Date();
        String message = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(current) + "\t keep alive package!";
        return message;
    }
}

package com.waterapps.lib;


/**
 * Created by Steve on 8/30/13.
 */
public class httpRequest {
    private String data;

    httpRequest() {
        data = new String();
    }

    httpRequest(String field, String data) {
        data = field.concat("=").concat(data);
    }

    public void addField(String field, String data) {
        data = data.concat("&").concat(field).concat("=").concat("data");
    }

    public String getString() {
        return data;
    }

    public byte [] getBytes() {
        return data.getBytes();
    }
}

package com.github.liaoheng.codec.model;

import java.io.IOException;

/**
 * @author liaoheng
 * @version 2019-04-12 09:59
 */
public class InitCodecException extends IOException {
    private String user;

    public InitCodecException(String user,Throwable e) {
        super(e);
        this.user = user;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}

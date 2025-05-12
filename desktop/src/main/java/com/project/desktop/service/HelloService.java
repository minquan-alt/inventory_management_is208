package com.project.desktop.service;

import com.project.desktop.util.HttpClientUtil;

public class HelloService {
    private final String helloUrl;

    public HelloService(String helloUrl) {
        this.helloUrl = helloUrl;
    }

    public String fetchHello() {
        return HttpClientUtil.get(helloUrl);
    }
}

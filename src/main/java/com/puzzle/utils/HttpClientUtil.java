package com.puzzle.utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpClientUtil {
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    public static String get(String url) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
            return CLIENT.send(req, HttpResponse.BodyHandlers.ofString()).body();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
package org.example;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class UrlWrapper { //Create for content server test purpose
    private final URL url;

    public UrlWrapper(String spec) throws MalformedURLException {
        this.url = new URL(spec);
    }

    public HttpURLConnection openConnection() throws IOException {
        return (HttpURLConnection) url.openConnection();
    }
}

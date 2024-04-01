package com.example.artemis.Component;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerConnectionChecker {
    public interface ConnectionListener {
        void onConnectionChecked(boolean isConnected);
    }

    private ExecutorService executor;

    public ServerConnectionChecker() {
        executor = Executors.newSingleThreadExecutor();
    }

    public void checkServerConnection(final ConnectionListener listener) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                boolean isConnected = false;
                try {
                    URL url = new URL("http://192.168.1.2:5000/");
                    HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                    urlc.setRequestMethod("GET");
                    urlc.setConnectTimeout(3000); // 3 seconds timeout
                    urlc.connect();
                    int code = urlc.getResponseCode();
                    isConnected = (code == HttpURLConnection.HTTP_OK);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (listener != null) {
                    listener.onConnectionChecked(isConnected);
                }
            }
        });
    }

    public void shutdown() {
        if (executor != null) {
            executor.shutdown();
        }
    }
}

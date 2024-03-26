//******************************************************************************************************
// APIService.java
//
// API service according to redfish specification.
//
//Copyright (C) 2022, PICMG.
//
//        This program is free software: you can redistribute it and/or modify
//        it under the terms of the GNU General Public License as published by
//        the Free Software Foundation, either version 3 of the License, or
//        (at your option) any later version.
//
//        This program is distributed in the hope that it will be useful,
//        but WITHOUT ANY WARRANTY; without even the implied warranty of
//        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//        GNU General Public License for more details.
//
//        You should have received a copy of the GNU General Public License
//        along with this program.  If not, see <https://www.gnu.org/licenses/>.
//*******************************************************************************************************


package org.picmg.redfish_server_template.services;

import org.apache.catalina.util.TLSUtil;
import org.picmg.redfish_server_template.utils.Utils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Service
public class APIServices {
    private static class DefaultTrustManager implements X509TrustManager {

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
        return null;
        }
    }
    String responseString = null;

    public String callGETAPI(String stringURL, String authToken) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://127.0.0.1:9443/",
                "data:{}",
                String.class);
        return responseString;
    }

    public JSONObject parseAPIResponse(HttpURLConnection connection) throws JSONException {
        String responseText = null;
        try(BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), Utils.API_CHARACTER_SET_NAME))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            responseText= String.valueOf(response);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONObject responseJson = new JSONObject(responseText);
        return responseJson;
    }

    // how to create key pair for "server side" of events
    // openssl req -x509 -newkey rsa:4096 -keyout key.pem -out cert.pem -days 10000 -nodes
    // how to create certificate for truststore
    // openssl x509 -outform der -in cert.pem -out cert.crt
    // command to add key to java default keystore:
    // sudo keytool -import -alias redfish-event-listener -keystore ./cacerts -file ~/git/Redfish-Event-Listener/cert.crt


    public String callPOSTAPI(String stringurl, String body, String authToken) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        URL url = new URL(stringurl);
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

        // Create the SSL connection
        SSLContext sc;
        sc = SSLContext.getInstance("TLS");
        sc.init(null, new TrustManager[] {new DefaultTrustManager()},  null);
        con.setSSLSocketFactory(sc.getSocketFactory());
        con.setRequestProperty("Content-Type", "application/json");
        con.setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession sslSession) {
                // accept all certificate sources
                // TODO: this should be replaced.  It is a patch to allow for
                // self-signed certs during debug.
                return true;
            }
        });
        con.setRequestMethod("POST");
        //con.setDoInput(true);
        con.setDoOutput(true);
        con.setUseCaches(false);
        con.setConnectTimeout(1500);
        con.setReadTimeout(500);
        con.connect();
        OutputStream os = con.getOutputStream();
        byte[] input = body.getBytes(StandardCharsets.US_ASCII);
        os.write(input, 0, input.length);
        os.flush();
        con.getContent();
        con.disconnect();
        return responseString;
    }
}

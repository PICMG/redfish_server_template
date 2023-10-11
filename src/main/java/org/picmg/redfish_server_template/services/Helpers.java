package org.picmg.redfish_server_template.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

public class Helpers {
    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static String createJsonStringFromObject(Object obj) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = "{}";
        try {
            // this removes any null values from the output
            json = mapper.writeValueAsString(obj);
        } catch (Exception ignored) {
        }
        return json;
    }

    public static ResponseEntity<?> getExternalResourceFromUri(HttpServletRequest request, String uri) {
        // TODO: need to implement this
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("External Resource Not Found");
    }

    public static String createDatabaseReffromRef(String ref, String currentDb) {
        // parse the reference into a uri and a definition location
        String refDefn = ref.split("#")[1];
        String dbUri = ref.split("#")[0];

        // remove leading / if one exists.
        dbUri = dbUri.substring(dbUri.lastIndexOf('/')+1);
        if (dbUri.isEmpty()) dbUri = currentDb;
        return dbUri + "#" +refDefn;
    }
}

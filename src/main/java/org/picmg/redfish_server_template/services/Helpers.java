package org.picmg.redfish_server_template.services;

public class Helpers {
    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(Exception e) {
            return false;
        }
        return true;
    }
}

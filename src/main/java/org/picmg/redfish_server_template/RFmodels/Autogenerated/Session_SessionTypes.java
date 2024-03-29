package org.picmg.redfish_server_template.RFmodels.Autogenerated;

import com.fasterxml.jackson.annotation.JsonValue;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Gets or Sets Session_v1_6_0_SessionTypes
 */
public enum Session_SessionTypes   {
  
  HOSTCONSOLE("HostConsole"),
  
  MANAGERCONSOLE("ManagerConsole"),
  
  IPMI("IPMI"),
  
  KVMIP("KVMIP"),
  
  OEM("OEM"),
  
  REDFISH("Redfish"),
  
  VIRTUALMEDIA("VirtualMedia"),
  
  WEBUI("WebUI");

  private String value;

  Session_SessionTypes(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static Session_SessionTypes fromValue(String value) {
    for (Session_SessionTypes b : Session_SessionTypes.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}


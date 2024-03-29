package org.picmg.redfish_server_template.RFmodels.Autogenerated;

import com.fasterxml.jackson.annotation.JsonValue;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Gets or Sets ExternalAccountProvider_v1_6_0_AuthenticationTypes
 */
public enum ExternalAccountProvider_AuthenticationTypes   {
  
  TOKEN("Token"),
  
  KERBEROSKEYTAB("KerberosKeytab"),
  
  USERNAMEANDPASSWORD("UsernameAndPassword"),
  
  OEM("OEM");

  private String value;

  ExternalAccountProvider_AuthenticationTypes(String value) {
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
  public static ExternalAccountProvider_AuthenticationTypes fromValue(String value) {
    for (ExternalAccountProvider_AuthenticationTypes b : ExternalAccountProvider_AuthenticationTypes.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}


package org.picmg.redfish_server_template.RFmodels.Autogenerated;

import com.fasterxml.jackson.annotation.JsonValue;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * The enumerations of InterfaceTypeSelection specify the method for switching the TrustedModule InterfaceType, for instance between TPM1_2 and TPM2_0, if supported.
 */
public enum ComputerSystem_InterfaceTypeSelection   {
  
  NONE("None"),
  
  FIRMWAREUPDATE("FirmwareUpdate"),
  
  BIOSSETTING("BiosSetting"),
  
  OEMMETHOD("OemMethod");

  private String value;

  ComputerSystem_InterfaceTypeSelection(String value) {
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
  public static ComputerSystem_InterfaceTypeSelection fromValue(String value) {
    for (ComputerSystem_InterfaceTypeSelection b : ComputerSystem_InterfaceTypeSelection.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}

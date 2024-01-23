package org.picmg.redfish_server_template.RFmodels.Autogenerated;

import com.fasterxml.jackson.annotation.JsonValue;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Gets or Sets Volume_EncryptionTypes
 */
public enum VolumeEncryptionTypes {
  
  NATIVEDRIVEENCRYPTION("NativeDriveEncryption"),
  
  CONTROLLERASSISTED("ControllerAssisted"),
  
  SOFTWAREASSISTED("SoftwareAssisted");

  private String value;

  VolumeEncryptionTypes(String value) {
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
  public static VolumeEncryptionTypes fromValue(String value) {
    for (VolumeEncryptionTypes b : VolumeEncryptionTypes.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}


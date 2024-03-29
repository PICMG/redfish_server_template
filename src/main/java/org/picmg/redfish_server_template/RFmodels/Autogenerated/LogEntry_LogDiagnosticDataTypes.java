package org.picmg.redfish_server_template.RFmodels.Autogenerated;

import com.fasterxml.jackson.annotation.JsonValue;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Gets or Sets LogEntry_v1_15_0_LogDiagnosticDataTypes
 */
public enum LogEntry_LogDiagnosticDataTypes   {
  
  MANAGER("Manager"),
  
  PREOS("PreOS"),
  
  OS("OS"),
  
  OEM("OEM"),
  
  CPER("CPER"),
  
  CPERSECTION("CPERSection");

  private String value;

  LogEntry_LogDiagnosticDataTypes(String value) {
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
  public static LogEntry_LogDiagnosticDataTypes fromValue(String value) {
    for (LogEntry_LogDiagnosticDataTypes b : LogEntry_LogDiagnosticDataTypes.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}


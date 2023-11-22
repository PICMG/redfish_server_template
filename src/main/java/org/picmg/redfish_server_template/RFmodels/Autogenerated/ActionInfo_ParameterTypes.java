package org.picmg.redfish_server_template.RFmodels.Autogenerated;

import com.fasterxml.jackson.annotation.JsonValue;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Gets or Sets ActionInfo_v1_4_0_ParameterTypes
 */
public enum ActionInfo_ParameterTypes   {
  
  BOOLEAN("Boolean"),
  
  NUMBER("Number"),
  
  NUMBERARRAY("NumberArray"),
  
  STRING("String"),
  
  STRINGARRAY("StringArray"),
  
  OBJECT("Object"),
  
  OBJECTARRAY("ObjectArray");

  private String value;

  ActionInfo_ParameterTypes(String value) {
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
  public static ActionInfo_ParameterTypes fromValue(String value) {
    for (ActionInfo_ParameterTypes b : ActionInfo_ParameterTypes.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}

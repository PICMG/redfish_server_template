package org.picmg.redfish_server_template.RFmodels.custom;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Objects;

/**
 * POJO wrapper for json_schema table entries.
 */
@Document("json_schema")
public class CachedSchema {
  @Field("_id")
  @Id
  private ObjectId _id;

  @JsonProperty("source")
  @Field("source")
  private String source;

  @JsonProperty("schema")
  @Field("schema")
  private String schema;

  public String getSource() {return source;}
  public String getSchema() {
    return schema;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CachedSchema obj = (CachedSchema) o;
    return Objects.equals(this.source, obj.source) &&
        Objects.equals(this.schema, obj.schema);
  }

  @Override
  public int hashCode() {
    return Objects.hash(source, schema);
  }

  @Override
  public String toString() {
      return "class CachedSchema {\n" +
            "    source: " + toIndentedString(source) + "\n" +
            "    schema: " + toIndentedString(schema) + "\n" +
            "}";
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}


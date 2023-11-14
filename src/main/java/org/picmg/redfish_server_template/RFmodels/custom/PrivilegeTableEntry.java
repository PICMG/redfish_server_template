package org.picmg.redfish_server_template.RFmodels.custom;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.security.core.GrantedAuthority;
import springfox.documentation.spring.web.json.Json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Document("privileges_table")
public class PrivilegeTableEntry {
    @Field("_id")
    @Id
    private ObjectId _id;

    @JsonProperty("uri")
    @Field("uri")
    private String uri;

    @JsonProperty("Entity")
    @Field("Entity")
    private String entity;

    @JsonProperty("OperationMap")
    @Field("OperationMap")
    private Map<String, Object> operationMap;

    public String getUri() {return uri;}

    public String getEntity() {return entity;}

    public boolean isMatchingUrl(String uri) {
        Pattern regex = Pattern.compile(this.uri);
        Matcher matcher = regex.matcher(uri);
        return matcher.matches();
    }

    public boolean isAuthorized(String operation, Collection<? extends GrantedAuthority> roles) {
        try {
            // attempt to find a matching operation
            ArrayList<Map<String,ArrayList<String>>> operationPrivileges = (ArrayList<Map<String,ArrayList<String>>>)(operationMap.get(operation));

            // loop for each privilege associated with the operation
            for (Map<String, ArrayList<String>> obj: operationPrivileges) {
                // loop for each role in the json array
                ArrayList<String> ary = obj.get("Privilege");
                for (String role: ary) {
                    for (GrantedAuthority assigned_role:roles) {
                        if (Objects.equals(assigned_role.getAuthority(), role)) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        if (!Objects.equals(uri, ((PrivilegeTableEntry) o).getUri())) return false;
        return (Objects.equals(operationMap,((PrivilegeTableEntry) o).operationMap));
    }

    @Override
    public int hashCode() {
        return Objects.hash(_id,uri,operationMap);
    }

    @Override
    public String toString() {
        return "class PrivilegeTableEntry {\n" +
                "    uri: " + toIndentedString(uri) + "\n" +
                "    OperationMap: " + toIndentedString(operationMap) + "\n" +
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


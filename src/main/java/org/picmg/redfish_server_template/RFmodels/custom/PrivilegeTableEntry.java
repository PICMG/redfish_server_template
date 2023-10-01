package org.picmg.redfish_server_template.RFmodels.custom;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
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

    @JsonProperty("OperationMap")
    @Field("operationMap")
    private JSONObject operationMap;

    public String getUri() {return uri;}

    public JSONObject getOperationMap() {return operationMap;}

    public boolean isMatchingUrl(String uri) {
        Pattern regex = Pattern.compile(this.uri);
        Matcher matcher = regex.matcher(uri);
        return matcher.matches();
    }

    public boolean isAuthorized(String operation, Collection<? extends GrantedAuthority> roles) {
        try {
            // attempt to find a matching operation
            JSONArray operationPrivileges = operationMap.getJSONArray(operation);
            // loop for each privilege associated with the operation
            for (Object obj: operationPrivileges) {
                JSONObject jobj = (JSONObject)obj;
                // loop for each role in the json array
                JSONArray ary = ((JSONArray)(jobj.get("Privilege")));
                for (int i = 0; i<ary.length(); i++) {
                    String role = ary.getString(i);
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
        return (Objects.equals(operationMap,((PrivilegeTableEntry) o).getOperationMap()));
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


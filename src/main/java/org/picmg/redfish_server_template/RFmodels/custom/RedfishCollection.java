package org.picmg.redfish_server_template.RFmodels.custom;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RedfishCollection extends RedfishObject {
    public List<Document> getMembers() {
        List<Document> result = new ArrayList<>();
        if (!containsKey("Members")) return result;
        if (get("Members") instanceof List) {
            for (Object obj: (List)get("Members")) {
                if (obj instanceof Document) result.add((Document)obj);
            }
        }
        return result;
    }

    public void setMembers(List<Document> members) {

        put("Members",members);
    }
    public Integer getMembersAtOdataCount() {
        if (!containsKey("Members@odata.count")) return 0;
        if (get("Members@odata.count") instanceof Integer)
            return (Integer) get("Members@odata.count");
        return 0;
    }

    public String getMembersAtOdataNextLink() {
        if (!containsKey("Members@odata.nextLink")) return null;
        if (get("Members@odata.nextLink") instanceof String)
            return (String)get("Members@odata.nextLink");
        return null;
    }
    public void setMembersAtOdataNextLink(String membersAtOdataNextLink) {
        put("Members@odata.nextLink", membersAtOdataNextLink);
    }
}

package org.picmg.redfish_server_template.RFmodels.custom;

import org.bson.Document;

import java.util.List;
import java.util.Map;

public class RedfishObject extends Document {
    public String getAtOdataId() {
        return this.get("@odata.id").toString();
    }

    public void setAtOdataId(String atOdataId) {
        this.put("@odata.id", atOdataId);
    }

    public String getAtOdataType() {
        return this.get("@odata.type").toString();
    }

    public void setAtOdataType(String atOdataType) {
        this.put("@odata.type", atOdataType);
    }

    public String getAtOdataContext() {
        return this.get("@odata.context").toString();
    }
    public void setAtOdataContext(String atOdataContext) {
        this.put("@odata.context", atOdataContext);
    }

    public String getAtOdataEtag() {
        return this.get("_odata_etag","00000000000000000000000000000000");
    }

    public String getId() {
        return this.get("Id").toString();
    }

    public void setId(String id) {
        this.put("Id", id);
    }

    public String getName() {
        return this.get("Name").toString();
    }

    public void setName(String name) {
        this.put("Name", name);
    }

    public String getDescription() {
        return this.get("Description").toString();
    }

    public void setDescription(String description) {
        this.put("Description", description);
    }

    private void updateArrayHelper(List<Object> original, List<Object> data) {
        if (data.isEmpty()) return;
        for (int i=0;i<data.size();i++) {
            Object dataElement = data.get(i);
            if (dataElement == null) continue;
            if (dataElement instanceof Map) {
                // skip no change elements
                if (((Map<String,Object>)dataElement).isEmpty()) continue;
            }
            // here if the element should be modified or added
            if (i>=original.size()) {
                original.add(data.get(i));
            } else {
                original.set(i, data.get(i));
            }
        }
        // now delete values
        for (int i=data.size()-1; i>=0; i--) {
            if (i>=original.size()) continue;
            if (data.get(i)==null) original.remove(i);
        }
    }

    private void updateHelper(Map<String,Object> original, Map<String,Object> data) {
        // loop for each key in the data set
        for (String key: data.keySet()) {
            if (!original.containsKey(key)) {
                // here if the key is new - just add it.
                original.put(key,data.get(key));
            } else {
                // here the key matches an object in the original
                if (original.get(key).getClass() != data.get(key).getClass()) {
                    // the class of the new object is not the same as the class of the original
                    // replace what is in the original
                    original.put(key, data.get(key));
                } else {
                    if (original.get(key) instanceof Map) {
                        updateHelper((Map<String,Object>)original.get(key), (Map<String,Object>)data.get(key));
                    } else if (original.get(key) instanceof List) {
                        updateArrayHelper((List<Object>)original.get(key),(List<Object>)data.get(key));
                    } else {
                        original.put(key, data.get(key));
                    }
                }
            }
        }
    }

    // update the redfish object using information stored in the data parameter.
    //    Matching named fields are replaced with the new data
    //    Named fields that don't exist in the object are created from the data
    //    Updates for arrays are handled in this order:  Modifications, deletions, additions
    //    For arrays, {}--> no change;  null--> delete, anything else replaces
    public void update(Map<String,Object> data) {
        updateHelper(this, data);
    }
}
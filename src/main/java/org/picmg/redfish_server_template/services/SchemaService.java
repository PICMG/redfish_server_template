//******************************************************************************************************
// AccountService.java
//
// Service for account according to redfish.
//
//Copyright (C) 2022, PICMG.
//
//        This program is free software: you can redistribute it and/or modify
//        it under the terms of the GNU General Public License as published by
//        the Free Software Foundation, either version 3 of the License, or
//        (at your option) any later version.
//
//        This program is distributed in the hope that it will be useful,
//        but WITHOUT ANY WARRANTY; without even the implied warranty of
//        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//        GNU General Public License for more details.
//
//        You should have received a copy of the GNU General Public License
//        along with this program.  If not, see <https://www.gnu.org/licenses/>.
//*******************************************************************************************************


package org.picmg.redfish_server_template.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.picmg.redfish_server_template.RFmodels.custom.CachedSchema;
import org.picmg.redfish_server_template.repository.CachedSchemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;

@Service
public class SchemaService {
    private List<CachedSchema> cache = null;

    @Autowired
    CachedSchemaRepository schemaRepository;
    public CachedSchema getFromSource(String source) {
        if (cache == null) {
            cache = schemaRepository.findAll();
        }
        return schemaRepository.getFirstBySource(source);
    }

    // return true if the base class for the referenced schema type
    // is deletable
    public boolean isDeletable(String type) {
        CachedSchema schemaObj = getFromSource(type+".json");
        // this is not the most robust ways to check for the deletable field in the schema, but it is relatively fast
        String schemaStr = schemaObj.getSchema().replace(" ","");
        return schemaStr.contains("\"deletable\":true");
    }

    // return true if the base class for the referenced schema type
    // is insertable
    public boolean isInsertable(String type) {
        CachedSchema schemaObj = getFromSource(type+".json");
        // this is not the most robust ways to check for the insertable field in the schema, but it is relatively fast
        String schemaStr = schemaObj.getSchema().replace(" ","");
        return schemaStr.contains("\"insertable\":true");
    }

    // return true if the base class for the referenced schema type
    // is updatable
    public boolean isUpdatable(String type) {
        CachedSchema schemaObj = getFromSource(type+".json");
        // this is not the most robust ways to check for the deletable field in the schema, but it is relatively fast
        String schemaStr = schemaObj.getSchema().replace(" ","");
        return schemaStr.contains("\"updatable\":true");
    }

    public CachedSchema getNewestVersionFromType(String type) {
        if (cache==null) {
            cache = schemaRepository.findAll();
        }

        String newestVersion = null;
        CachedSchema schema = null;
        // loop for each entry in the cache
        for (CachedSchema entry: cache) {
            // skip entries that are not of the same base type
            if (!entry.getSource().startsWith(type+".")) continue;

            // get the version information from the source file
            String source = entry.getSource();
            String version = source.split("\\.")[1].toLowerCase();

            // remove the 'v' from the version.  skip files that don't have a version.
            if (!version.startsWith("v")) continue;
            version = version.replace("v","");

            // if this is the first entry with a valid version, then it is the newest one found so far
            if (newestVersion==null) {
                newestVersion = version;
                schema = entry;
                continue;
            }

            // Otherwise, compare this version with the previous newest version.
            // Compare major versions
            if (Integer.parseInt(version.split("_")[0])>
                Integer.parseInt(newestVersion.split("_")[0])) {
                newestVersion = version;
                schema = entry;
                continue;
            }
            if (Integer.parseInt(version.split("_")[0])<
                    Integer.parseInt(newestVersion.split("_")[0])) {
                continue;
            }
            // here if major version is the same - check minor version
            if (Integer.parseInt(version.split("_")[1])>
                    Integer.parseInt(newestVersion.split("_")[1])) {
                newestVersion = version;
                schema = entry;
                continue;
            }
            if (Integer.parseInt(version.split("_")[1])<
                    Integer.parseInt(newestVersion.split("_")[1])) {
                continue;
            }
            // here if major and minor versions are the same.  Check the subversion.
            if (Integer.parseInt(version.split("_")[2])>
                    Integer.parseInt(newestVersion.split("_")[2])) {
                newestVersion = version;
                schema = entry;
            }
        }
        return schema;
    }

    // isSchemaFieldReadOnly
    //
    // Given a field name in the format of field/subfield/subfield, determine if the field is read-only based on
    // the value of the readonly metadata field for the associated property.  Note: the field reference should
    // not include array indices.  So the reference for field/1/subfield, should just be field/subfield
    //
    // parameters
    //   schemaReference - the complete odata type reference to be checked in the form url#/fragment
    //   fieldName - the field name to check.
    public boolean isSchemaFieldReadOnly(String schemaReference, String fieldName) {
        // split reference at hash sign to get the fragment and the url
        String [] refparts = schemaReference.split("#");
        if (refparts.length != 2) return true;
        String schemaUri = refparts[0];
        String schemaFragment = refparts[1];
        String schemaFile = schemaUri.split("/")[schemaUri.split("/").length-1];

        // attempt to find the schema
        CachedSchema schemaObj = this.getFromSource(schemaFile);
        if (schemaObj == null) return true;

        // Convert the schema into a json object
        JsonNode schemaJson;
        try {
            schemaJson = new ObjectMapper().readTree(schemaObj.getSchema());
        } catch (JsonProcessingException ignored) {
            return true;
        }

        // see if the field exists
        String[] fieldparts = fieldName.split("/");
        String field = fieldparts[0];

        // assume fields with @ symbol in them are not updatable through patching
        if (field.contains("@")) return true;

        // construct the reference
        String ref = schemaFragment+"/properties/"+field;
        if (ref.startsWith("#")) ref = ref.replaceFirst("#","/");
        JsonNode jsonNode = schemaJson.at(ref);
        if (jsonNode == null) return true;

        // if the field exists, return the readonly metadata value if it exists
        if (jsonNode.has("readonly")) {
            // if the readonly field exists, return its value
            return jsonNode.get("readonly").asBoolean();
        }
        if (jsonNode.has("readOnly")) {
            // if the readonly field exists, return its value
            return jsonNode.get("readOnly").asBoolean();
        }

        // here if the field is an object or an array
        String newRef = null;
        if (jsonNode.has("$ref")) {
            // here if the field references an object
            newRef = jsonNode.get("$ref").toString();
        } else if (jsonNode.has("items")) {
            // here if the field references an array
            // the items field should be a complex object that has a reference in it or an anyOf construct
            if (jsonNode.get("items").has("$ref")) {
                newRef = jsonNode.get("items").get("$ref").toString();
            } else if (jsonNode.get("items").has("anyOf")) {
                // get the last entry of the array that has a $ref in it
                JsonNode anyOfNode = jsonNode.get("items").get("anyOf");
                if (!anyOfNode.isArray()) return true;
                Iterator<JsonNode> it = anyOfNode.elements();
                while (it.hasNext()) {
                    JsonNode node = it.next();
                    if (node.has("$ref")) newRef = node.get("$ref").toString();
                }
            } else {
                // here if this is an array without any reference type
                return true;
            }
        } else if (jsonNode.has("anyOf")) {
            // get the last entry in the array that has a $ref in it
            Iterator<JsonNode> it = jsonNode.elements();
            while (it.hasNext()) {
                JsonNode node = it.next();
                if (node.has("$ref")) newRef = node.get("$ref").toString();
            }
        }
        if (newRef==null) return true;
        if (!newRef.contains("#")) return true;
        newRef = newRef.replace("\"", "");
        String newUrl = newRef.split("#")[0];
        String newFragment = newRef.split("#")[1];
        if (newUrl.contains("/")) newUrl = newUrl.substring(newUrl.lastIndexOf("/")+1);
        if (newUrl.isEmpty()) newUrl = schemaFile;
        return isSchemaFieldReadOnly(newUrl+"#"+newFragment,fieldName.substring(field.length()+1));
    }

    // isSchemaFieldWriteOnly
    //
    // Given a field name in the format of field/subfield/subfield, determine if the field is write-only based on
    // the value of the writeonly metadata field for the associated property.  Note: the field reference should
    // not include array indices.  So the reference for field/1/subfield, should just be field/subfield
    //
    // parameters
    //   schemaReference - the complete odata type reference to be checked in the form url#/fragment
    //   fieldName - the field name to check.
    public boolean isSchemaFieldWriteOnly(String schemaReference, String fieldName) {
        // split reference at hash sign to get the fragment and the url
        String [] refparts = schemaReference.split("#");
        if (refparts.length != 2) return false;
        String schemaUri = refparts[0];
        String schemaFragment = refparts[1];
        String schemaFile = schemaUri.split("/")[schemaUri.split("/").length-1];

        // attempt to find the schema
        CachedSchema schemaObj = this.getFromSource(schemaFile);
        if (schemaObj == null) return false;

        // Convert the schema into a json object
        JsonNode schemaJson;
        try {
            schemaJson = new ObjectMapper().readTree(schemaObj.getSchema());
        } catch (JsonProcessingException ignored) {
            return false;
        }

        // see if the field exists
        String[] fieldparts = fieldName.split("/");
        String field = fieldparts[0];

        // assume fields with @ symbol in them are not updatable through patching
        if (field.contains("@")) return false;

        // construct the reference
        String ref = schemaFragment+"/properties/"+field;
        if (ref.startsWith("#")) ref = ref.replaceFirst("#","/");
        JsonNode jsonNode = schemaJson.at(ref);
        if (jsonNode == null) return false;

        // if the field exists, return the readonly metadata value if it exists
        if ((jsonNode.has("readonly"))||(jsonNode.has("readonly"))) {
            // if the readonly field exists, we have traversed to the final depth
            if (jsonNode.has("writeonly")) {
                return jsonNode.get("writeonly").asBoolean();
            }
            if (jsonNode.has("writeOnly")) {
                return jsonNode.get("writeOnly").asBoolean();
            }
            // if writeonly doesn't exist, assume the field is writeable
            return false;
        }

        // here if the field is an object or an array
        String newRef = null;
        if (jsonNode.has("$ref")) {
            // here if the field references an object
            newRef = jsonNode.get("$ref").toString();
        } else if (jsonNode.has("items")) {
            // here if the field references an array
            // the items field should be a complex object that has a reference in it or an anyOf construct
            if (jsonNode.get("items").has("$ref")) {
                newRef = jsonNode.get("items").get("$ref").toString();
            } else if (jsonNode.get("items").has("anyOf")) {
                // get the last entry of the array that has a $ref in it
                JsonNode anyOfNode = jsonNode.get("items").get("anyOf");
                if (!anyOfNode.isArray()) return false;
                Iterator<JsonNode> it = anyOfNode.elements();
                while (it.hasNext()) {
                    JsonNode node = it.next();
                    if (node.has("$ref")) newRef = node.get("$ref").toString();
                }
            } else {
                // here if this is an array without any reference type
                return false;
            }
        } else if (jsonNode.has("anyOf")) {
            // get the last entry in the array that has a $ref in it
            Iterator<JsonNode> it = jsonNode.elements();
            while (it.hasNext()) {
                JsonNode node = it.next();
                if (node.has("$ref")) newRef = node.get("$ref").toString();
            }
        }
        if (newRef==null) return false;
        if (!newRef.contains("#")) return false;
        newRef = newRef.replace("\"", "");
        String newUrl = newRef.split("#")[0];
        String newFragment = newRef.split("#")[1];
        if (newUrl.contains("/")) newUrl = newUrl.substring(newUrl.lastIndexOf("/")+1);
        if (newUrl.isEmpty()) newUrl = schemaFile;
        return isSchemaFieldWriteOnly(newUrl+"#"+newFragment,fieldName.substring(field.length()+1));
    }
}

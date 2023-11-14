package org.picmg.redfish_server_template.controllers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.picmg.redfish_server_template.RFmodels.AllModels.RedfishError;
import org.picmg.redfish_server_template.RFmodels.custom.CachedSchema;
import org.picmg.redfish_server_template.RFmodels.custom.RedfishCollection;
import org.picmg.redfish_server_template.RFmodels.custom.RedfishObject;
import org.picmg.redfish_server_template.data_validation.ValidRedfishObject;
import org.picmg.redfish_server_template.repository.RedfishObjectRepository;
import org.picmg.redfish_server_template.services.Helpers;
import org.picmg.redfish_server_template.services.PrivilegeTableService;
import org.picmg.redfish_server_template.services.RedfishErrorResponseService;
import org.picmg.redfish_server_template.services.SchemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

// this is the highest level redfish object handler.  All pages are derived from it
// so the Request mapping is for all matches **.  The wildcard uri is resolved so that more specific
// uris will take precedence over a wildcard match.  So controllers derived from this can override the path with
// a more specific value.
@RestController
@RequestMapping({
        "/redfish/v1/**"
})
class RedfishObjectController {

    @Autowired
    RedfishObjectRepository objectRepository;

    @Autowired
    RedfishErrorResponseService redfishErrorResponseService;

    @Autowired
    SchemaService schemaService;

    @Autowired
    PrivilegeTableService privilegeTableService;

    @GetMapping(value="")
    public ResponseEntity<?> getEntryPoint(HttpServletRequest request) {
        // get the database entry associated with this path
        String uri = request.getRequestURI();
        RedfishObject entity = objectRepository.findFirstWithQuery(Criteria
                .where("_odata_id").is(uri));

        if (entity == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }

        // the entity must include the @odata.type field.  Check to see if it includes "Collection" in the name
        if (!entity.containsKey("@odata.type")) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("");
        }

        if (entity instanceof RedfishCollection) {
            return processCollectionGet((RedfishCollection) entity, request);
        }
        return processObjectGet(entity, request);
    }

    private void nullWriteOnlyHelper(Map<String,Object> map, String schemaReference, String field) {
        for (String key: map.keySet()) {
            Object obj = map.get(key);
            String newField = field;
            if (newField.isEmpty()) {
                newField = key;
            } else {
                newField = newField+"/"+key;
            }
            if (obj instanceof Map) {
                // recurse to take care of this object
                //noinspection unchecked
                nullWriteOnlyHelper((Map<String,Object>)obj, schemaReference, newField);

            } else if (obj instanceof List) {
                // if the entire list is write-only, then null all the items
                if (schemaService.isSchemaFieldWriteOnly(schemaReference,newField)) {
                    //noinspection unchecked
                    Collections.fill(((List<Object>) obj), null);
                } else {
                    // the list is not write-only, but elements of it might be
                    //noinspection unchecked
                    for (Object element : (List<Object>) obj) {
                        if (element instanceof Map)
                            // recurse to take care of this object - the elements should be complex
                            //noinspection unchecked
                            nullWriteOnlyHelper((Map<String,Object>)element, schemaReference, newField);
                    }
                }
            } else {
                // otherwise, this is a simple object - apply the property for the object
                if (schemaService.isSchemaFieldWriteOnly(schemaReference,newField)) {
                    map.put(key,null);
                }
            }
        }
    }

    private void nullWriteOnlyValues(RedfishObject entity) {
        // Create the schema reference from the entity information
        String ref = entity.get("@odata.type").toString();
        ref = ref.replace("#","");
        int pos = ref.lastIndexOf('.');
        String schemaFilePart = ref.substring(0,pos);
        String fragmentPart = ref.substring(pos+1);
        String schemaReference = schemaFilePart+".json#/definitions/"+fragmentPart;

        String field = "";
        nullWriteOnlyHelper(entity, schemaReference, field);
    }

    private List<RedfishError> removeReadOnlyHelper(Map<String,Object> map, String schemaReference, String field) {
        ArrayList<RedfishError> result = new ArrayList<>();

        Iterator<Map.Entry<String,Object>> it = map.entrySet().iterator();
        while (it.hasNext ()) {
            Map.Entry<String,Object> entry = it.next();
            String key = entry.getKey();
            Object obj = entry.getValue();
            String newField = field;
            if (newField.isEmpty()) {
                newField = key;
            } else {
                newField = newField+"/"+key;
            }
            if (obj instanceof Map) {
                // recurse to take care of this object
                @SuppressWarnings("unchecked") List<RedfishError> partial = removeReadOnlyHelper((Map<String,Object>)obj, schemaReference, newField);
                result.addAll(partial);
                if (!partial.isEmpty()) it.remove();

            } else if (obj instanceof List) {
                // if the entire list is write-only, then null all the items
                if (schemaService.isSchemaFieldReadOnly(schemaReference,newField)) {
                    @SuppressWarnings("unchecked") Iterator<Object> lItr = ((List<Object>)obj).iterator();
                    while (lItr.hasNext()) {
                        lItr.next();
                        // Property is not writable - remove it from the list
                        RedfishError error = redfishErrorResponseService.getErrorMessage(
                                "Base",
                                "PropertyNotWritable",
                                Collections.singletonList(newField),
                                new ArrayList<>());
                        result.add(error);
                        lItr.remove();
                    }
                    //noinspection unchecked
                    if (((List<Object>) obj).isEmpty()) {
                        it.remove();
                    }
                } else {
                    // Objects in the list may be written only if they are objects
                    @SuppressWarnings("unchecked") Iterator<Object> lItr = ((List<Object>)obj).iterator();
                    while (lItr.hasNext()) {
                        Object element = lItr.next();
                        // recurse to take care of this object - the elements should be complex
                        if (element instanceof Map) {
                            @SuppressWarnings("unchecked") List<RedfishError> partial = removeReadOnlyHelper((Map<String,Object>)element, schemaReference, newField);
                            result.addAll(partial);
                            if (!partial.isEmpty()) lItr.remove();
                        }
                    }
                    //noinspection unchecked
                    if (((List<Object>) obj).isEmpty()) {
                        it.remove();
                    }
                }
            } else {
                // otherwise, this is a simple object - apply the property for the object
                if (schemaService.isSchemaFieldReadOnly(schemaReference,newField)) {
                    // Property not writable, remove it from the object
                    // don't give an error for @odata.type - this may have been added by the data validation filter
                    if (!newField.equals("@odata.type")) {
                        RedfishError error = redfishErrorResponseService.getErrorMessage(
                                "Base",
                                "PropertyNotWritable",
                                Collections.singletonList(newField),
                                new ArrayList<>());
                        result.add(error);
                    }
                    it.remove();
                }
            }
        }
        return result;
    }

    private List<RedfishError> removeReadOnlyValues(RedfishObject entity) {
        // Create the schema reference from the entity information
        String ref = entity.get("@odata.type").toString();
        ref = ref.replace("#","");
        int pos = ref.lastIndexOf('.');
        String schemaFilePart = ref.substring(0,pos);
        String fragmentPart = ref.substring(pos+1);
        String schemaReference = schemaFilePart+".json#/definitions/"+fragmentPart;

        String field = "";
        return removeReadOnlyHelper(entity, schemaReference, field);
    }

    public ResponseEntity<?> processObjectGet(RedfishObject entity, HttpServletRequest request) {
        // Check parameters for unknown or unsupported parameters
        Map<String,String[]> parameters = request.getParameterMap();
        ArrayList<String> unknownParameters = new ArrayList<>();
        boolean excerpt = false;
        for (Map.Entry<String,String[]> parameter: parameters.entrySet()) {
            String key = parameter.getKey();
            if (key.startsWith("$")) {
                switch (key) {
                    case "$top":
                    case "$skip":
                        // REQ: Shall return HTTP 400 bad request status code for any query parameters
                        // that contain values that are invalid or values applied to the query parameters
                        // without defined values, such as excerpt, only
                        if (parameter.getValue().length != 1 ||
                                (!Helpers.isInteger(parameter.getValue()[0])) ||
                                (Integer.parseInt(parameter.getValue()[0]) < 0)) {
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("");
                        }
                        // ignore top for non collections
                        // ignore skip for non-collections
                        break;
                    default:
                        // REQ: may support $expand, $filter, and $select query parameters
                        unknownParameters.add(key);
                        break;
                }
            } else {
                switch(key) {
                    case "excerpt":
                    case "only":
                        // REQ: Shall return HTTP 400 bad request status code for any query parameters
                        // that contain values that are invalid or values applied to the query parameters
                        // without defined values, such as excerpt, only
                        if ((parameter.getValue().length != 1) || (!parameter.getValue()[0].isEmpty())) {
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bad Request");
                        }
                        if (key.equals("excerpt")) excerpt = true;
                        break;
                    default:
                        // REQ: Shall ignore unknown or unsupported query parameters that
                        // do not begin with $
                        break;
                }
            }
        }
        if (!unknownParameters.isEmpty()) {
            // REQ: shall return a 501 not implemented status code for any unsupported
            // query parameters that start with $.  An extended error that
            // indicates unsupported query parameters for this resource
            RedfishError error = redfishErrorResponseService.getErrorMessage(
                    "Base","QueryNotSupported",new ArrayList<>(), new ArrayList<>());
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(error);
        }

        if ((parameters.containsKey("only"))&&(parameters.entrySet().size()>1)) {
            // REQ: Services should return the HTTP 400 Bad Request with the QueryCombinationInvalid message from the
            // base message registry if 'only' is being combined with other query parameters.
            RedfishError error = redfishErrorResponseService.getErrorMessage(
                    "Base","QueryCombinationInvalid",new ArrayList<>(), new ArrayList<>());
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(error);
        }

        // Convert the entity to a json object and prepare it to send in response body
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = "{}";
        // remove any keys that start with underscore character
        entity.remove("_odata_id");
        entity.remove("_odata_type");
        entity.remove("_id");

        // Replace any write-only content with nulls
        nullWriteOnlyValues(entity);

        try {
            json = mapper.writeValueAsString(entity);
        } catch (Exception ignored) {
        }

        // here if the request is valid - convert the POJO to a clean JSON string and return the results
        // within the body of the HTTP response.
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(json);
    }

    public ResponseEntity<?> processCollectionGet(RedfishCollection entity, HttpServletRequest request) {
        // get the database entry associated with this path
        String uri = request.getRequestURI();

        // Check parameters for unknown or unsupported parameters
        Map<String,String[]> parameters = request.getParameterMap();
        ArrayList<String> unknownParameters = new ArrayList<>();
        int top = entity.getMembersAtOdataCount();
        int skip = 0;
        boolean excerpt = false;
        boolean only = false;
        for (Map.Entry<String,String[]> parameter: parameters.entrySet()) {
            String key = parameter.getKey();
            if (key.startsWith("$")) {
                switch (key) {
                    case "$top":
                    case "$skip":
                        // REQ: Shall return HTTP 400 bad request status code for any query parameters
                        // that contain values that are invalid or values applied to the query parameters
                        // without defined values, such as excerpt, only
                        if (parameter.getValue().length != 1 ||
                                (!Helpers.isInteger(parameter.getValue()[0])) ||
                                (Integer.parseInt(parameter.getValue()[0]) < 0)) {
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("");
                        }
                        if (key.equals("$top")) {
                            top = Integer.parseInt((parameter.getValue()[0]));
                            if (top > entity.getMembersAtOdataCount())
                                top = entity.getMembersAtOdataCount();
                        }
                        if (key.equals("$skip")) {
                            skip = Integer.parseInt(parameter.getValue()[0]);
                            if (skip > entity.getMembersAtOdataCount())
                                skip = entity.getMembersAtOdataCount();
                        }
                        break;
                    default:
                        // REQ: may support $expand, $filter, and $select query parameters
                        unknownParameters.add(key);
                        break;
                }
            } else {
                switch(key) {
                    case "excerpt":
                    case "only":
                        // REQ: Shall return HTTP 400 bad request status code for any query parameters
                        // that contain values that are invalid or values applied to the query parameters
                        // without defined values, such as excerpt, only
                        if ((parameter.getValue().length != 1) || (!parameter.getValue()[0].isEmpty())) {
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bad Request");
                        }
                        if (key.equals("only")) only = true;
                        if (key.equals("excerpt")) excerpt = true;
                        break;
                    default:
                        // REQ: Shall ignore unknown or unsupported query parameters that
                        // do not begin with $
                        break;
                }
            }
        }
        if (!unknownParameters.isEmpty()) {
            // REQ: shall return a 501 not implemented status code for any unsupported
            // query parameters that start with $.  An extended error that
            // indicates unsupported query parameters for this resource
            RedfishError error = redfishErrorResponseService.getErrorMessage(
                    "Base","QueryNotSupported",new ArrayList<>(), new ArrayList<>());
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(error);
        }

        if ((parameters.containsKey("only"))&&(parameters.entrySet().size()>1)) {
            // REQ: Services should return the HTTP 400 Bad Request with the QueryCombinationInvalid message from the
            // base message registry if 'only' is being combined with other query parameters.
            RedfishError error = redfishErrorResponseService.getErrorMessage(
                    "Base","QueryCombinationInvalid",new ArrayList<>(), new ArrayList<>());
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(error);
        }

        // Check for special case - there is only one member left, and only is specified
        //if ((entity.getMembersAtOdataCount() == 1) && (parameters.containsKey("only"))) {
/* TODO - redo this  code to use RedfishObject collection search
            // Parse the URI to determine if it is local or remote
            String refUri = entity.getMembers().get(0).getAtOdataId();

            // attempt to get the resource from the related database
            RedfishObject refObject = objectRepository.getFirstByUri(refUri);
            if (refObject != null) {
                // object found in local repository.
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(
                        Helpers.createJsonStringFromObject(refObject));
            }

            // if the reference is for an external entity, return the entity
            return Helpers.getExternalResourceFromUri(request, refUri);

 */
        //}

        // REQ: Services shall process query parameters in this order: $filter, $skip, $top,
        // apply server-side pagination, $expand, excerpt, $select
        List<Document> members = entity.getMembers();
        if (skip+top<members.size()) {
            entity.setMembersAtOdataNextLink(uri+"?$skip="+Long.toString(skip+top));
        } else {
            entity.setMembersAtOdataNextLink(null);
        }
        // skip the first n members in the collection
        for (int i=0;i<skip;i++) {
            if (!members.isEmpty()) members.remove(0);
        }
        // show only the top m members in the collection
        while (members.size()>top) members.remove(top);
        entity.setMembers(members);

        // remove null values and values that begin with _
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = "{}";
        // remove any keys that start with underscore character
        entity.remove("_odata_id");
        entity.remove("_odata_type");
        entity.remove("_id");

        nullWriteOnlyValues(entity);
        try {
            json = mapper.writeValueAsString(entity);
        } catch (Exception ignored) {
        }

        // here if the request is valid - convert the POJO to a clean JSON string and return the results
        // within the body of the HTTP response.
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(json);
    }

    // onPostCheckForExistence()
    //
    // This method is called during an HTTP post request after initial payload has been validated against the schema.
    // It can be assumed that the payload has all required fields for onCreate, but other required fields may be missing.
    // This method checks to see if any other instances of this object exist in the database.
    // If a duplicate is found, the method returns a RedfishError indicating that a duplicate record has been found
    // otherwise, null is returned.
    //
    // The default behavior of this function is to return null.  Objects that extend this class should perform
    // whatever behavior is appropriate for their class.
    //
    // parameters:
    //    RedfishObject obj -- the object to be posted
    //    HttpServletRequest request -- the post request that was received
    //    CachedSchema schema -- the related schema object for the posted data
    //
    // returns:
    //    RedfishError describing the issue, otherwise null
    //
    protected RedfishError onPostCheckForExistence(RedfishObject obj, HttpServletRequest request, CachedSchema schema) {
        return null;
    }

    // onPostCompleteMissingFields()
    //
    // This method is called during an HTTP post request after initial payload has been validated against the schema.
    // It can be assumed that the payload has all required fields for onCreate, but other required fields may be missing.
    // This method completes field data for any required fields and the updated redfish object is returned.
    //
    // The default behavior of this function is to complete the @odata.id, id, and Name fields.  @odata.type has
    // already been completed. Objects that extend this class should update any other required fields.
    //
    // parameters:
    //    RedfishObject obj -- the object to be posted
    //    HttpServletRequest request -- the post request that was received
    //    CachedSchema schema -- the related schema object for the posted data
    //
    // returns:
    //    RedfishObject with updated fields
    //
    protected RedfishObject onPostCompleteMissingFields(RedfishObject obj, HttpServletRequest request, CachedSchema schema) {
        RedfishObject result = new RedfishObject();
        result.putAll(obj);

        // Always overwrite the @odata.id and Id fields with a unique identifier
        String uuid = UUID.randomUUID().toString();
        String uri = request.getRequestURI();
        if (!uri.endsWith("/Members")) {
            uri = uri.replace("/Members","");
        }
        result.setAtOdataId(uri+"/"+uuid);
        result.setId(uuid);

        if ((result.getName()==null) || (result.getName().isEmpty())) {
            String name = result.getAtOdataType().replace("#","").split("\\.")[0];
            name = name + " " + uuid;
            result.setName(name);
        }
        return result;
    }

    // onPostCreationChecks()
    //
    // This method checks the validity of the provided data during a POST operation.
    // It can be assumed that the payload has all required fields populated.
    // This method checks field values against service requirements to make sure the object is valid.
    //
    // The default behavior of this is to return null (no error). Objects that extend this class should
    // override this behavior to meet the needs of their specific object type.
    //
    // parameters:
    //    RedfishObject obj -- the object to be posted
    //    HttpServletRequest request -- the post request that was received
    //    CachedSchema schema -- the related schema object for the posted data
    //
    // returns:
    //    RedfishError if errors are found, otherwise null
    //
    protected RedfishError onPostCreationChecks(RedfishObject ignoredObj, HttpServletRequest ignoredRequest, CachedSchema ignoredSchema) {
        return null;
    }

    protected void onPostAfterCreation(RedfishObject ignoredObj, HttpServletRequest ignoredRequest, CachedSchema ignoredSchema) {
    }

    @PostMapping()
    // The function body will only be entered if the user has proper permissions for
    // posting and the provided redfish object meets the minimum criteria for creation
    // as determined by validation against its schema.
    // NOTE: POST is only supported for resource collections.
    public ResponseEntity<?> post(@ValidRedfishObject RedfishObject obj, HttpServletRequest request) {
        // the privilege table maps all acceptable URIs to their Entities and Privileges.  Use this lookup to
        // find the associated entity type for this request.
        String schemaClass = privilegeTableService.getEntityTypeFromUri(request.getRequestURI().replace("/Members",""));
        assert schemaClass != null; // this will happen if there is no entry for the class in the privilege table

        // return an error if the record is not insertable
        if (!schemaService.isInsertable(schemaClass)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("");
        }

        // get the schema from the object - the odata id field should hold the value
        // odata.type will be of the form of either #TYPE.version.TYPE, or #TYPE.TYPE
        String odatatype = obj.getAtOdataType();
        String type = odatatype.replace("#","").split("\\.")[0];
        String source = odatatype.replace("#","");
        source = source.substring(0,source.length()-type.length());
        source = source + "json";

        // get the schema associated with the source
        CachedSchema schema = schemaService.getFromSource(source);

        // check to see if the resource already exists.  If so, send an error response
        RedfishError error = onPostCheckForExistence(obj,request, schema);
        if (error!=null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        // check for any object-specific issues that might exist
        error = onPostCreationChecks(obj, request, schema);
        if (error!=null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        // update any missing fields
        RedfishObject updatedObj = onPostCompleteMissingFields(obj, request, schema);

        // set the _odata_type and _odata_id fields prior to writing.
        updatedObj.put("_odata_type",updatedObj.getAtOdataType().replace("#","").split("\\.")[0]);
        updatedObj.put("_odata_id",updatedObj.getAtOdataId());

        objectRepository.save(updatedObj);

        // perform any tasks that must be completed after posting the new object
        onPostAfterCreation(updatedObj, request, schema);

        // remove null values and values that begin with _
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = "{}";
        // remove any keys that start with underscore character
        updatedObj.remove("_odata_id");
        updatedObj.remove("_odata_type");
        updatedObj.remove("_id");
        try {
            json = mapper.writeValueAsString(updatedObj);
        } catch (Exception ignored) {
        }
        String uri = updatedObj.getAtOdataId();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Location", uri);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .headers(responseHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .body(json);
    }

    // onPatchCreationChecks()
    //
    // This method checks the validity of the modified object for a PATCH operation.
    //
    // The default behavior of this is to return null (no error). Objects that extend this class should
    // override this behavior to meet the needs of their specific object type.
    //
    // parameters:
    //    RedfishObject obj -- the object to be written back
    //    HttpServletRequest request -- the post request that was received
    //
    // returns:
    //    RedfishError if errors are found, otherwise null
    //
    protected List<RedfishError> onPatchPreWriteChecks(RedfishObject ignoredObj, HttpServletRequest ignoredRequest) {
        return null;
    }

    protected void onPatchPostWrite(RedfishObject ignoredObj, HttpServletRequest ignoredRequest) {
    }
    @PatchMapping()
    public ResponseEntity<?> patch(@ValidRedfishObject RedfishObject obj, HttpServletRequest request) {
        // assume that at this point the payload has already been validated against the schema related to this url
        // load the database entry to be patched
        String uri = request.getRequestURI();
        RedfishObject entity = objectRepository.findFirstWithQuery(Criteria
                .where("_odata_id").is(uri));

        if (entity == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }

        // step 1: remove read-only fields from the requested changes, creating errors as we go
        List<RedfishError> result = removeReadOnlyValues(obj);

        // check to see if there are any fields left in the requested changes
        if (obj.isEmpty()) {
            // If all properties in the update request are read-only, unknown, or unsupported, but the resource can be
            // updated, the service shall return the HTTP 400 Bad Request status code and an error response with
            // messages that show the non-updatable properties.
            RedfishError errResult = redfishErrorResponseService.getErrorMessage("Base","GeneralError", new ArrayList<>(), new ArrayList<>());
            errResult.getError().getAtMessageExtendedInfo().clear();
            for (RedfishError err: result) {
                errResult.getError().getAtMessageExtendedInfo().addAll(err.getError().getAtMessageExtendedInfo());
            }
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errResult);
        }

        // there are still non-null fields left to change - make the changes to the payload
        entity.update(obj);

        // perform class-specific checks before writing the changes
        List<RedfishError> additionalErrors = onPatchPreWriteChecks(entity, request);
        if ((additionalErrors != null) && (additionalErrors.isEmpty())) {
            RedfishError errResult = redfishErrorResponseService.getErrorMessage("Base","GeneralError", new ArrayList<>(), new ArrayList<>());
            errResult.getError().getAtMessageExtendedInfo().clear();
            for (RedfishError err: result) {
                errResult.getError().getAtMessageExtendedInfo().addAll(err.getError().getAtMessageExtendedInfo());
            }
            for (RedfishError err: additionalErrors) {
                errResult.getError().getAtMessageExtendedInfo().addAll(err.getError().getAtMessageExtendedInfo());
            }
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errResult);
        }

        // write back the changes
        objectRepository.update(entity);

        // perform any activities after patch has occurred
        onPatchPostWrite(entity, request);

        // return the result with the proper response code
        if (!result.isEmpty()) {
            entity.put("@Message.ExtendedInfo:",result);
        }
        // remove null values and values that begin with _
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = "{}";
        // remove any keys that start with underscore character
        entity.remove("_odata_id");
        entity.remove("_odata_type");
        entity.remove("_id");
        try {
            json = mapper.writeValueAsString(entity);
        } catch (Exception ignored) {
        }
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(json);
    }

    protected void onDeleteRemoveSubordinates(RedfishObject ignoredObject, HttpServletRequest ignoredRequest) {
    }

    @DeleteMapping()
    public ResponseEntity<?> delete(HttpServletRequest request) {
        String uri = request.getRequestURI();

        // the privilege table maps all acceptable URIs to their Entities and Privileges.  Use this lookup to
        // find the associated entity type for this request.
        String schemaClass = privilegeTableService.getEntityTypeFromUri(uri);
        assert schemaClass != null; // this will happen if there is no entry for the class in the privilege table

        // return an error if the record is not deletable
        if (!schemaService.isDeletable(schemaClass)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("");
        }

        RedfishObject entity = objectRepository.findFirstWithQuery(Criteria
                .where("_odata_id").is(uri));
        if (entity==null) {
            // return 404 error - object not found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }

        // attempt to delete any subordinates first
        onDeleteRemoveSubordinates(entity, request);

        // now remove the entity
        objectRepository.delete(entity);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = "{}";
        // remove any keys that start with underscore character
        entity.remove("_odata_id");
        entity.remove("_odata_type");
        entity.remove("_id");

        // Replace any write-only content with nulls
        nullWriteOnlyValues(entity);

        try {
            json = mapper.writeValueAsString(entity);
        } catch (Exception ignored) {
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(json);
    }
}


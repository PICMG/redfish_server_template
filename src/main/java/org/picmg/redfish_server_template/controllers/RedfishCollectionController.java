package org.picmg.redfish_server_template.controllers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.json.JsonObject;
import org.picmg.redfish_server_template.RFmodels.AllModels.Odata_IdRef;
import org.picmg.redfish_server_template.RFmodels.AllModels.RedfishError;
import org.picmg.redfish_server_template.RFmodels.custom.RedfishCollection;
import org.picmg.redfish_server_template.RFmodels.custom.RedfishObject;
import org.picmg.redfish_server_template.data_validation.ValidRedfishObject;
import org.picmg.redfish_server_template.repository.RedfishCollectionRepository;
import org.picmg.redfish_server_template.repository.RedfishObjectRepository;
import org.picmg.redfish_server_template.services.Helpers;
import org.picmg.redfish_server_template.services.RedfishErrorResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// a generic for all redfish collection controllers.  T is the odata type for the collection itself.
// U is the type for the entities within the collection and V is the Type for the collection repository
class RedfishCollectionController<T extends RedfishCollection, V extends RedfishCollectionRepository<T>, W extends RedfishObject, X extends RedfishObjectRepository<W>> {

    @Autowired
    V collectionRepository;

    @Autowired
    X objectRepository;

    @Autowired
    RedfishErrorResponseService redfishErrorResponseService;

    // return the collection page.  This function
    @GetMapping(value="")
    public ResponseEntity<?> get(HttpServletRequest request) {
        // get the database entry associated with this path
        String uri = request.getRequestURI();
        T entity = collectionRepository.getFirstByUri(uri);

        // Check parameters for unknown or unsupported parameters
        Map<String,String[]> parameters = request.getParameterMap();
        ArrayList<String> unknownParameters = new ArrayList<String>();
        int top = entity.getMembers().size();
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
                            top = Integer.parseInt(parameter.getValue()[0]);
                            if (top > entity.getMembers().size())
                                top = entity.getMembers().size();
                        }
                        if (key.equals("$skip")) {
                            skip = Integer.parseInt(parameter.getValue()[0]);
                            if (skip > entity.getMembers().size())
                                skip = entity.getMembers().size();
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
                    "Base","QueryNotSupported",new ArrayList<String>(), new ArrayList<String>());
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(error);
        }

        if ((parameters.containsKey("only"))&&(parameters.entrySet().size()>1)) {
            // REQ: Services should return the HTTP 400 Bad Request with the QueryCombinationInvalid message from the
            // base message registry if 'only' is being combined with other query parameters.
            RedfishError error = redfishErrorResponseService.getErrorMessage(
                    "Base","QueryCombinationInvalid",new ArrayList<String>(), new ArrayList<String>());
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(error);
        }

        // Check for special case - there is only one member left, and only is specified
        if ((entity.getMembersAtOdataCount() == 1) && (parameters.containsKey("only"))) {
            // Parse the URI to determine if it is local or remote
            String refUri = entity.getMembers().get(0).getAtOdataId();

            // attempt to get the resource from the related database
            W refObject = objectRepository.getFirstByUri(refUri);
            if (refObject != null) {
                // object found in local repository.
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(
                        Helpers.createJsonStringFromObject(refObject));
            }

            // if the reference is for an external entity, return the entity
            return Helpers.getExternalResourceFromUri(request, refUri);
        }

        // REQ: Services shall process query parameters in this order: $filter, $skip, $top,
        // apply server-side pagination, $expand, excerpt, $select
        List<Odata_IdRef> members = entity.getMembers();
        if (skip+top<members.size()) {
            entity.setMembersAtOdataNextLink(uri+"?$skip="+Integer.toString(skip+top));
        } else {
            entity.setMembersAtOdataNextLink(null);
        }
        // skip the first n members in the collection
        for (int i=0;i<skip;i++) {
            if (!members.isEmpty()) members.remove(0);
        }
        // show only the top m members in the collection
        while (members.size()>top) members.remove(top);

        // here if the request is valid - convert the POJO to a clean JSON string and return the results
        // within the body of the HTTP response.
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(
                Helpers.createJsonStringFromObject(entity));
    }

    @PostMapping()
    public ResponseEntity<?> post(@ValidRedfishObject("unused") W obj, HttpServletRequest request) {
        // phase 1, attempt to create new object from the body of the request

        // phase 2, post-creation checks - these are object dependent.  For instance,
        // do resources pointed to by links in the object exist?

        // phase 3, Assign service-defined fields

        // phase 4, Add the object to the database and update the collection

        return null;
    }

    @PatchMapping()
    public ResponseEntity<?> patch(HttpServletRequest request) {
        return null;
    }

    @DeleteMapping()
    public ResponseEntity<?> delete(HttpServletRequest request) {
        return null;
    }
}


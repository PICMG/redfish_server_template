//******************************************************************************************************
// EventController.java
//
// Controller for Event service.
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

package org.picmg.redfish_server_template.controllers;

import org.bson.Document;
import org.picmg.redfish_server_template.RFmodels.Autogenerated.RedfishError;
import org.picmg.redfish_server_template.RFmodels.custom.CachedSchema;
import org.picmg.redfish_server_template.RFmodels.custom.RedfishCollection;
import org.picmg.redfish_server_template.RFmodels.custom.RedfishObject;
import org.picmg.redfish_server_template.data_validation.ValidRedfishObject;
import org.picmg.redfish_server_template.services.EventService;
import org.picmg.redfish_server_template.services.QueryParameterService;
import org.picmg.redfish_server_template.services.RedfishErrorResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.*;

@Component
@RequestMapping("/redfish/v1/EventService/Subscriptions")
public class EventDestinationCollectionController extends RedfishObjectController {
    @Autowired
    EventService eventService;

    @Autowired
    RedfishErrorResponseService redfishErrorResponseService;

    public boolean createSSEEventDestination(SseEmitter emitter, HttpServletRequest request, TreeMap<String,ArrayList<String>> filterMap) {
        RedfishObject result = new RedfishObject();

        // Always overwrite the @odata.id and Id fields with a unique identifier
        String uuid = UUID.randomUUID().toString();
        String uri = request.getRequestURI();
        uri = uri.substring(0,uri.indexOf("/SSE"));

        CachedSchema schema = schemaService.getNewestVersionFromType("EventDestination");
        String type = schema.getSource();

        type = "#" + type.substring(0,type.indexOf(".json")) + ".EventDestination";
        result.setAtOdataType(type);
        result.setAtOdataContext("/redfish/v1/$metadata#EventDestination.EventDestination");
        result.setName("SSE Event Destination " + uuid);

        // complete all the missing fields
        result = onPostCompleteMissingFields(result, request, schema);

        // fix the odataid
        result.setAtOdataId(uri+"/Subscriptions/"+uuid);
        result.setId(uuid);

        // fix the actions (if any)
        // Additionally, the supported actions for this event destination will also be created (overriding any that exist).
        if (result.containsKey("Actions")) {
            Map<String,Object> actions = (Map<String,Object>)result.get("Actions");
            for (String action: actions.keySet()) {
                Map<String,Object> target = (Map<String,Object>)actions.get(action);
                if (target.containsKey("@odata.id")) {
                    String id = (String)target.get("@odata.id");
                    id = result.getAtOdataId()+ id.substring(id.indexOf("/Actions"));
                    target.put("@odata.id",id);
                }
            }
        }

        result.put("Destination","redfish-sse://"+request.getRemoteAddr()+":"+request.getRemotePort());

        if (filterMap.containsKey("MessageId")) {
            result.put("MessageIds", new ArrayList<String>(filterMap.get("MessageId")));
        }

        if (filterMap.containsKey("OriginResource")) {
            List<Object> resourceObjects = new ArrayList<>();
            for (String resourceUri: filterMap.get("OriginResource")) {
                resourceObjects.add(Collections.singletonMap("@odata.id",resourceUri));
            }
            result.put("OriginResources",resourceObjects);
        }

        if (filterMap.containsKey("RegistryPrefix")) {
            result.put("RegistryPrefixes", new ArrayList<String>(filterMap.get("RegistryPrefix")));
        }

        if (filterMap.containsKey("ResourceType")) {
            result.put("ResourceTypes", new ArrayList<String>(filterMap.get("ResourceType")));
        }

        result.put("SubscriptionType","SSE");

        // check the result
        if (onPostCreationChecks(result, request, schema)!= null) {
            return false;
        }

        // add the new destination to the object repository
        objectRepository.save(result);

        // add the new object to the event service
        eventService.addSubscription(result, emitter);
        return true;
    }

    /* @GetMapping("") - handled by default behavior */

    // onPostCompleteMissingFields()
    //
    // This method is called during an HTTP post request after initial payload has been validated against the schema.
    // It can be assumed that the payload has all required fields for onCreate, but other required fields may be missing.
    // This method completes field data for any required fields and the updated redfish object is returned.
    //
    // The default behavior of this function is to complete the @odata.id, Id, and Name fields.  @odata.type has
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
    @Override
    protected RedfishObject onPostCompleteMissingFields(RedfishObject obj, HttpServletRequest request, CachedSchema schema) {
        RedfishObject result = super.onPostCompleteMissingFields(obj, request, schema);

        // if not specified, the Context must be created - technically this should be supplied by the client
        if (!result.containsKey("Context")) {
            result.put("Context", "Generic event, unspecified context");
        }

        // Additionally, the supported actions for this event destination will also be created (overriding any that exist).
        result.put("Actions", new HashMap<>(Map.of(
                "#EventDestination.ResumeSubscription",
                new HashMap<>(Collections.singletonMap("@odata.id", result.get("@odata.id") + "/Actions/EventDestination.ResumeSubscription")),
                "#EventDestination.SuspendSubscription",
                new HashMap<>(Collections.singletonMap("@odata.id", result.get("@odata.id") + "/Actions/EventDestination.SuspendSubscription"))))
        );

        // state
        result.put("State", new HashMap<>(Map.of("Status","Enabled")));

        // if no event format type is specified, use "Event"
        if (!result.containsKey("EventFormatType")) {
            result.put("EventFormatType", "Event");
        }
        return result;
    }

    // onPostCreationChecks()
    //
    // This method checks the validity of the provided data during a POST operation.
    // It can be assumed that the payload has all required fields populated.
    // This method checks to make sure that all object fields are valid
    //
    // parameters:
    //    RedfishObject obj -- the object to be posted
    //    HttpServletRequest request -- the post request that was received
    //    CachedSchema schema -- the related schema object for the posted data
    //
    // returns:
    //    RedfishError if errors are found, otherwise null
    //
    @Override
    protected RedfishError onPostCreationChecks(RedfishObject obj, HttpServletRequest request, CachedSchema schema) {
        // Check for deprecated values
        if (obj.containsKey("EventType")) {
            return redfishErrorResponseService.getErrorMessage(
                    "Base",
                    "PropertyDeprecated",
                    List.of("EventType"),
                    new ArrayList<>());
        }

        // is the event format type supported?
        if (!eventService.isEventFormatTypeSupported(obj.getString("EventFormatType"))) {
            return redfishErrorResponseService.getErrorMessage(
                    "Base",
                    "PropertyValueIncorrect",
                    List.of("EventFormatType",obj.getString("EventFormatType")),
                    new ArrayList<>());
        }

        // check the registry prefix types to make sure they are all supported
        if (obj.containsKey("RegistryPrefixes")) {
            for (String prefix : obj.getList("RegistryPrefixes",String.class)) {
                if (!eventService.isRegistryPrefixSupported(prefix)) {
                    return redfishErrorResponseService.getErrorMessage(
                            "Base",
                            "PropertyValueIncorrect",
                            List.of("RegistryPrefixes",prefix),
                            new ArrayList<>());
                }
            }
        }

        // check the exclude registry prefix types to make sure they are all supported
        if (obj.containsKey("ExcludeRegistryPrefixes")) {
            for (String prefix : obj.getList("ExcludeRegistryPrefix",String.class)) {
                if (!eventService.isRegistryPrefixSupported(prefix)) {
                    return redfishErrorResponseService.getErrorMessage(
                            "Base",
                            "PropertyValueIncorrect",
                            List.of("ExcludeRegistryPrefixes",prefix),
                            new ArrayList<>());
                }
            }
        }

        // check the Message Ids to make sure they are all supported
        // The MessageId should be in the `MessageRegistryPrefix.MessageKey` format.
        // If included, the MessageId major and minor version details should be ignored.
        if (obj.containsKey("MessageIds")) {
            for (String msgid : obj.getList("MessageIds",String.class)) {
                if (!eventService.isMessageIdValid(msgid)) {
                    return redfishErrorResponseService.getErrorMessage(
                            "Base",
                            "PropertyValueIncorrect",
                            List.of("MessageIds",msgid),
                            new ArrayList<>());
                }
            }
        }

        // check the Exclude Message Ids to make sure they are all supported
        // The MessageId should be in the `MessageRegistryPrefix.MessageKey` format.
        // If included, the MessageId major and minor version details should be ignored.
        if (obj.containsKey("ExcludeMessageIds")) {
            for (String msgid : obj.getList("ExcludeMessageIds",String.class)) {
                if (!eventService.isMessageIdValid(msgid)) {
                    return redfishErrorResponseService.getErrorMessage(
                            "Base",
                            "PropertyValueIncorrect",
                            List.of("ExcludeMessageIds",msgid),
                            new ArrayList<>());
                }
            }
        }

        // check the OriginResources to make sure they are all valid
        if (obj.containsKey("OriginResources")) {
            for (Document doc : obj.getList("ExcludeMessageIds",Document.class)) {
                if (doc.containsKey("@odata.id")) {
                    // see if the resource exists
                    String odataId = doc.getString("@odata.id");
                    if (objectRepository.findFirstWithQuery(Criteria.where("_odata_id").is(odataId))==null) {
                        return redfishErrorResponseService.getErrorMessage(
                                "Base",
                                "ResourceMissingAtUri",
                                List.of(odataId),
                                new ArrayList<>());
                    }
                }
            }
        }

        // Check the Resource Types to make sure they are valid
        if (obj.containsKey("ResourceTypes")) {
            for (String type : obj.getList("ResourceTypes",String.class)) {
                if (!eventService.isResourceTypeSupported(type)) {
                    return redfishErrorResponseService.getErrorMessage(
                            "Base",
                            "PropertyValueIncorrect",
                            List.of("ResourceTypes",type),
                            new ArrayList<>());
                }
            }
        }

        // check the exclude resource types to make sure they are all supported
        if (obj.containsKey("ExcludeResourceTypes")) {
            for (String type : obj.getList("ExcludeResourceTypes",String.class)) {
                if (!eventService.isResourceTypeSupported(type)) {
                    return redfishErrorResponseService.getErrorMessage(
                            "Base",
                            "PropertyValueIncorrect",
                            List.of("ExcludeResourceTypes",type),
                            new ArrayList<>());
                }
            }
        }

        // Check the Severities to make sure they are supported
        if (obj.containsKey("Severities")) {
            for (String severity : obj.getList("Severities",String.class)) {
                if (!eventService.isSeveritySupported(severity)) {
                    return redfishErrorResponseService.getErrorMessage(
                            "Base",
                            "PropertyValueIncorrect",
                            List.of("ResourceTypes",severity),
                            new ArrayList<>());
                }
            }
        }
        return null;
    }

    // onPostAfterCreation()
    //
    // This method checks is called after the event destination has been created - here, the event service is
    // invoked to create the new event subscription.
    //
    // parameters:
    //    RedfishObject obj -- the eventdestination object
    //    HttpServletRequest request -- the post request that was received
    //    CachedSchema schema -- the related schema object for the posted data
    //
    // returns:
    //    RedfishError if errors are found, otherwise null
    //
    @Override
    protected void onPostAfterCreation(RedfishObject obj, HttpServletRequest ignoredRequest, CachedSchema ignoredSchema) {
        //eventService.createSubscription(obj);
    }

    @Override
    @PostMapping(value = {""})
    public ResponseEntity<?> post(@ValidRedfishObject RedfishObject obj, HttpServletRequest request) {
        return super.post(obj, request);
    }
}
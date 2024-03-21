//******************************************************************************************************
// AccountController.java
//
// Controller for account service.
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

import org.picmg.redfish_server_template.RFmodels.custom.RedfishObject;
import org.picmg.redfish_server_template.repository.RedfishObjectRepository;
import org.picmg.redfish_server_template.services.EventService;
import org.picmg.redfish_server_template.services.LogService;
import org.picmg.redfish_server_template.services.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;


@RestController
@RequestMapping(value = {
        "/redfish/v1/EventService/Actions/*"
})
public class EventServiceActionController extends RedfishObjectController {
    @Autowired
    RedfishObjectRepository objectRepository;

    @Autowired
    EventService eventService;

    // actionAsyncHandler
    // This method is called by the task service to handle the particular action.  Default behavior does nothing.
    //
    // parameters - the parameters passed to the action from the caller.
    // uri - the uri of the action call
    // request - the complete request that invoked the action
    // taskId - the taskId that will be associated with this action if it is not completed quickly
    // taskService - the task service that invoked this method call.
    //
    // returns - a response to be returned to the calling client
    @Override
    public ResponseEntity<?> actionAsyncHandler(RedfishObject parameters, String uri, HttpServletRequest request, String taskId, TaskService taskService) {
        // get the action type
        String action = uri.substring(uri.lastIndexOf('.')+1);
        if (action.equals("SubmitTestEvent")) {
            // Create an event object with the provided parameters
            String messageId = parameters.getString("MessageId");
            String msgSeverity = null;
            if (parameters.containsKey("MessageSeverity")) msgSeverity = parameters.getString("MessageSeverity");
            String severity = null;
            if (parameters.containsKey("Severity")) severity = parameters.getString("Severity");
            String originOfCondition = null;
            if (parameters.containsKey("OriginOfCondition")) originOfCondition = parameters.getString("OriginOfCondition");
            ArrayList<String> args = null;
            if (parameters.containsKey("MessageArgs")) args = (ArrayList<String>)parameters.get("MessageArgs");
            RedfishObject event = eventService.createEvent(messageId, args, msgSeverity, severity, originOfCondition);
            ArrayList<HashMap<String,Object>> records = (ArrayList<HashMap<String,Object>>)event.get("Events");
            HashMap<String,Object> eventRecord = records.get(0);
            if (parameters.containsKey("Message"))
                eventRecord.put("Message", parameters.get("Message"));
            if (parameters.containsKey("EventTimestamp"))
                eventRecord.put("EventTimestamp", parameters.get("EventTimstamp"));
            if (parameters.containsKey("EventId"))
                eventRecord.put("EventId", parameters.get("EventId"));
            if (parameters.containsKey("EventGroupId"))
                eventRecord.put("EventGroupId", parameters.get("EventGroupId"));
            if (parameters.containsKey("Message"))
                eventRecord.put("Message", parameters.get("Message"));
            if (parameters.containsKey("EventTimestamp"))
                eventRecord.put("EventTimestamp", parameters.get("EventTimstamp"));
            if (parameters.containsKey("EventId"))
                eventRecord.put("EventId", parameters.get("EventId"));
            if (parameters.containsKey("EventGroupId"))
                eventRecord.put("EventGroupId", parameters.get("EventGroupId"));
            eventService.sendEvent(event);
            return ResponseEntity.created(null).contentType(MediaType.APPLICATION_JSON).body(event.toJson());
        }
        if (action.equals("TestEventSubscription")) {
            // Create an event object with the provided parameters
            String messageId = "ResourceEvent.TestMessage";
            String msgSeverity = ("OK");
            String severity = "OK";
            String originOfCondition = null;
            ArrayList<String> args = null;
            RedfishObject event = eventService.createEvent(messageId, args, msgSeverity, severity, originOfCondition);
            eventService.sendEvent(event);
            return ResponseEntity.created(null).contentType(MediaType.APPLICATION_JSON).body(event.toJson());
        }

        return ResponseEntity.badRequest().body("{}");
    }
}
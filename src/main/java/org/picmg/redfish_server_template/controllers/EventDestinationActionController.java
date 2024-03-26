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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;


@RestController
@RequestMapping(value = {"/redfish/v1/EventService/Subscriptions/{SubscriptionId}/Actions/*"})
public class EventDestinationActionController extends RedfishObjectController {
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
        if (action.equals("SuspendSubscription")) {
            eventService.suspendSubscription(uri.substring(0, uri.indexOf("/Actions")));
            return ResponseEntity.ok().body("{}");
        } else if (action.equals("ResumeSubscription")) {
            eventService.resumeSubscription(uri.substring(0, uri.indexOf("/Actions")));
            return ResponseEntity.ok().body("{}");
        }

        return ResponseEntity.badRequest().body("{}");
    }
}
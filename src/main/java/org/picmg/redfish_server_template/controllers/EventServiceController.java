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

import org.picmg.redfish_server_template.RFmodels.custom.RedfishObject;
import org.picmg.redfish_server_template.services.EventService;
import org.picmg.redfish_server_template.services.QueryParameterService;
import org.picmg.redfish_server_template.services.RedfishErrorResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;


@RestController
@RequestMapping("/redfish/v1/EventService")
public class EventServiceController extends RedfishObjectController {
    @Autowired
    EventService eventService;

    @Autowired
    EventDestinationCollectionController eventDestinationCollectionController;

    @CrossOrigin
    @RequestMapping(value = "/SSE", method = RequestMethod.GET, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter beginSseConnection(HttpServletRequest request) {
        Map<String,String[]> parameters = request.getParameterMap();

        // verify get a list of the requested filters
        TreeMap<String, ArrayList<String>> filterSet = null;
        if (parameters.containsKey("filter")) {
            filterSet = eventService.evaluateSSEFilter(parameters.get("filter")[0]);
        }
        if (filterSet==null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        // create a destination object for this event
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        if (eventDestinationCollectionController.createSSEEventDestination(emitter, request, filterSet)) return emitter;
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }
}

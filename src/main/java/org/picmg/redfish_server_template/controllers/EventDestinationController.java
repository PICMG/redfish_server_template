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
import org.picmg.redfish_server_template.services.RedfishErrorResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Component
@RequestMapping("/redfish/v1/EventService/Subscriptions/*")
public class EventDestinationController extends RedfishObjectController {
    @Autowired
    EventService eventService;

    @Autowired
    RedfishErrorResponseService redfishErrorResponseService;

    protected void onDeleteRemoveSubordinates(RedfishObject resource, HttpServletRequest ignoredRequest) {
        // this function has been called because the resource has already been deleted - all subordinate
        // resources should also be removed.
        eventService.closeSubscription(resource.getAtOdataId());
    }

}

//******************************************************************************************************
// TasksController.java
//
// Controller for Task service.
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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.picmg.redfish_server_template.RFmodels.custom.RedfishObject;
import org.picmg.redfish_server_template.services.QueryParameterService;
import org.picmg.redfish_server_template.services.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;


@RestController
@RequestMapping(value = "/redfish/v1/TaskService/TaskMonitors/**")
public class TaskMonitorController extends RedfishObjectController {
    @Autowired
    TaskService taskService;

    // get - use the default get behavior
    // before a task is complete - return a blank body and a 202 response
    // after a task is complete - return the body, response code as if the task had completed synchronously
    // this is accomplished by the task itself.  When the asynchronous task completes, it will update the task state


    /* No POST */

    /* No PATCH */

    // special delete behavior:
    // if the associated task is
    //   Canceling - do nothing, return ACCEPTED with the representation of the task monitor
    //   Canceled - This should not occur - the Task service will delete the task and the task
    //              monitor once the service cancels.
    //   All other states - send cancellation request to the task, and return ACCEPTED with the
    //              representation of the task monitor.
    //
    @DeleteMapping()
    @Override
    public ResponseEntity<?> delete(HttpServletRequest request) {
        String uri = request.getRequestURI();

        RedfishObject entity = objectRepository.findFirstWithQuery(Criteria
                .where("_odata_id").is(uri));
        if (entity==null) {
            // return 404 error - object not found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }

        //taskService.cancelTaskFromTaskMonitor(uri);

        // otherwise, return 202
        return ResponseEntity
                .status(HttpStatus.ACCEPTED).contentType(MediaType.APPLICATION_JSON).body("{}");
    }
}

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

import org.picmg.redfish_server_template.services.QueryParameterService;
import org.picmg.redfish_server_template.services.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.persistence.EntityExistsException;
import java.util.Collections;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/redfish/v1/TaskService")
public class TaskController extends RedfishObjectController {

    String controllerEntityName = "TaskService";
    @Autowired
    TaskService taskService;

    @Autowired
    QueryParameterService queryParameterService;
/*
    @GetMapping("/Tasks")
    public ResponseEntity<?> getAll(@RequestHeader String authorization, @RequestParam Map<String, String> params) throws Exception {
        if(params.isEmpty())
            return ResponseEntity.ok().body(taskService.getAllTaskDetails());
        List<Task_Task> objectList = taskService.getAllTaskDetails();
        if(params.containsKey("$top"))
            return ResponseEntity.ok().body(queryParameterService.getTopQueryParameterResult(Collections.singletonList(objectList), Integer.parseInt(params.get("$top"))));
        if(params.containsKey("$skip"))
            return ResponseEntity.ok().body(queryParameterService.getSkippedQueryParameterResult(Collections.singletonList(objectList), Integer.parseInt(params.get("$skip"))));
        if(params.containsKey("only")) {
            if(objectList.size()==1)
                return ResponseEntity.ok().body(objectList.get(0));
            return ResponseEntity.ok().body(objectList);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Querying is not supported on the requested resource.");
    }

    @GetMapping("/Tasks/{Id}")
    public ResponseEntity<?> getById(@RequestHeader String authorization, @PathVariable String Id) throws Exception {
        Task_Task task = taskService.getTask(Id);
        if(task == null) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Request succeeded, but no content is being returned in the body of the response.");
        }
        return ResponseEntity.ok().body(task);
    }


    @GetMapping("/Tasks/{Id}/monitor")
    public ResponseEntity<?> getMonitorById(@RequestHeader String authorization, @PathVariable String Id) throws Exception {
        TaskMonitor taskMonitor = null;
        try {
            taskMonitor = taskService.getTaskMonitor(Id);
        } catch (ChangeSetPersister.NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }
        if(taskMonitor.getTaskResponse()==null)
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(taskMonitor);
        return ResponseEntity.status(HttpStatus.OK).body(taskMonitor);
    }

    @RequestMapping(value = "/Tasks", method= RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addTask(@RequestBody Task_Task task, @RequestHeader String authorization) throws Exception {
        try {
            task = taskService.addTask(task);
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Location", task.getAtOdataId());
            return ResponseEntity.status(HttpStatus.CREATED).headers(responseHeaders).body(task);
        } catch (EntityExistsException e) {
            ResponseEntity.status(HttpStatus.CONFLICT).body("Creation or update request could not be completed because it would cause a conflict in the");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Request could not be processed because it contains invalid information");
    }

    @RequestMapping(value="/Tasks", method=RequestMethod.DELETE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteTask(@RequestBody Task_Task task, @RequestHeader String authorization) throws Exception {
        if(taskService.deleteTask(task))
            return ResponseEntity.ok(task);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(String.format("The requested resource of type '%1' named %2 was not found.", "Session", task.getName()));
    }

    @RequestMapping(value="/Tasks", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> updateTask(@RequestBody Task_Task task, @RequestHeader String authorization) throws Exception {
        if(taskService.updateTask(task, null))
            return ResponseEntity.ok("The request completed successfully");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Request could not be processed because it contains invalid information");
    }

 */
}

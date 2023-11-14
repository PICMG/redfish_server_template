//******************************************************************************************************
// TaskService.java
//
// Task service according to redfish specification.
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

import org.picmg.redfish_server_template.RFmodels.AllModels.ResourceHealth;
import org.picmg.redfish_server_template.RFmodels.custom.RedfishObject;
import org.picmg.redfish_server_template.RFmodels.AllModels.Task_TaskState;
import org.picmg.redfish_server_template.repository.RedfishObjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    @Autowired
    RedfishObjectRepository objectRepository;

    @Autowired
    RedfishErrorResponseService redfishErrorResponseService;

    public TaskService() {

    }

/*
    public List<Task_Task> getAllTaskDetails(){
        List<Task_Task> taskList = taskRepository.findAll();
        return taskList;
    }
*/
    public RedfishObject getTask(String Id) {
        List<RedfishObject> taskList = objectRepository.findWithQuery(
                Criteria.where("_odata_type").is("Task")
                        .and("Id").is(Id));
        if (taskList.size()!=1) {
            return null;
        }
        return taskList.get(0);
    }
/*
    public TaskMonitor getTaskMonitor(String Id) throws ChangeSetPersister.NotFoundException {
        Task_Task task = taskRepository.getById(Id);
        if(task == null || task.getTaskMonitor() == null)
            throw new ChangeSetPersister.NotFoundException();
        return task.getTaskMonitor();
    }

    public Task_Task addTask(Task_Task task) throws EntityExistsException {
        Task_Task newTask = taskRepository.getByName(task.getName());
        if(newTask != null)
            throw new EntityExistsException();
        Integer newTaskId = getMaxTaskCount();
        task.setId(newTaskId.toString());
        task.setAtOdataId("/redfish/v1/TaskService/Tasks/" + newTaskId.toString());
        task.setAtOdataType("#Task.v1_6_1.Task");
        task = taskRepository.save(task);
        return task;
    }

    public Boolean deleteTask(Task_Task task) {
        Task_Task existingTask = taskRepository.getByName(task.getName());
        if(existingTask == null)
            return false;
        taskRepository.delete(existingTask);
        return true;
    }
*/
    public void updateTaskState(String id, Task_TaskState taskState, Object asyncResponse) throws Exception {
        RedfishObject task = new RedfishObject();
        task.setId(id);
        task.put("TaskState",taskState);
        if(taskState == Task_TaskState.COMPLETED)
            task.put("EndTime",OffsetDateTime.now());
        updateTask(task, asyncResponse);
    }

    public Boolean updateTask(RedfishObject task, Object asyncResponse) throws Exception {
        List<RedfishObject> taskList = objectRepository.findWithQuery(
                Criteria.where("_odata_type").is("Task")
                        .and("Id").is(task.getId()));
        if (taskList.size()!=1) {
            throw new Exception("Resource Not Found");
        }
        RedfishObject existingTask = taskList.get(0);
        if(existingTask == null)
            return false;
        if(task.getName() != null)
            existingTask.setName(task.getName());
        if(task.containsKey("TaskStatus") && task.get("TaskStatus")!= null)
            existingTask.put("TaskStatus", task.get("TaskStatus"));
        if(task.containsKey("TaskState") && task.get("TaskState")!= null)
            existingTask.put("TaskState", task.get("TaskState"));
        if(task.containsKey("StartTime") && task.get("StartTime")!= null)
            existingTask.put("StartTime", task.get("StartTime"));
        if(task.containsKey("EndTime") && task.get("EndTime")!= null)
            existingTask.put("EndTime", task.get("EndTime"));
        if(task.getAtOdataType() != null)
            existingTask.setAtOdataType(task.getAtOdataType());
        if(task.getAtOdataId() != null)
            existingTask.setAtOdataId(task.getAtOdataId());
        if(existingTask.containsKey("TaskMonitor") && existingTask.get("TaskMonitor")!=null && asyncResponse!=null) {
            // the task monitor should be a reference.  See if the reference exists
            RedfishObject taskMonitor = objectRepository.findFirstWithQuery(
                    Criteria.where("_odata_type").is("TaskMonitor")
                            .and("Id").is(existingTask.get("TaskMonitor").toString()));
            taskMonitor.put("TaskResponse",asyncResponse);
            taskMonitor.put("TaskState",task.get("TaskState").toString());
            taskMonitor.put("Message",
                redfishErrorResponseService.getErrorMessage(
                        "TaskEvent",
                        "TaskCompletedOK",
                        new ArrayList<>(Arrays.asList(existingTask.getId())),
                        new ArrayList<>()
                ));
            objectRepository.save(taskMonitor);
        }

        objectRepository.save(existingTask);
        return true;
    }

    public RedfishObject createTaskForAsyncOperation(OffsetDateTime startTime) {
        // create a new task
        RedfishObject newTask = new RedfishObject();
        String taskId = UUID.randomUUID().toString();
        newTask.setId(taskId);
        newTask.setName("Task " + taskId);
        newTask.put("TaskState",Task_TaskState.RUNNING);
        newTask.put("StartTime",startTime);
        newTask.put("TaskStatus", ResourceHealth.valueOf("OK"));
        newTask.setAtOdataId("/redfish/v1/TaskService/Tasks/" + taskId);
        newTask.setAtOdataType("#Task.v1_6_1.Task");

        // create a new task monitor associated with this task.
        String taskMonitorUri = "/redfish/v1/TaskService/TaskMonitors/"+ UUID.randomUUID();
        RedfishObject taskMonitor = new RedfishObject();
        taskMonitor.setAtOdataId(taskMonitorUri);
        taskMonitor.setAtOdataType("Task_Monitor");
        taskMonitor.put("TaskResponse", HttpStatus.ACCEPTED);
        taskMonitor.put("TaskState",String.valueOf(Task_TaskState.RUNNING));
        taskMonitor.put("Message",
                redfishErrorResponseService.getErrorMessage(
                        "TaskEvent",
                        "TaskCompletedOK",
                        new ArrayList<>(Arrays.asList(taskId)),
                        new ArrayList<>()
                ));
        newTask.put("TaskMonitor",taskMonitorUri);

        objectRepository.save(taskMonitor);
        objectRepository.save(newTask);
        return newTask;
    }

}
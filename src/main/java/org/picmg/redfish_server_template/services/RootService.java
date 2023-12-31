//******************************************************************************************************
// RootService.java
//
// Root service according to redfish specification.
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

import org.picmg.redfish_server_template.RFmodels.AllModels.*;
import org.picmg.redfish_server_template.RFmodels.custom.MetadataFile;
import org.picmg.redfish_server_template.RFmodels.custom.OdataFile;
import org.picmg.redfish_server_template.repository.MetadataFileRepository;
import org.picmg.redfish_server_template.repository.OdataFileRepository;
import org.picmg.redfish_server_template.repository.RootServiceRepository;
import org.picmg.redfish_server_template.repository.SessionService.SessionCollectionRepository;
import org.picmg.redfish_server_template.repository.SessionService.SessionRepository;
import org.picmg.redfish_server_template.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Future;


@Service
public class RootService {

    @Value("${async.task.wait-time}")
    long taskWaitTime;

    @Autowired
    RootServiceRepository rootServiceRepository;

    @Autowired
    SessionRepository sessionRepository;

    @Autowired
    SessionCollectionRepository sessionCollectionRepository;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    TaskService taskService;

    @Autowired 
    MetadataFileRepository metadataFileRepository;

    @Autowired
    OdataFileRepository odataFileRepository;

    @PostConstruct
    public void abortAllPreviouslyRunningOperations() {
        abortAllPreviouslyRunningTasks();
        clearAllPreviousSessions();
    }

    public void clearAllPreviousSessions() {
        // DEBUG: System.out.println("Clearing all Previous Sessions....");
        List<Session_Session> sessionsList = sessionRepository.findAll();
        if(sessionsList.size()==0)return;
        for (Session_Session session: sessionsList) {
            deleteMemberFromSessionCollection(session.getId());
            deleteSession(session.getId());
        }

    }

    public boolean deleteMemberFromSessionCollection(String id){
        Session_Session session = sessionRepository.findByID(id);
        String member = session.getAtOdataId();
        SessionCollection sessionCollection = sessionCollectionRepository.findByName("Session Collection");
        List<Odata_IdRef> members = sessionCollection.getMembers();
        Odata_IdRef idRef = new Odata_IdRef();
        idRef.atOdataId(member);
        members.remove(idRef);
        sessionCollection.setMembers(members);
        sessionCollection.setMembersAtOdataCount(sessionCollection.getMembersAtOdataCount() - 1);
        sessionCollectionRepository.save(sessionCollection);
        return true;

    }

    public Session_Session deleteSession(String id) {
        Session_Session session = sessionRepository.deleteByID(id);
        return session;
    }

    public void abortAllPreviouslyRunningTasks() {
        // DEBUG: System.out.println("Aborting all Previous Running Tasks....");
        List<Task_Task> taskList = taskRepository.findAll();
        for(Task_Task task: taskList) {
            if(task.getTaskState().toString().equalsIgnoreCase(String.valueOf(Task_TaskState.RUNNING))) {
                task.setTaskState(Task_TaskState.SUSPENDED);
                if(task.getTaskMonitor()!=null && task.getTaskMonitor().getTaskState().equalsIgnoreCase(String.valueOf(Task_TaskState.RUNNING)))
                    task.getTaskMonitor().setTaskState(String.valueOf(Task_TaskState.SUSPENDED));
            }
        }
        taskRepository.saveAll(taskList);
    }

    @Async
    public Future<List<ServiceRoot_ServiceRoot>> getRootData(OffsetDateTime startTime, Integer taskId) throws Exception {
        List<ServiceRoot_ServiceRoot> rootList = rootServiceRepository.findAll();
        if( ChronoUnit.SECONDS.between(startTime, OffsetDateTime.now())  > taskWaitTime+1) {
            taskService.updateTaskState(taskId.toString(), Task_TaskState.COMPLETED, rootList);
        }
        return new AsyncResult<List<ServiceRoot_ServiceRoot>>(rootList);
    }

    @Async
    public Future<List<MetadataFile>> getMetadataEntity(OffsetDateTime startTime, Integer taskId) throws Exception {
        List<MetadataFile> list = metadataFileRepository.findAll();
        if( ChronoUnit.SECONDS.between(startTime, OffsetDateTime.now())  > taskWaitTime+1) {
            taskService.updateTaskState(taskId.toString(), Task_TaskState.COMPLETED, list);
        }
        return new AsyncResult<List<MetadataFile>>(list);
    }

    @Async
    public Future<List<OdataFile>> getOdataEntity(OffsetDateTime startTime, Integer taskId) throws Exception {
        List<OdataFile> list = odataFileRepository.findAll();
        if( ChronoUnit.SECONDS.between(startTime, OffsetDateTime.now())  > taskWaitTime+1) {
            taskService.updateTaskState(taskId.toString(), Task_TaskState.COMPLETED, list);
        }
        return new AsyncResult<List<OdataFile>>(list);
    }

    public void createTaskForOperation(OffsetDateTime startTime, Integer newTaskId, String uri) {
        taskService.createTaskForAsyncOperation(startTime, newTaskId, uri);
    }

    public Integer getTaskId() {
        return taskService.getMaxTaskCount();
    }

    public String getTaskServiceURI(String newTaskId) {
        return taskService.getTaskServiceURI() + newTaskId + "/monitor";
    }

    public Task_Task getTaskResource(String Id) {
        return taskService.getTask(Id);
    }

}

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

import org.picmg.redfish_server_template.RFmodels.custom.MetadataFile;
import org.picmg.redfish_server_template.RFmodels.custom.OdataFile;
import org.picmg.redfish_server_template.repository.MetadataFileRepository;
import org.picmg.redfish_server_template.repository.OdataFileRepository;
import org.picmg.redfish_server_template.repository.RedfishObjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class RootService {

    @Value("${async.task.wait-time}")
    long taskWaitTime;

    @Autowired
    TaskService taskService;

    @Autowired 
    MetadataFileRepository metadataFileRepository;

    @Autowired
    OdataFileRepository odataFileRepository;

    @Autowired
    RedfishObjectRepository objectRepository;

/*
    @PostConstruct
    public void abortAllPreviouslyRunningOperations() {
        abortAllPreviouslyRunningTasks();
        clearAllPreviousSessions();
    }

    public void clearAllPreviousSessions() {
        // DEBUG: System.out.println("Clearing all Previous Sessions....");
        List<RedfishCollection> sessionsList = collectionRepository.findAllByType("SessionCollection");
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
*/
    public List<MetadataFile> getMetadataEntity() throws Exception {
        List<MetadataFile> list = metadataFileRepository.findAll();
        return list;
    }

    public List<OdataFile> getOdataEntity() throws Exception {
        List<OdataFile> list = odataFileRepository.findAll();
        return list;
    }
/*
    public void createTaskForOperation(OffsetDateTime startTime, Integer newTaskId, String uri) {
        taskService.createTaskForAsyncOperation(startTime, newTaskId, uri);
    }

    public Integer getTaskId() {
        return taskService.getMaxTaskCount();
    }

    public String getTaskServiceURI(String newTaskId) {
        return taskService.getTaskServiceURI() + newTaskId + "/monitor";
    }

    public RedfishObject getTaskResource(String Id) {
        return taskService.getTask(Id);
    }
*/
}

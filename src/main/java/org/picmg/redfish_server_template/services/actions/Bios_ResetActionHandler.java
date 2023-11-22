//******************************************************************************************************
// Bios_ResetActionHandler.java
//
// Reset action handler for bios..
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


package org.picmg.redfish_server_template.services.actions;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.picmg.redfish_server_template.RFmodels.custom.RedfishObject;
import org.picmg.redfish_server_template.services.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.Future;

@Service("Bios_Reset")
public class Bios_ResetActionHandler implements ActionHandler{

    @Value("${async.task.wait-time}")
    long taskWaitTime;

    @Autowired
    TaskService taskService;

    public Bios_ResetActionHandler() {
    }

    @Override
    @Async
    public Future<Boolean> execute(OffsetDateTime startTime, Integer taskId) throws Exception{
        //Variable for Storing value of task result in Task table.
        String result = "Action Executed";
        //Write code for executing the Action below
        //TODO: Delaying Service Method by 40 Seconds For Async Demo
//        Thread.sleep(40000);
//        if(startTime.getSecond() > taskWaitTime+1) {
//            taskService.updateTaskState(taskId.toString(), Utils.TASK_STATE_COMPLETED, result);
//        }
        return new AsyncResult<>(true);
    }

    @Override
    public List<String> validateRequestBody(String requestBody, List<RedfishObject> actionInfoParameters) {
        return null;
    }

    @Override
    public void setRequestBody(String requestBody) throws JsonProcessingException {

    }
}

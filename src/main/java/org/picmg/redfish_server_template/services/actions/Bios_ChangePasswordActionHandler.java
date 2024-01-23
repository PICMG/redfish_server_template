//******************************************************************************************************
// Bios_ChangePasswordActionHandler.java
//
// Change password action handler for bios.
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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.picmg.redfish_server_template.RFmodels.custom.RedfishObject;
import org.picmg.redfish_server_template.services.TaskService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

@Primary
@Service("Bios_ChangePassword")
public class Bios_ChangePasswordActionHandler implements ActionHandler {

    @Value("${async.task.wait-time}")
    long taskWaitTime;

    @Autowired
    TaskService taskService;



    public Bios_ChangePasswordActionHandler() {
    }

    public void setRequestBody(String requestBody) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        this.requestBody = mapper.readValue(requestBody, JsonNode.class);
    }

    private JsonNode requestBody;


    @Override
    @Async
    public Future<Boolean> execute(OffsetDateTime startTime, Integer taskId) throws TimeoutException, Exception {
        //Variable for Storing value of task result in Task table.
        String result = "Action Executed";
        //Write code for executing the Action below
        // DEBUG: System.out.println(this.requestBody.getPasswordName());
        //TODO: Delaying Service Method by 40 Seconds For Async Demo
//        Thread.sleep(40000);
//        if(startTime.getSecond() > taskWaitTime+1) {
//            taskService.updateTaskState(taskId.toString(), Utils.TASK_STATE_COMPLETED, result);
//        }
        return new AsyncResult<>(true);
    }

    @Override
    public List<String> validateRequestBody(String requestBody, List<RedfishObject> actionInfoParameters) {
        JSONObject requestBodyMap = new JSONObject(requestBody);
        List<String> errors = new ArrayList<>();
        for (String key:requestBodyMap.keySet()) {
            if (requestBodyMap.get(key).equals("")){
                errors.add("InvalidParameterValue_" + key);
            }
        }

        for (RedfishObject parameter: actionInfoParameters) {
            if (parameter.containsKey("Required") && !requestBodyMap.has(parameter.getName())){
                errors.add("ActionParameterMissing_" + parameter.getName());
            }
            if(requestBodyMap.has(parameter.getName())){
                Object obj = requestBodyMap.get(parameter.getName()).getClass();
                String[] dt = obj.toString().split("\\.");
                String dataType = dt[dt.length - 1];
                String parameterType = parameter.get("DataType").toString();
                if (!dataType.equalsIgnoreCase(parameterType)){
                    errors.add("IncorrectParameterType_"+ parameter.getName() + "_" + requestBodyMap.get(parameter.getName()));
                }
                /* TODO - Fix later
                if (parameter.containsKey("AllowableValues") && parameter.get("AllowableValues").size()>0){
                    if (!parameter.get("AllowableValues").contains(requestBodyMap.get(parameter.getName()))){
                        errors.add("IncorrectParameterValue_"+ parameter.getName() + "_" + requestBodyMap.get(parameter.getName()));
                    }
                }

                 */
            }

        }
        return errors;
    }
}

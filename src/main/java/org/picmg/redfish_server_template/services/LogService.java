//******************************************************************************************************
// SessionService.java
//
// Session service according to redfish specification.
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.picmg.redfish_server_template.RFmodels.custom.RedfishObject;
import org.picmg.redfish_server_template.repository.RedfishObjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class LogService  {

    @Autowired
    RedfishObjectRepository objectRepository;

    public void clearLog(String logUri) {
        objectRepository.deleteWithQuery(Criteria
                .where("_odata_type").is("LogEntry")
                .and("_odata_id").regex("^"+logUri+"[^/]+$"));
    }

    @PostConstruct
    private void configureService() {
        // configure all the log services in this system to match the capabilities of this server
        List<RedfishObject> logservices = objectRepository.findWithQuery(Criteria.where("_odata_type").is("LogService"));
        for (RedfishObject logservice : logservices) {
            String serviceUri = logservice.getAtOdataId();

            // log service should only support the clear log action
            logservice.put("Actions", Collections.singletonMap(
                    "#LogService.ClearLog", Collections.singletonMap(
                            "target",serviceUri+"/Log/Actions/ClearLog")
            ));

            // remove these keys if they exist
            logservice.remove("AutoDSTEnabled");
            logservice.remove("DateTime");
            logservice.remove("DateTimeLocalOffset");
            logservice.remove("MaxNumberOfRecords");
            logservice.remove("Overflow");
            logservice.remove("SyslogFilters");

            // set default valus
            logservice.put("LogEntryType","Event");
            logservice.put("Persistency",true);
            logservice.put("ServiceEnabled",true);
        }
    }
}

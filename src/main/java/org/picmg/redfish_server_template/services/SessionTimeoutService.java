package org.picmg.redfish_server_template.services;

import org.picmg.redfish_server_template.RFmodels.custom.RedfishObject;
import org.picmg.redfish_server_template.repository.RedfishObjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionTimeoutService {
    @Autowired
    RedfishObjectRepository objectRepository;

    private final ConcurrentHashMap<String, OffsetDateTime> activeSessionTiming = new ConcurrentHashMap<>();

    // A simple scheduled task that runs every 60 seconds to get rid of expired sessions
    @Scheduled(fixedRate = 60000)
    public void scheduledSessionCleaner() {
        long sessionTimeout = 5*60;  // by default, use a timeout value of 5 minutes
        // get the current value for session timeouts
        RedfishObject resource = objectRepository.findFirstWithQuery(
                Criteria.where("_odata_type").is("SessionService"));
        if (resource!=null) {
            if (resource.containsKey("SessionTimeout")) {
                sessionTimeout = resource.getInteger("SessionTimeout")*60;
            }
        }
        for (Iterator<Map.Entry<String,OffsetDateTime>> it = activeSessionTiming.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String,OffsetDateTime> entry = it.next();
            if (entry.getValue().until(OffsetDateTime.now(), ChronoUnit.SECONDS)>sessionTimeout) {
                // an expired session has been found - remove it
                objectRepository.deleteWithQuery(
                        Criteria.where("_odata_type").is("Session").and("UserName").is(entry.getKey()));
                it.remove();
            }
        }
    }

    public void touch(String userName) {
        activeSessionTiming.put(userName,OffsetDateTime.now());
    }

    public void hintForRemoval(String userName) {
        activeSessionTiming.put(userName,OffsetDateTime.MIN);
    }
}

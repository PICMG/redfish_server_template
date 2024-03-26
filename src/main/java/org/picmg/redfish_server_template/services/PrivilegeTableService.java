//******************************************************************************************************
// AccountService.java
//
// Service for account according to redfish.
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
import org.bson.Document;
import org.picmg.redfish_server_template.RFmodels.custom.PrivilegeTableEntry;
import org.picmg.redfish_server_template.RedfishServerApplication;
import org.picmg.redfish_server_template.repository.PrivilegeTableRepository;
import org.picmg.redfish_server_template.repository.RedfishObjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

@Service
public class PrivilegeTableService {
    @Autowired
    PrivilegeTableRepository privilegeTableRepository;

    @Autowired
    RedfishObjectRepository objectRepository;

    List <PrivilegeTableEntry> cache = null;

    @PostConstruct
    public void addMissingPrivilegeTableEntries() {
        // initialize the cache
        if (cache == null) {
            cache = privilegeTableRepository.findAll();
        }

        // load any additional resources
        InputStream filestream = getClass().getClassLoader().getResourceAsStream("additionalPrivileges.json");
        if (filestream != null) {
            try {
                BufferedReader reader = new BufferedReader( new InputStreamReader(filestream, "UTF-8" ));
                StringBuilder strbuilder = new StringBuilder();
                String line;
                while(( line = reader.readLine()) != null ) strbuilder.append( line );

                // convert to json object
                JsonNode jsonAdditions = new ObjectMapper().readTree(strbuilder.toString());

                for (JsonNode node : jsonAdditions.get("Mappings")) {
                    // add the node to the cache.
                    PrivilegeTableEntry entry = new PrivilegeTableEntry();
                    entry.setUri(node.get("uri").asText());
                    entry.setEntity(node.get("Entity").asText());
                    if (!entry.setOperationMap(node.get("OperationMap"))) throw new Exception();
                    cache.add(entry);
                }
            } catch (Exception ignored) {
                System.out.println("Error reading additionalPrivieges.json file.  Operation aborted.");
            }
        }
    }

    public PrivilegeTableEntry getPrivilegeTableEntryFromUri(String uri) {
        for (PrivilegeTableEntry entry: cache) {
            if (!entry.isMatchingUrl(uri)) continue;
            return entry;
        }
        return null;
    }

    public String getEntityTypeFromUri(String uri) {
        for (PrivilegeTableEntry entry: cache) {
            if (!entry.isMatchingUrl(uri)) continue;
            return entry.getEntity();
        }
        return null;
    }

    public void addActionPrivileges(String uri, String resourceType, String[] privileges) {
        cache.add(PrivilegeTableEntry.actionEntry(uri,resourceType,privileges));
    }
}

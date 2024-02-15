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

import org.picmg.redfish_server_template.RFmodels.custom.PrivilegeTableEntry;
import org.picmg.redfish_server_template.repository.PrivilegeTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PrivilegeTableService {
    @Autowired
    PrivilegeTableRepository privilegeTableRepository;

    List <PrivilegeTableEntry> cache = null;

    public PrivilegeTableEntry getPrivilegeTableEntryFromUri(String uri) {
        if (cache==null) {
            cache = privilegeTableRepository.findAll();
        }

        for (PrivilegeTableEntry entry: cache) {
            if (!entry.isMatchingUrl(uri)) continue;
            return entry;
        }
        return null;
    }

    public String getEntityTypeFromUri(String uri) {
        // get the authorities for the authenticated user
        if (cache==null) {
            cache = privilegeTableRepository.findAll();
        }

        for (PrivilegeTableEntry entry: cache) {
            if (!entry.isMatchingUrl(uri)) continue;
            return entry.getEntity();
        }
        return null;
    }

    public void addActionPrivileges(String uri, String resourceType, String[] privileges) {
        if (cache==null) {
            cache = privilegeTableRepository.findAll();
        }
        cache.add(PrivilegeTableEntry.actionEntry(uri,resourceType,privileges));
    }
}

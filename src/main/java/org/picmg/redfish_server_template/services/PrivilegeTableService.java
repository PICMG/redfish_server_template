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

import org.picmg.redfish_server_template.RFmodels.AllModels.Task_TaskState;
import org.picmg.redfish_server_template.RFmodels.custom.PrivilegeTableEntry;
import org.picmg.redfish_server_template.RFmodels.custom.RedfishObject;
import org.picmg.redfish_server_template.repository.AccountService.RedfishAuthorizationManager;
import org.picmg.redfish_server_template.repository.PrivilegeTableRepository;
import org.picmg.redfish_server_template.repository.RedfishObjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Supplier;

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
}

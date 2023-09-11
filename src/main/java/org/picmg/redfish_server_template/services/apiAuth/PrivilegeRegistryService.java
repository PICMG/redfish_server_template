//******************************************************************************************************
// PrivilegeRegistry.java
//
// privilege registry service file.
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


package org.picmg.redfish_server_template.services.apiAuth;

import org.picmg.redfish_server_template.RFmodels.AllModels.PrivilegeRegistry_PrivilegeRegistry;
import org.picmg.redfish_server_template.repository.PrivilegeRegistryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PrivilegeRegistryService {

    @Autowired
    PrivilegeRegistryRepository privilegeRegistryRepository;

    public List<PrivilegeRegistry_PrivilegeRegistry> getAllPrivilegesList() {
        List<PrivilegeRegistry_PrivilegeRegistry> privilegeRegistryList = privilegeRegistryRepository.findAll();
        return privilegeRegistryList;
    }
}

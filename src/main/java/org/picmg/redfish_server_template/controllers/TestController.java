//******************************************************************************************************
// TestController.java
//
// Controller for Tests.
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

package org.picmg.redfish_server_template.controllers;

import org.picmg.redfish_server_template.repository.MessageRegistryRepository;
import org.picmg.redfish_server_template.services.PasswordEncryptorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    MessageRegistryRepository messageRegistryRepository;

    @Autowired
    PasswordEncryptorService passwordEncryptorService;

/*    @GetMapping("/privilege")
    public List<PrivilegeRegistry_PrivilegeRegistry> getPrivileges(@RequestHeader String authorization) {

        String token = authorization.substring(7);
        Boolean isAPIAuthorized = apiAuthService.isUserAuthorizedForOperationType(token, "", "");

        return privilegeRegistryService.getAllPrivilegesList();
    }

    @GetMapping("/messageR")
    public List<MessageRegistry_MessageRegistry> getMessageRegistryList() {
        List<MessageRegistry_MessageRegistry> messageRegistryList = messageRegistryRepository.findAll();
        return messageRegistryList;
    }

    @GetMapping("/pass/{password}")
    public String getEncPassword(@PathVariable String password) {
        String passTest = passwordEncryptorService.encryptPassword(password);
        return passTest;
    }

    @GetMapping("/event")
    public String connectRedfishServiceFromEvents() {
        return "Calling Internal Redfish API from Event for Protocol: Redfish";
    }

 */
}

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
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Service
public class PasswordEncoderService implements UserDetailsService {
    @Autowired RedfishObjectRepository objectRepository;
    @Autowired ObjectMapper objectMapper;

    @Bean
    // by default, hash passwords using this encoder
    public PasswordEncoder passwordEncoder(){
        //return NoOpPasswordEncoder.getInstance();
        return new BCryptPasswordEncoder(10);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        RedfishObject userAccount = objectRepository.findFirstWithQuery(
                Criteria.where("_odata_type").is("ManagerAccount")
                        .and("UserName").is(username));
        if (userAccount == null) throw(new UsernameNotFoundException("Username not found"));

        // convert the body string into a JsonNode object
        JsonNode accountJson;
        try {
            accountJson = objectMapper.readTree(userAccount.toJson());
        } catch (JsonProcessingException e) {
            // invalid json data in database
            throw new RuntimeException(e);
        }

        // check to see if this is a redfish account
        if (accountJson.has("AccountTypes")) {
            boolean redfishAccount  = false;
            List<String> types = accountJson.findValuesAsText("AccountTypes");
            if (accountJson.get("AccountTypes").isArray()) {
                for (JsonNode arrayItem : accountJson.get("AccountTypes")) {
                    String accountType = arrayItem.asText("unknown");
                    if (accountType.toLowerCase().equals("redfish")) {
                        redfishAccount = true;
                        break;
                    }
                }
            }
            if (!redfishAccount) {
                // account does not support Redfish login
                throw (new UsernameNotFoundException("Username not found"));
            }
        }

        // create the authorization list for this user
        String rid = accountJson.get("RoleId").asText("");
        ArrayList<GrantedAuthority> p_list = new ArrayList<GrantedAuthority>();
        if (rid != null && !rid.isEmpty()) {
            // here the user has been assigned a role id
            // check the roles db to get the privileges
            RedfishObject r_entry = objectRepository.findFirstWithQuery(
                    Criteria.where("_odata_type").is("Role")
                            .and("Id").is(rid));
            if (r_entry != null) {
                // here if there is a matching Role in the Roles table
                // add the standard and oem privileges
                if ((r_entry.get("AssignedPrivileges") != null) &&
                        (r_entry.get("AssignedPrivileges") instanceof List  )) {
                    for (Object r: (List)r_entry.get("AssignedPrivileges")) {
                        p_list.add(new SimpleGrantedAuthority(r.toString()));
                    }
                }
                if ((r_entry.get("OemPrivileges") != null) &&
                        (r_entry.get("OemPrivileges") instanceof List  )) {
                    for (Object s: (List)r_entry.get("AssignedPrivileges")) {
                        p_list.add(new SimpleGrantedAuthority(s.toString()));
                    }
                }
            }
        }
        // create a new UserDetail with the username and password for the user
        return new User(userAccount.get("UserName").toString(),
                userAccount.get("Password").toString(),
                userAccount.get("Enabled").toString().equalsIgnoreCase("true"),
                true,
                true,
                !userAccount.get("Locked").toString().equalsIgnoreCase("true"),
                p_list);
    }
}

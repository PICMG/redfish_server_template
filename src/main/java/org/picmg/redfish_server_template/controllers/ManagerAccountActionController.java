//******************************************************************************************************
// AccountController.java
//
// Controller for account service.
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

import org.picmg.redfish_server_template.RFmodels.Autogenerated.RedfishError;
import org.picmg.redfish_server_template.RFmodels.custom.RedfishObject;
import org.picmg.redfish_server_template.services.*;
import org.picmg.redfish_server_template.services.jwt.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.*;


@RestController
@RequestMapping(value = {"/redfish/v1/AccountService/Accounts/*/Actions/*"})
public class ManagerAccountActionController extends RedfishObjectController {
    @Autowired
    JWTService jwtService;

    @Autowired
    SessionService sessionService;

    @Autowired PasswordEncoderService passwordEncoderService;
    @Autowired
    AccountService accountService;

    @Autowired
    PrivilegeTableService privilegeTableService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AccountController accountController;

    @PostConstruct
    private void addPrivilegesForActions() {
        String[] privileges = {"ConfigureUsers","ConfigureSelf"};
        privilegeTableService.addActionPrivileges(
                "/redfish/v1/AccountService/Accounts/[^\\\\/]+/Actions/ManagerAccount.ChangePassword",
                "ManagerAccount", privileges);
    }

    // actionAsyncHandler
    // This method is called by the task service to handle the particular action.  Default behavior does nothing.
    //
    // parameters - the parameters passed to the action from the caller.
    // uri - the uri of the action call
    // request - the complete request that invoked the action
    // taskId - the taskId that will be associated with this action if it is not completed quickly
    // taskService - the task service that invoked this method call.
    //
    // returns - a response to be returned to the calling client
    @Override
    public ResponseEntity<?> actionAsyncHandler(RedfishObject parameters, String uri, HttpServletRequest request, String taskId, TaskService taskService) {
        // attempt to get the user information from the Authorization Header
        String authHeader = request.getHeader("Authorization");
        String xauthHeader = request.getHeader("X-Auth-Token");
        if (authHeader == null && xauthHeader== null) {
            // here if there is no authentication - this should not happen, however,
            // we will check for it anyway.
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED).body("");
        }

        String authUserName = null;
        if(xauthHeader!=null) {
            try {
                String token = xauthHeader;
                authUserName = jwtService.extractJWTUsername(token);
            } catch (Exception e) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED).body("");
            }
        } else if(authHeader.startsWith("Bearer")) {
            // For bearer authentication, get the user name from the token
            try {
                String token = authHeader.substring(7);
                authUserName = jwtService.extractJWTUsername(token);
            } catch (Exception e) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED).body("");
            }
        } else if (authHeader.startsWith("Basic")) {
            // in this case, HTTP basic authentication was used, decode the token and extract the user name
            String token = authHeader.substring(6);
            byte[] tokenDecoded = Base64.getDecoder().decode(token);
            authUserName = new String(tokenDecoded, StandardCharsets.UTF_8).split(":",2)[0];
        }

        // get user information from current session's user name
        UserDetails sessionUserDetails = passwordEncoderService.loadUserByUsername(authUserName);
        String sessionPassword = sessionUserDetails.getPassword();

        // verify that the password matches the password complexity requirements set by the AccountController
        if (!accountController.isPasswordComplexityOk(parameters.get("NewPassword").toString())) {
            RedfishError err =  redfishErrorResponseService.getErrorMessage(
                    "Base","ActionParameterValueError",
                    new ArrayList<>(Arrays.asList("Password", "ManagerAccount.ChangePassword")),
                    new ArrayList<>());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(err.toString());
        }

        // verify the session password against the password provided in the action's parameters
        if (!passwordEncoder.matches(parameters.get("SessionAccountPassword").toString(),sessionPassword)) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED).body("");
        }

        // get the resource using the URI
        String resourceUri = uri.substring(0,uri.indexOf("/Actions/"));
        RedfishObject entity = objectRepository.findFirstWithQuery(Criteria
                .where("_odata_id").is(resourceUri));
        if (entity==null) {
            // resource not found error - this should not happen
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }

        // get the user name from the manager account that is being updated
        if (!entity.containsKey("UserName")) {
            // No UserName field - this should not happen
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("");
        }

        String requiredAuthority = "ConfigureUsers";
        if (entity.get("UserName").toString().equals(authUserName)) {
            // the requester must have "ConfigureSelf" privileges
            requiredAuthority = "ConfigureSelf";
        }
        // Check the requester's authority
        boolean hasAuthority = false;
        for (GrantedAuthority authority:sessionUserDetails.getAuthorities()) {
            if (Objects.equals(authority.getAuthority(), requiredAuthority)) {
                hasAuthority = true;
                break;
            }
        }
        if (!hasAuthority) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("");
        }

        // update the password
        String userName = entity.get("UserName").toString();
        if (!accountService.updatePassword(userName, parameters.get("NewPassword").toString(), null)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("");
        }
        RedfishError result = redfishErrorResponseService.getErrorMessage(
                "Base",
                "AccountModified",
                new ArrayList<>(),
                new ArrayList<>());

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(result);
    }
}
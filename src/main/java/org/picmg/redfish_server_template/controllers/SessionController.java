//******************************************************************************************************
// SessionController.java
//
// Controller for Session service.
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

import org.picmg.redfish_server_template.RFmodels.custom.RedfishObject;
import org.picmg.redfish_server_template.dto.SessionLoginDTO;
import org.picmg.redfish_server_template.services.RedfishErrorResponseService;
import org.picmg.redfish_server_template.services.jwt.JWTService;
import org.picmg.redfish_server_template.services.SessionService;
import org.hibernate.UnknownProfileException;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
public class SessionController extends RedfishObjectController {
    @Autowired
    SessionService sessionService;
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    JWTService jwtService;

    @Autowired
    RedfishErrorResponseService errorResponseService;

    @RequestMapping(value = "/redfish/v1/SessionService/Sessions", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> sessionPost(@RequestBody SessionLoginDTO account) throws IOException, NoSuchAlgorithmException {
        RedfishObject user = sessionService.validateUser(account.getUserName());
        try {
            if (user == null){
                return ResponseEntity.badRequest().body("Username or Password is Incorrect");
            }
            if (user.get("Password").toString().compareTo(account.getPassword()) != 0){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username or Password is Incorrect");
            }
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(account.getUserName(), account.getPassword())
            );
            RedfishObject session = new RedfishObject();
            session.setId(UUID.randomUUID().toString());
            session.setName("User Session");
            session.setDescription("Manager User Session");
            session.put("UserName",user.get("UserName"));
            session.setAtOdataId("/redfish/v1/SessionService/Sessions/" + session.getId());
            session.setAtOdataType("Session_Session");
            session.put("CreatedTime",JsonNullable.of(OffsetDateTime.now()));
            sessionService.addSession(session);
            String jwt = jwtService.generateToken(user, session.getId());
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.LOCATION, session.getAtOdataId());
            headers.set("X-Auth-Token", jwt);
            return ResponseEntity.ok().headers(headers).body(session);

        } catch (UnknownProfileException unknownProfileException) {
            ResponseEntity.badRequest().body(unknownProfileException.getMessage());
        }
        catch (Exception exception){
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
        return ResponseEntity.badRequest().body("");
    }

    /* TODO: add back later?
    @RequestMapping(value="/Sessions", method=RequestMethod.PATCH, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateSession(@RequestBody Session_Session session, @RequestHeader(value="Authorization", required=false) String authorization) throws Exception {
        if(session.getId() == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Request could not be processed because it contains invalid information");


        if(sessionService.updateSession(session))
            return ResponseEntity.ok(errorResponseService.getSuccessResponse());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Request could not be processed because it contains invalid information");
    }
*/
    /* TODO: add back later?
    @RequestMapping(value="", method=RequestMethod.PATCH, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateSessionService(@RequestBody SessionService_SessionService sessionService1, @RequestHeader(value="Authorization", required=false) String authorization) throws Exception {
        if(sessionService1 == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Request could not be processed because it contains invalid information");

        return ResponseEntity.ok(sessionService.updateSessionService(sessionService1));
    }

    @RequestMapping(value = "/Sessions/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteSession(@RequestHeader(value="Authorization", required=false) String authorization, @PathVariable("id") String id) throws Exception {
        if(id == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ID cannot be null");
        sessionService.deleteMemberFromSessionCollection(id);
        Session_Session deletedSession = sessionService.deleteSession(id);

        if(deletedSession != null)
            return ResponseEntity.ok(deletedSession);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Session not found");
    }
    @RequestMapping(value="/Sessions", method=RequestMethod.GET)
    public ResponseEntity<?> getSessionCollection(@RequestHeader(value="Authorization", required=false) String authorization) throws Exception {
        return ResponseEntity.ok().body(sessionService.getSessionCollection());
    }

    @RequestMapping(value="", method=RequestMethod.GET)
    public ResponseEntity<?> getSessionService(@RequestHeader(value="Authorization", required=false) String authorization) throws Exception {
        return ResponseEntity.ok().body(sessionService.getSessionService());

    }

     */
}
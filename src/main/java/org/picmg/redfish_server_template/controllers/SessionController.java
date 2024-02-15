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

import org.picmg.redfish_server_template.RFmodels.custom.CachedSchema;
import org.picmg.redfish_server_template.RFmodels.custom.RedfishObject;
import org.picmg.redfish_server_template.services.AccountService;
import org.picmg.redfish_server_template.services.RedfishErrorResponseService;
import org.picmg.redfish_server_template.services.jwt.JWTService;
import org.picmg.redfish_server_template.services.SessionService;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;


@RestController
@RequestMapping(value = "/redfish/v1/SessionService/Sessions")
public class SessionController extends RedfishObjectController {
    @Autowired
    SessionService sessionService;
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    JWTService jwtService;

    @Autowired
    RedfishErrorResponseService errorResponseService;

    @Autowired
    AccountService accountService;

    // onPostCompleteMissingFields()
    //
    // This method is called during an HTTP post request after initial payload has been validated against the schema.
    // It can be assumed that the payload has all required fields for onCreate, but other required fields may be missing.
    // This method completes field data for any required fields and the updated redfish object is returned.
    //
    // The default behavior of this function is to complete the @odata.id, id, and Name fields.  @odata.type has
    // already been completed. Objects that extend this class should update any other required fields.
    //
    // parameters:
    //    RedfishObject obj -- the object to be posted
    //    HttpServletRequest request -- the post request that was received
    //    CachedSchema schema -- the related schema object for the posted data
    //
    // returns:
    //    RedfishObject with updated fields
    //
    protected RedfishObject onPostCompleteMissingFields(RedfishObject obj, HttpServletRequest request, CachedSchema schema) {
        RedfishObject session = super.onPostCompleteMissingFields(obj,request,schema);
        session.setName(obj.getString("UserName") + " Session");
        session.setDescription("User Session for " + obj.getString("UserName"));
        session.put("UserName",obj.getString("UserName"));
        // don't store the password field - although it is required on create, we don't need to store it.
        session.remove("Password");
        session.put("CreatedTime",OffsetDateTime.now().toString());

        return session;
    }

    // onPostCreationChecks()
    //
    // This method checks the validity of the provided data during a POST operation.
    // It can be assumed that the payload has all required fields populated.
    // This method checks field values against service requirements to make sure the object is valid.
    //
    // The default behavior of this is to return null (no error). Objects that extend this class should
    // override this behavior to meet the needs of their specific object type.
    //
    // parameters:
    //    RedfishObject obj -- the object to be posted
    //    HttpServletRequest request -- the post request that was received
    //    CachedSchema schema -- the related schema object for the posted data
    //
    // returns:
    //    RedfishError if errors are found, otherwise null
    //
    @Override
    protected ResponseEntity<?> onPostUpdateResponse(RedfishObject obj, HttpServletRequest request, ResponseEntity<?> response) {
        // get the related account
        RedfishObject account = accountService.getAccountFromUserName(obj.getString("UserName"));
        if (account!=null) {
            String jwt = null;
            try {
                jwt = jwtService.generateToken(account, obj.getId());
                HttpHeaders respHeaders = new HttpHeaders();
                respHeaders.addAll(response.getHeaders());
                respHeaders.add("X-Auth-Token", jwt);
                return ResponseEntity.status(response.getStatusCode())
                        .headers(respHeaders).body(response.getBody());
            } catch (Exception ignored) {
            }
        }
        return response;
    }

}
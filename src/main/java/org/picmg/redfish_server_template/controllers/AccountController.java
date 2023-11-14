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

import org.picmg.redfish_server_template.RFmodels.AllModels.RedfishError;
import org.picmg.redfish_server_template.RFmodels.custom.CachedSchema;
import org.picmg.redfish_server_template.RFmodels.custom.RedfishObject;
import org.picmg.redfish_server_template.data_validation.ValidRedfishObject;
import org.picmg.redfish_server_template.repository.RedfishObjectRepository;
import org.picmg.redfish_server_template.services.AccountService;
import org.picmg.redfish_server_template.services.PasswordEncryptorService;
import org.picmg.redfish_server_template.services.RedfishErrorResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;


@RestController
@RequestMapping({
        "/redfish/v1/AccountService"
})
public class AccountController extends RedfishObjectController {
    @Autowired
    PasswordEncryptorService passwordEncryptorService;

    @Autowired
    RedfishObjectRepository objectRepository;

    @Autowired
    RedfishErrorResponseService errorResponseService;

    @Autowired AccountService accountService;


    // onPostCheckForExistence()
    //
    // This method is called during an HTTP post request after initial payload has been validated against the schema.
    // It can be assumed that the payload has all required fields for onCreate, but other required fields may be missing.
    // This method checks to see if any other instances of this object exist in the database.
    // If a duplicate is found, the method returns a RedfishError indicating that a duplicate record has been found
    // otherwise, null is returned.
    //
    // This override version of this function makes sure that no other account  has the same username
    //
    // parameters:
    //    RedfishObject obj -- the object to be posted
    //    HttpServletRequest request -- the post request that was received
    //    CachedSchema schema -- the related schema object for the posted data
    //
    // returns:
    //    RedfishError describing the issue, otherwise null
    //
    @Override
    protected RedfishError onPostCheckForExistence(RedfishObject obj, HttpServletRequest request, CachedSchema schema) {
        List<RedfishObject> userAccounts = objectRepository.findWithQuery(
                Criteria.where("_odata_type").is("ManagerAccount")
                        .and("UserName").is(obj.get("UserName").toString()));
        if (!userAccounts.isEmpty()) {
            return errorResponseService.getErrorMessage("Base","ResourceAlreadyExists",
                    Arrays.asList("ManagerAccount", "UserName", obj.get("UserName").toString()),
                    null);
        }
        return null;
    }

    // onPostCompleteMissingFields()
    //
    // This method is called during an HTTP post request after initial payload has been validated against the schema.
    // It can be assumed that the payload has all required fields for onCreate, but other required fields may be missing.
    // This method completes field data for any required fields and the updated redfish object is returned.
    //
    // The default behavior of this function is to complete the @odata.id, Id, and Name fields.  @odata.type has
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
    @Override
    protected RedfishObject onPostCompleteMissingFields(RedfishObject obj, HttpServletRequest request, CachedSchema schema) {
        RedfishObject result = super.onPostCompleteMissingFields(obj, request, schema);

        if(result.containsKey("Password")) {
            String encPassword = passwordEncryptorService.encryptPassword(result.get("Password").toString());
            result.put("Password",encPassword);
        }
        if (!result.containsKey("AccountTypes")) {
            result.put("AccountTypes", Collections.singletonList("Redfish"));
        }
        return result;
    }

    // onPostCreationChecks()
    //
    // This method checks the validity of the provided data during a POST operation.
    // It can be assumed that the payload has all required fields populated.
    // This method checks to make sure that all object fields are valid
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
    protected RedfishError onPostCreationChecks(RedfishObject obj, HttpServletRequest request, CachedSchema schema) {
        // make sure the RoleId is valid - it must exist because it has already been validated against the schema
        String roleId = obj.get("RoleId").toString();
        boolean roleMatched = false;
        for (String role: accountService.getRoles()) {
            if (Objects.equals(roleId, role)) {
                roleMatched=true;
                break;
            }
        }
        if (!roleMatched) {
            return redfishErrorResponseService.getErrorMessage(
                    "Base","PropertyValueError", Collections.singletonList("RoleId"), new ArrayList<>());
        }
        return null;
    }

    @Override
    @PostMapping({"/Accounts","/Accounts/Members"})
    public ResponseEntity<?> post(@ValidRedfishObject RedfishObject obj, HttpServletRequest request) {
        return super.post(obj, request);
    }
}
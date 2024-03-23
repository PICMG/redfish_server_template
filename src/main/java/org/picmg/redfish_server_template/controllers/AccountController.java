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

import io.swagger.models.Response;
import org.picmg.redfish_server_template.RFmodels.Autogenerated.RedfishError;
import org.picmg.redfish_server_template.RFmodels.custom.CachedSchema;
import org.picmg.redfish_server_template.RFmodels.custom.RedfishObject;
import org.picmg.redfish_server_template.data_validation.ValidRedfishObject;
import org.picmg.redfish_server_template.repository.RedfishObjectRepository;
import org.picmg.redfish_server_template.services.AccountService;
import org.picmg.redfish_server_template.services.PasswordEncoderService;
import org.picmg.redfish_server_template.services.RedfishErrorResponseService;
import org.picmg.redfish_server_template.services.jwt.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.*;


@RestController
@RequestMapping(value = {"/redfish/v1/AccountService"})
public class AccountController extends RedfishObjectController {
    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired PasswordEncoderService passwordEncoderService;

    @Autowired
    RedfishObjectRepository objectRepository;

    @Autowired
    RedfishErrorResponseService errorResponseService;

    @Autowired
    AccountService accountService;

    @Autowired
    JWTService jwtService;

    // on post and patch operations, check the complexity of the password to make sure it meets system
    // requirements
    public boolean isPasswordComplexityOk(String password) {
        RedfishObject service = objectRepository.findFirstWithQuery(Criteria.where("_odata_type").is("AccountService"));
        if (service != null) {
            if (service.containsKey("MinPasswordLength")) {
                long minLength = service.getInteger("MinPasswordLength");
                if (password.length() < minLength) {
                    return false;
                }
            }
            if (service.containsKey("MaxPasswordLength")) {
                long maxLength = service.getInteger("MaxPasswordLength");
                if (password.length() > maxLength) {
                    return false;
                }
            }
        }
        return true;
    }


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
            return errorResponseService.getErrorMessage("Base", "ResourceAlreadyExists",
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

        if (result.containsKey("Password")) {
            String encPassword = passwordEncoder.encode(result.get("Password").toString());
            result.put("Password", encPassword);
        }
        if (!result.containsKey("AccountTypes")) {
            result.put("AccountTypes", Collections.singletonList("Redfish"));
        }
        if (!result.containsKey("Enabled")) result.put("Enabled", true);
        if (!result.containsKey("Locked")) result.put("Locked", false);
        // TODO: Add link to role?

        // put the actions into the object for password change
        result.put("Actions", Collections.singletonMap(
                "#ManagerAccount.ChangePassword",
                Collections.singletonMap("target", result.get("@odata.id") + "/Actions/ManagerAccount.ChangePassword")));
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
        for (String role : accountService.getRoles()) {
            if (Objects.equals(roleId, role)) {
                roleMatched = true;
                break;
            }
        }
        if (!roleMatched) {
            return redfishErrorResponseService.getErrorMessage(
                    "Base", "PropertyValueError", Collections.singletonList("RoleId"), new ArrayList<>());
        }
        // check password complexity based on account service rules
        if (!obj.containsKey("Password")) {
            obj.put("Password", "");
        }
        if (!isPasswordComplexityOk(obj.get("Password").toString())) {
            return redfishErrorResponseService.getErrorMessage(
                    "Base", "PropertyValueIncorrect",
                    new ArrayList<>(Arrays.asList("Password", obj.get("Password").toString())),
                    new ArrayList<>());
        }
        return null;
    }

    // onPatchPreApplyChanges()
    //
    // This method checks the validity of the pre-modified object for a PATCH operation.
    //
    // The default behavior of this is to return null (no error). Objects that extend this class should
    // override this behavior to meet the needs of their specific object type.
    //
    // parameters:
    //    RedfishObject obj -- the partial object that specifies changes to be made
    //    HttpServletRequest request -- the post request that was received
    //
    // returns:
    //    RedfishError if errors are found, otherwise null
    //    protected List<RedfishError> onPatchPreApplyChanges(RedfishObject ignoredObj, HttpServletRequest ignoredRequest) {
    protected List<RedfishError> onPatchPreApplyChanges(RedfishObject obj, HttpServletRequest request) {
        // check to see if the user has authority to change this resource
        // get target info
        String authHeader = request.getHeader("Authorization");
        String userName = null;
        boolean authorized = false;
        try {
            // For bearer authentication, get the user name from the token
            if(authHeader != null && authHeader.startsWith("Bearer")) {
                String jwt = authHeader.substring(7);
                userName = jwtService.extractJWTUsername(jwt);

            }
            else if (authHeader != null && authHeader.startsWith("Basic")) {
                String basicToken = authHeader.substring(6);

                // decode the basic token to get the username/password
                String decodedToken = new String(Base64.getDecoder().decode(basicToken), StandardCharsets.UTF_8);
                userName = decodedToken.substring(0,decodedToken.indexOf(':'));
            }
            // get the user details
            UserDetails userdetails = passwordEncoderService.loadUserByUsername(userName);
            RedfishObject target = objectRepository.findFirstWithQuery(Criteria.where("_odata_id").is(request.getRequestURI()));
            if (userdetails.getAuthorities().contains(new SimpleGrantedAuthority("ConfigureUsers"))) {
                // this authority can do an unrestricted patch
                authorized = true;
            } else if (userdetails.getAuthorities().contains(new SimpleGrantedAuthority("ConfigureSelf")) &&
                    (target.get("UserName").equals(userName)) && (obj.containsKey("Password")) && (obj.keySet().size() == 1)) {
                // this authority can only configure the password for the user's own account
                authorized = true;
            }
        } catch (Exception e) {
            System.out.println("Exception");
        }
        if (!authorized) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return null;
    }


    // onPatchPreWriteChecks()
    //
    // This method checks the validity of the modified object for a PATCH operation.
    //
    // The default behavior of this is to return null (no error). Objects that extend this class should
    // override this behavior to meet the needs of their specific object type.
    //
    // parameters:
    //    RedfishObject obj -- the object to be written back
    //    HttpServletRequest request -- the post request that was received
    //
    // returns:
    //    RedfishError if errors are found, otherwise null
    //
    @Override
    protected List<RedfishError> onPatchPreWriteChecks(RedfishObject obj, HttpServletRequest request) {

        // if the Account Service does not allow for patching the password, and the user is trying to change the
        // password, this is an error
        if ((obj.containsKey("Password")) && (accountService.isChangePasswordActionRequired())) {
            List<RedfishError> errors = new ArrayList<>();
            errors.add(redfishErrorResponseService.getErrorMessage(
                    "Base", "PropertyNotWritable",
                    new ArrayList<>(List.of("Password")),
                    new ArrayList<>()));
            return errors;
        }

        if ((obj.containsKey("Password")) && (!isPasswordComplexityOk(obj.get("Password").toString()))) {
            List<RedfishError> errors = new ArrayList<>();
            errors.add(redfishErrorResponseService.getErrorMessage(
                    "Base", "PropertyValueIncorrect",
                    new ArrayList<>(Arrays.asList("Password", obj.get("Password").toString())),
                    new ArrayList<>()));
            return errors;
        }

        // encode the password
        if (obj.containsKey("Password")) {
            RedfishObject userAccount = objectRepository.findFirstWithQuery(Criteria.where("_odata_id").is(request.getRequestURI().toString()));
            if (userAccount == null) return null;

            // update the password and any statistics with it
            accountService.updatePassword(userAccount.get("UserName").toString(),obj.get("Password").toString());
            obj.put("Password",passwordEncoder.encode(obj.get("Password").toString()));
        }
        return null;
    }

    @Override
    @PostMapping(value = {"/Accounts", "/Accounts/Members"})
    public ResponseEntity<?> post(@ValidRedfishObject RedfishObject obj, HttpServletRequest request) {
        return super.post(obj, request);
    }


    @Override
    @PatchMapping(value = {"/Accounts/*"})
    public ResponseEntity<?> patch(@ValidRedfishObject RedfishObject obj, HttpServletRequest request) {
        return super.patch(obj, request);
    }
}

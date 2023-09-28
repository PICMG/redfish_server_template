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

import org.picmg.redfish_server_template.RFmodels.AllModels.*;
import org.picmg.redfish_server_template.dto.AccountDTO;
import org.picmg.redfish_server_template.repository.AccountService.AccountServiceRepository;
import org.picmg.redfish_server_template.repository.AccountService.ManagerAccountCollectionRepository;
import org.picmg.redfish_server_template.repository.AccountService.RoleCollectionRepository;
import org.picmg.redfish_server_template.repository.RolesRepository;
import org.picmg.redfish_server_template.services.AccountService;
import org.picmg.redfish_server_template.services.QueryParameterService;
import org.picmg.redfish_server_template.services.RedfishErrorResponseService;
import org.picmg.redfish_server_template.services.apiAuth.APIAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


@RestController
@RequestMapping("/redfish/v1/AccountService")
public class AccountController {

    @Value("${async.task.retry-time}")
    Integer taskRetryTime;

    @Value("${async.task.wait-time}")
    long taskWaitTime;

    String baseURL = "/redfish/v1/AccountService";

    String controllerEntityName = "AccountService";

    @Autowired
    AccountService accountService;

    @Autowired
    APIAuthService apiAuthService;

    @Autowired
    QueryParameterService queryParameterService;

    @Autowired
    RedfishErrorResponseService redfishErrorResponseService;

    @Autowired
    AccountServiceRepository accountServiceRepository;

    @Autowired
    ManagerAccountCollectionRepository managerAccountCollectionRepository;

    @Autowired
    RoleCollectionRepository roleCollectionRepository;

    @Autowired
    RolesRepository rolesRepository;

    private ManagerAccount_ManagerAccount hideAccountDetailsInResponse(ManagerAccount_ManagerAccount account) {
        account.setPassword(null);
        return account;
    }

    private List<ManagerAccount_ManagerAccount> hideAccountDetailsListInResponse(List<ManagerAccount_ManagerAccount> accountList) {
        for(ManagerAccount_ManagerAccount account: accountList)
            hideAccountDetailsInResponse(account);
        return accountList;
    }

    @GetMapping
    public ResponseEntity<?> getAccountServices() {
        String uri = "/redfish/v1/AccountService";
        return ResponseEntity.ok(accountServiceRepository.findFirstByOdataId(uri));
    }

    @GetMapping("/Accounts")
    public ResponseEntity<?> getAccounts(@RequestParam Map<String, String> params) throws IOException {
        String uri = "/redfish/v1/AccountService/Accounts";
        return ResponseEntity.ok(managerAccountCollectionRepository.findFirstByOdataId(uri));
    }

    @RequestMapping(value="/Accounts", method=RequestMethod.DELETE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteData(@RequestBody ManagerAccount_ManagerAccount account, @RequestHeader String authorization) {
        try {
            if(accountService.deleteAccount(account))
                return ResponseEntity.ok(account);
        } catch (ChangeSetPersister.NotFoundException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(String.format("The requested resource of type %1 named '%2' was not found.", "Account", account.getName()));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(String.format("Request could not be processed because it contains invalid information"));
    }

    @RequestMapping(value="/Accounts", method=RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addAccount(@RequestBody ManagerAccount_ManagerAccount account, @RequestHeader String authorization) {
        String uri = "/redfish/v1/AccountService/Accounts";
        Integer newTaskId = accountService.getTaskId();
        OffsetDateTime startTime = OffsetDateTime.now();
        Boolean accountCreated =  false;
        ManagerAccount_ManagerAccount userAccount = new ManagerAccount_ManagerAccount();
        userAccount = account;
        try {
            Future<Boolean> resp = accountService.addAccount(startTime, newTaskId, userAccount);
            accountCreated = resp.get(taskWaitTime, TimeUnit.SECONDS);
            if(accountCreated) {
                HttpHeaders responseHeaders = new HttpHeaders();
                responseHeaders.set("Location",
                        baseURL + "/Account/"+userAccount.getId());
                return ResponseEntity.status(HttpStatus.CREATED).headers(responseHeaders).body(hideAccountDetailsInResponse(userAccount));
            }
        } catch (TimeoutException e) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Location", accountService.getTaskServiceURI(newTaskId.toString()));
            responseHeaders.set("Retry-After", taskRetryTime + " seconds");
            accountService.createTaskForOperation(startTime, newTaskId, uri);
            return ResponseEntity.status(HttpStatus.ACCEPTED).headers(responseHeaders).body(accountService.getTaskResource(newTaskId.toString()));
        }
        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Creation or update request could not be completed because it would cause a conflict in the\n" +
                    "current state of the resources that the platform supports");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Request could not be processed because it contains invalid information");
    }

    @GetMapping("/Accounts/{ID}")
    @ResponseBody
    public ResponseEntity<?> findAccountById(@PathVariable String ID) {
        try {
            ManagerAccount_ManagerAccount userAccount = hideAccountDetailsInResponse(accountService.getAccountById(ID));
            return ResponseEntity.status(HttpStatus.OK).body(userAccount);
        } catch (ChangeSetPersister.NotFoundException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Request succeeded, but no content is being returned in the body of the response.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Request could not be processed because it contains invalid information");
        }
    }

    @PatchMapping("/Accounts")
    @ResponseBody
    public ResponseEntity<?> updateAccountByUserName(@RequestBody ManagerAccount_ManagerAccount account, @RequestHeader String authorization) {
        String uri = "/redfish/v1/AccountService/Accounts";
        Integer newTaskId = accountService.getTaskId();
        OffsetDateTime startTime = OffsetDateTime.now();
        Boolean accountUpdated =  false;
        try {
            Future<Boolean> resp = accountService.updateUser(startTime, newTaskId, account);
            accountUpdated = resp.get(taskWaitTime, TimeUnit.SECONDS);
            if(accountUpdated)
                return ResponseEntity.status(HttpStatus.OK).body(hideAccountDetailsInResponse(account));
        } catch (TimeoutException e) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Location", accountService.getTaskServiceURI(newTaskId.toString()));
            responseHeaders.set("Retry-After", taskRetryTime + " seconds");
            accountService.createTaskForOperation(startTime, newTaskId, uri);
            return ResponseEntity.status(HttpStatus.ACCEPTED).headers(responseHeaders).body(accountService.getTaskResource(newTaskId.toString()));
        } catch(UnsupportedOperationException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(redfishErrorResponseService.getNOOperationErrorResponse());
        } catch (ChangeSetPersister.NotFoundException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Request succeeded, but no content is being returned in the body of the response.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Request could not be processed because it contains invalid information");
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Creation or update request could not be completed because it would cause a conflict in the" +
                "current state of the resources that the platform supports");
    }

    @GetMapping("/Roles")
    @ResponseBody
    public ResponseEntity<?> getRoleCollection(@RequestHeader String authorization) {
        String uri = "/redfish/v1/AccountService/Roles";
        return ResponseEntity.status(HttpStatus.OK).body(roleCollectionRepository.findFirstByOdataId(uri));
    }

    @GetMapping("/Roles/{Id}")
    @ResponseBody
    public ResponseEntity<?> getRole(@PathVariable String Id) {
        String uri = "/redfish/v1/AccountService/Roles/" + Id;
        Role_Role role = rolesRepository.getByOdataId(uri);
        // TODO handle not found;
        return ResponseEntity.status(HttpStatus.OK).body(role);
    }

    @GetMapping("/ExternalAccountProviders")
    @ResponseBody
    public ResponseEntity<?> getAccountProviders(@RequestHeader String authorization) {
        String uri = "/redfish/v1/AccountService/ExternalAccountProviders";
        Integer newTaskId = accountService.getTaskId();
        OffsetDateTime startTime = OffsetDateTime.now();
        ExternalAccountProviderCollection externalAccountProviderCollection = null;
        try {
            Future<ExternalAccountProviderCollection> resp = accountService.getExternalAccountProviders(startTime, newTaskId);
            externalAccountProviderCollection = resp.get(taskWaitTime, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Location", accountService.getTaskServiceURI(newTaskId.toString()));
            responseHeaders.set("Retry-After", taskRetryTime + " seconds");
            accountService.createTaskForOperation(startTime, newTaskId, uri);
            return ResponseEntity.status(HttpStatus.ACCEPTED).headers(responseHeaders).body(accountService.getTaskResource(newTaskId.toString()));
        } catch (ChangeSetPersister.NotFoundException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Request succeeded, but no content is being returned in the body of the response.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Request could not be processed because it contains invalid information");
        }
        return ResponseEntity.status(HttpStatus.OK).body(externalAccountProviderCollection);
    }
}
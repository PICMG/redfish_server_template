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
import org.picmg.redfish_server_template.RFmodels.custom.RedfishObject;
import org.picmg.redfish_server_template.repository.RedfishObjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

@Service
public class AccountService {

    @Value("${async.task.wait-time}")
    long taskWaitTime;

    @Autowired
    RedfishObjectRepository objectRepository;

    @Autowired
    TaskService taskService;

    @Autowired
    PasswordEncryptorService passwordEncryptorService;


    /* TODO: add back in if required
    public List<AccountService_ExternalAccountProvider> getAllAccountServiceV1110ExternalAccountProvider() {
        List<AccountService_ExternalAccountProvider> accountServiceV1110ExternalAccountProviderArrayList =
                accountServiceExternalAccountProviderRepository.findAll();
        return accountServiceV1110ExternalAccountProviderArrayList;
    }
    */

    /* TODO: add back in if required
    public Boolean validateUser(ManagerAccount_ManagerAccount account) throws UnknownProfileException {
        ManagerAccount_ManagerAccount userAccount = accountRepository.getByUserName(account.getUserName());
        if(account.getPassword() != null && userAccount.getPassword() != null &&
                account.getPassword().toString().equalsIgnoreCase(userAccount.getPassword().toString()))return true;
        return false;
    }
    */

    @Async
    //  This method assumes that the account parameter holds a valid account Redfish object
    //
    public Future<Boolean> updateUser(OffsetDateTime startTime, Integer taskId, RedfishObject account) throws  Exception {
        List<RedfishObject> userAccounts = objectRepository.findWithQuery(
                Criteria.where("_odata_type").is("ManagerAccount")
                        .and("UserName").is(account.get("UserName").toString()));
        try {
            if (userAccounts.isEmpty())
                throw new ChangeSetPersister.NotFoundException();
            if (userAccounts.size()>1)
                throw new ChangeSetPersister.NotFoundException();
            RedfishObject userAccount = userAccounts.get(0);
            if(!account.getName().isEmpty())
                userAccount.setName(account.get("UserName").toString());
            if(!account.getDescription().isEmpty())
                userAccount.setDescription(account.get("Description").toString());
            if(account.containsKey("Password")) {
                String encPassword = passwordEncryptorService.encryptPassword(account.get("Password").toString());
                userAccount.put("Password",encPassword);
            }
            if(account.containsKey("Enabled"))
                userAccount.put("Enabled",account.get("Enabled"));
            if(account.containsKey("RoleId"))
                userAccount.put("RoleId",account.get("RoleId"));
            if(account.containsKey("Locked"))
                userAccount.put("Locked",account.get("Locked"));

            // DEBUG: System.out.println("Async Account Service Complete");
            objectRepository.save(userAccount);
            if(startTime.getSecond() > taskWaitTime+1) {
                taskService.updateTaskState(taskId.toString(), Task_TaskState.COMPLETED, userAccount);
            }
        } catch (Exception e) {
            throw new Exception("Error While Updating Account");
        }
        return new AsyncResult<Boolean>(true);
    }

    public Boolean deleteAccount(RedfishObject account) throws ChangeSetPersister.NotFoundException {
        List<RedfishObject> userAccounts = objectRepository.findWithQuery(
                Criteria.where("_odata_type").is("ManagerAccount")
                        .and("UserName").is(account.get("UserName").toString()));
        if (userAccounts.isEmpty())
            throw new ChangeSetPersister.NotFoundException();
        if (userAccounts.size()>1)
            throw new ChangeSetPersister.NotFoundException();

        objectRepository.delete(userAccounts.get(0));
        return true;
    }

    @Async
    public Future<Boolean> addAccount(OffsetDateTime startTime, Integer taskId, RedfishObject account) throws Exception {
        List<RedfishObject> userAccounts = objectRepository.findWithQuery(
                Criteria.where("_odata_type").is("ManagerAccount")
                        .and("UserName").is(account.get("UserName").toString()));
        if (!userAccounts.isEmpty())
            throw new Exception("UserName " + account.get("UserName") + " Already Exists");

        List<RedfishObject> listAccount = objectRepository.findWithQuery(
                Criteria.where("_odata_type").is("ManagerAccount"));
        if(listAccount.isEmpty()) {
            account.setId("1");
        } else {
            long max = 0;
            for(RedfishObject account1 : listAccount){
                max = Math.max(max, Long.parseLong(account1.getId()));
            }
            max++;
            account.setId(max+"");
            account.setAtOdataId("/redfish/v1/AccountService/Accounts/"+max);
            if(account.containsKey("Password")) {
                String encPassword = passwordEncryptorService.encryptPassword(account.get("Password").toString());
                account.put("Password",encPassword);
            }
        }
        objectRepository.save(account);

        if(startTime.getSecond() > taskWaitTime+1) {
            taskService.updateTaskState(taskId.toString(), Task_TaskState.COMPLETED, account);
        }
        return new AsyncResult<Boolean>(true);
    }

    public List<String> getRoles() {
        List<String> result = new ArrayList<>();

        // get a list of all the roles available
        List<RedfishObject> roleObjects = objectRepository.findWithQuery(
                Criteria.where("_odata_type").is("Role"));
        if (roleObjects.isEmpty()) return result;

        // compose a list of roleIds from the list
        for (RedfishObject obj: roleObjects) {
            if (obj.containsKey("Id")) result.add(obj.get("Id").toString());
        }

        return result;
    }

    /* TODO: add back in if required
    public List<AccountService_AccountService> getAccountServiceListData() {
        List<AccountService_AccountService> accountServiceV1110AccountServiceList = accountServiceRepository.findAll();
        return accountServiceV1110AccountServiceList;
    }
    */

    /* TODO: add back in if required
    @Async
    public Future<List<AccountService_AccountService>> getALllAccounts(OffsetDateTime startTime, Integer taskId) {
        List<AccountService_AccountService> accountService_accountServiceList = accountServiceRepository.findAll();
        if( ChronoUnit.SECONDS.between(startTime, OffsetDateTime.now())  > taskWaitTime+1) {
            taskService.updateTaskState(taskId.toString(), Task_TaskState.COMPLETED, accountService_accountServiceList);
        }
        return new AsyncResult<List<AccountService_AccountService>>(accountService_accountServiceList);
    }
    */
    /* TODO: add back in if required
    public ManagerAccount_ManagerAccount getAccountById(String Id) throws ChangeSetPersister.NotFoundException {
        ManagerAccount_ManagerAccount userAccount = accountRepository.getById(Id);
        if(userAccount == null)
            throw new ChangeSetPersister.NotFoundException();
        return userAccount;
    }
    */
    /* TODO: add back in if required
    @Async
    public Future<RoleCollection> roleCollection(OffsetDateTime startTime, Integer taskId) throws Exception {
        RoleCollection roleCollection = new RoleCollection();
        List<Odata_IdRef> list = new ArrayList<>();
        List<Role_Role> rolesList = rolesRepository.findAll();
        for(Role_Role role : rolesList) {
            list.add(new Odata_IdRef().atOdataId(role.getAtOdataId()));
        }
        roleCollection.setMembersAtOdataCount((long) list.size());
        roleCollection.setMembers(list);
        if(startTime.getSecond() > taskWaitTime+1) {
            taskService.updateTaskState(taskId.toString(), Task_TaskState.COMPLETED, roleCollection);
        }
        return new AsyncResult<RoleCollection>(roleCollection);
    }
    */

    /* TODO: add back in if required
    @Async
    public Future<Role_Role> getRole(OffsetDateTime startTime, Integer taskId, String Id) throws ChangeSetPersister.NotFoundException {
        Role_Role roles = rolesRepository.getById(Id);
        if(roles == null)
            throw new ChangeSetPersister.NotFoundException();
        if(startTime.getSecond() > taskWaitTime+1) {
            taskService.updateTaskState(taskId.toString(), Task_TaskState.COMPLETED, roles);
        }
        return new AsyncResult<Role_Role>(roles);
    }
    */

}

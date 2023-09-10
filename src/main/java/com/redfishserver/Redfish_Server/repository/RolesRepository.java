//******************************************************************************************************
// RolesRepository.java
//
// Interface for RolesRepository.
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


package com.redfishserver.Redfish_Server.repository;

import com.redfishserver.Redfish_Server.RFmodels.AllModels.Role_Role;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface RolesRepository extends MongoRepository<Role_Role, Object> {
    @Query(value = "{'Id': ?0}")
    Role_Role getById(String Id);
}

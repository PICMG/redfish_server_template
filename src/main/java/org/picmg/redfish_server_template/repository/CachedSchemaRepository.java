//******************************************************************************************************
// TaskRepository.java
//
// Interface for TaskRepository.
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


package org.picmg.redfish_server_template.repository;

import org.picmg.redfish_server_template.RFmodels.custom.CachedSchema;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface CachedSchemaRepository extends MongoRepository<CachedSchema, String> {
    @Query(value="{ 'Id' : ?0 }")
    CachedSchema getById(String Id);

    // this will return the first schema that has a base name that matches the given parameter
    @Query(value="{ 'source' : ?0 }")
    CachedSchema getFirstBySource(String source);
}

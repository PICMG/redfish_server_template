//******************************************************************************************************
// RootService.java
//
// Root service according to redfish specification.
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

import org.picmg.redfish_server_template.RFmodels.custom.MetadataFile;
import org.picmg.redfish_server_template.RFmodels.custom.OdataFile;
import org.picmg.redfish_server_template.repository.MetadataFileRepository;
import org.picmg.redfish_server_template.repository.OdataFileRepository;
import org.picmg.redfish_server_template.repository.RedfishObjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class RootService {

    @Value("${async.task.wait-time}")
    long taskWaitTime;

    @Autowired
    TaskService taskService;

    @Autowired 
    MetadataFileRepository metadataFileRepository;

    @Autowired
    OdataFileRepository odataFileRepository;

    @Autowired
    RedfishObjectRepository objectRepository;

    public List<MetadataFile> getMetadataEntity() throws Exception {
        List<MetadataFile> list = metadataFileRepository.findAll();
        return list;
    }

    public List<OdataFile> getOdataEntity() throws Exception {
        List<OdataFile> list = odataFileRepository.findAll();
        return list;
    }
}

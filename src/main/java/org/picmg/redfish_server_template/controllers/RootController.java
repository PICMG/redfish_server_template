//******************************************************************************************************
// RootController.java
//
// Controller for Root service.
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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.picmg.redfish_server_template.RFmodels.custom.MetadataFile;
import org.picmg.redfish_server_template.RFmodels.custom.OdataFile;
import org.picmg.redfish_server_template.RFmodels.custom.RedfishObject;
import org.picmg.redfish_server_template.data_validation.ValidRedfishObject;
import org.picmg.redfish_server_template.repository.RedfishObjectRepository;
import org.picmg.redfish_server_template.services.RootService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/redfish")
public class RootController extends RedfishObjectController {
    @Autowired
    RootService rootService;

    @Autowired
    RedfishObjectRepository objectRepository;

    @GetMapping(value = "/")
    public RedirectView redirectVersion(RedirectAttributes attributes) {
        return new RedirectView("/redfish");
    }

    @GetMapping(value = "/v1")
    public RedirectView redirectServiceRoot(RedirectAttributes attributes) {
        return new RedirectView("/redfish/v1/");
    }

    @Override
    @GetMapping(value = "")
    public ResponseEntity<?> get(HttpServletRequest request) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body("{\"v1\":\"/redfish/v1/\"}");
    }

    @GetMapping(value = "/v1/")
    public ResponseEntity<?> getRootEntity() {
        RedfishObject root = objectRepository.findFirstWithQuery(Criteria.where("_odata_type").is("ServiceRoot"));
        if (root == null)
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Request succeeded, but no content is being returned in the body of the response.");
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = "{}";
        // remove any keys that start with underscore character
        root.remove("_odata_id");
        root.remove("_odata_type");
        root.remove("_id");
        try {
            json = mapper.writeValueAsString(root);
        } catch (Exception ignored) {
        }
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(json);
    }

    @GetMapping(value = "/v1/$metadata")
    public ResponseEntity<?> getMetadataEntity() {
        List<MetadataFile> metaList = new ArrayList<>();
        try {
            metaList = rootService.getMetadataEntity();
        } catch (Exception e) {
            // all other exceptions
            // TODO: Add correct Redfish error reporting
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(String.format("Request could not be processed because it contains invalid information"));
        }
        if (metaList.isEmpty())
            // TODO: Add correct Redfish error reporting
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Request succeeded, but no content is being returned in the body of the response.");
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(metaList.get(0).getData());
    }

    @GetMapping(value = "/v1/odata")
    public ResponseEntity<?> getOdataEntity() {
        List<OdataFile> odataList = new ArrayList<>();
        try {
            odataList = rootService.getOdataEntity();
        } catch (Exception e) {
            // all other exceptions
            // TODO: Add correct Redfish error reporting
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(String.format("Request could not be processed because it contains invalid information"));
        }
        if (odataList.isEmpty())
            // TODO: Add correct Redfish error reporting
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Request succeeded, but no content is being returned in the body of the response.");
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(odataList.get(0).getData());
    }
}
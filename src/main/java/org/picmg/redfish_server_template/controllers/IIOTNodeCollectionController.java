package org.picmg.redfish_server_template.controllers;

import org.picmg.redfish_server_template.RFmodels.AllModels.IIoTNodeCollection;
import org.picmg.redfish_server_template.RFmodels.AllModels.IIoTNode_IIoTNode;
import org.picmg.redfish_server_template.repository.IIoTNodeCollectionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/redfish/v1/IIoTNodes")
public class IIOTNodeCollectionController extends RedfishCollectionController<
        IIoTNodeCollection,
        IIoTNode_IIoTNode,
        IIoTNodeCollectionRepository> {
    @Override
    @GetMapping(value = "")
    public ResponseEntity<?> get(HttpServletRequest request) {
        return super.get(request);
    }
}


package org.picmg.redfish_server_template.controllers;

import org.picmg.redfish_server_template.RFmodels.AllModels.IIoTNodeCollection;
import org.picmg.redfish_server_template.RFmodels.AllModels.IIoTNode_IIoTNode;
import org.picmg.redfish_server_template.data_validation.ValidRedfishObject;
import org.picmg.redfish_server_template.repository.IIoTNodeCollectionRepository;
import org.picmg.redfish_server_template.repository.IIoTNodeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/redfish/v1/IIoTNodes")
public class IIOTNodeCollectionController extends RedfishCollectionController<
        IIoTNodeCollection,
        IIoTNodeCollectionRepository,
        IIoTNode_IIoTNode,
        IIoTNodeRepository>
{
    @Override
    @GetMapping(value = "")
    public ResponseEntity<?> get(HttpServletRequest request) {
        return super.get(request);
    }

    @Override
    @PostMapping(value = "")
    public ResponseEntity<?> post(@ValidRedfishObject("IIoTNode.json#/definitions/IIoTNode") IIoTNode_IIoTNode node, HttpServletRequest request) {
        return super.post(node, request);
    }

}


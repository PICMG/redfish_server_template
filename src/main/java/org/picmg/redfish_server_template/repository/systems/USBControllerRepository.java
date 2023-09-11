package org.picmg.redfish_server_template.repository.systems;

import org.picmg.redfish_server_template.RFmodels.AllModels.USBController_USBController;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface USBControllerRepository extends MongoRepository<USBController_USBController, Object> {
    USBController_USBController getById(String Id);
}

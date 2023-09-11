package org.picmg.redfish_server_template.repository.systems;

import org.picmg.redfish_server_template.RFmodels.AllModels.USBControllerCollection;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface USBControllerCollectionRepository extends MongoRepository<USBControllerCollection, Object> {
}

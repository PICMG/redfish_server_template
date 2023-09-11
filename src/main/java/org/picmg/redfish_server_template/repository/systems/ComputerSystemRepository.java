package org.picmg.redfish_server_template.repository.systems;

import org.picmg.redfish_server_template.RFmodels.AllModels.ComputerSystem_ComputerSystem;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ComputerSystemRepository extends MongoRepository<ComputerSystem_ComputerSystem, Object> {

    ComputerSystem_ComputerSystem findById(String Id);
}

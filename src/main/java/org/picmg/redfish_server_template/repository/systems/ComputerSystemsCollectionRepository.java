package org.picmg.redfish_server_template.repository.systems;

import org.picmg.redfish_server_template.RFmodels.AllModels.ComputerSystemCollection;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ComputerSystemsCollectionRepository extends MongoRepository<ComputerSystemCollection, Object> {
}

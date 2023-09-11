package org.picmg.redfish_server_template.repository.systems;

import org.picmg.redfish_server_template.RFmodels.AllModels.SimpleStorage_SimpleStorage;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SimpleStorageRepository extends MongoRepository<SimpleStorage_SimpleStorage, Object> {
    SimpleStorage_SimpleStorage getById(String Id);
}

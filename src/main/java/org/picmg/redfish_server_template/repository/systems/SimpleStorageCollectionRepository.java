package org.picmg.redfish_server_template.repository.systems;

import org.picmg.redfish_server_template.RFmodels.AllModels.SimpleStorageCollection;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SimpleStorageCollectionRepository extends MongoRepository<SimpleStorageCollection, Object> {
}

package org.picmg.redfish_server_template.repository.systems;

import org.picmg.redfish_server_template.RFmodels.AllModels.MemoryCollection;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MemoryCollectionRepository extends MongoRepository<MemoryCollection, Object> {
}

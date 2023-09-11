package org.picmg.redfish_server_template.repository.systems;

import org.picmg.redfish_server_template.RFmodels.AllModels.VirtualMediaCollection;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface VirtualMediaCollectionRepository extends MongoRepository<VirtualMediaCollection, Object> {
}

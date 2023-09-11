package org.picmg.redfish_server_template.repository.systems;

import org.picmg.redfish_server_template.RFmodels.AllModels.GraphicsControllerCollection;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GraphicsControllerCollectionRepository extends MongoRepository<GraphicsControllerCollection, Object> {
}

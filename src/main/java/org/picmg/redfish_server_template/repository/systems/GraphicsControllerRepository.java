package org.picmg.redfish_server_template.repository.systems;

import org.picmg.redfish_server_template.RFmodels.AllModels.GraphicsController_GraphicsController;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GraphicsControllerRepository extends MongoRepository<GraphicsController_GraphicsController, Object> {
    GraphicsController_GraphicsController getById(String Id);
}

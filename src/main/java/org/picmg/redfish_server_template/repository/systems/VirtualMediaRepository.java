package org.picmg.redfish_server_template.repository.systems;

import org.picmg.redfish_server_template.RFmodels.AllModels.VirtualMedia_VirtualMedia;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface VirtualMediaRepository extends MongoRepository<VirtualMedia_VirtualMedia, Object> {
    VirtualMedia_VirtualMedia getById(String Id);
}

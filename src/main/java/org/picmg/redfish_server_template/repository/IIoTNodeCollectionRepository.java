package org.picmg.redfish_server_template.repository;

import org.picmg.redfish_server_template.RFmodels.AllModels.IIoTNodeCollection;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IIoTNodeCollectionRepository extends RedfishCollectionRepository<IIoTNodeCollection> {
}

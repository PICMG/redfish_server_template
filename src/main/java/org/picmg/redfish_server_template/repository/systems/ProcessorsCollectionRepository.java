package org.picmg.redfish_server_template.repository.systems;

import org.picmg.redfish_server_template.RFmodels.AllModels.ProcessorCollection;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProcessorsCollectionRepository extends MongoRepository<ProcessorCollection, Object> {
}

package org.picmg.redfish_server_template.repository.systems;

import org.picmg.redfish_server_template.RFmodels.AllModels.Processor_Processor;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProcessorRepository extends MongoRepository<Processor_Processor, Object> {
    Processor_Processor getById(String Id);
}

package org.picmg.redfish_server_template.repository.systems;

import org.picmg.redfish_server_template.RFmodels.AllModels.ProcessorMetrics_ProcessorMetrics;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProcessorMetricsRepository extends MongoRepository<ProcessorMetrics_ProcessorMetrics, Object> {
}

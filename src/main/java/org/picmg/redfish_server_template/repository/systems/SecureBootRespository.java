package org.picmg.redfish_server_template.repository.systems;

import org.picmg.redfish_server_template.RFmodels.AllModels.SecureBoot_SecureBoot;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SecureBootRespository extends MongoRepository<SecureBoot_SecureBoot, Object> {
}

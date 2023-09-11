package org.picmg.redfish_server_template.repository.systems;

import org.picmg.redfish_server_template.RFmodels.AllModels.SecureBootDatabase_SecureBootDatabase;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SecureBootDatabaseRepository extends MongoRepository<SecureBootDatabase_SecureBootDatabase, Object> {
    SecureBootDatabase_SecureBootDatabase getById(String Id);
}

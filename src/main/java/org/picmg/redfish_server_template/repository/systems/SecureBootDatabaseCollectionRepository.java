package org.picmg.redfish_server_template.repository.systems;

import org.picmg.redfish_server_template.RFmodels.AllModels.SecureBootDatabaseCollection;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SecureBootDatabaseCollectionRepository extends MongoRepository<SecureBootDatabaseCollection, Object> {

}

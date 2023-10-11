package org.picmg.redfish_server_template.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface RedfishObjectRepository<T> extends MongoRepository<T,Object> {
    @Query(value="{ '_odata_id' : ?0 }")
    T getFirstByUri(String uri);
}

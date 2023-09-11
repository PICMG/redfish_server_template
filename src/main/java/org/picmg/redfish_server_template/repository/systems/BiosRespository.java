package org.picmg.redfish_server_template.repository.systems;

import org.picmg.redfish_server_template.RFmodels.AllModels.Bios_Bios;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BiosRespository extends MongoRepository<Bios_Bios, Object> {
    Bios_Bios getById(String Id);
}

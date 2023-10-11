package org.picmg.redfish_server_template.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.picmg.redfish_server_template.data_validation.RedfishObjectHandlerMethodArgumentResolver;
import org.picmg.redfish_server_template.repository.CachedSchemaRepository;
import org.picmg.redfish_server_template.services.RedfishErrorResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

// Configure automatic parameter checking for Redfish Objects
@Configuration
class RedfishValidationConfiguration implements WebMvcConfigurer {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    RedfishErrorResponseService redfishErrorResponseService;

    @Autowired
    CachedSchemaRepository cachedSchemaRepository;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        // add our resolver to the list of existing resolvers
        resolvers.add(new RedfishObjectHandlerMethodArgumentResolver(objectMapper, redfishErrorResponseService, cachedSchemaRepository));
    }
}


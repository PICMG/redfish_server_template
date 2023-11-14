//******************************************************************************************************
// MongoConfiguration.java
//
// Mongo configuration file.
//
//Copyright (C) 2022, PICMG.
//
//        This program is free software: you can redistribute it and/or modify
//        it under the terms of the GNU General Public License as published by
//        the Free Software Foundation, either version 3 of the License, or
//        (at your option) any later version.
//
//        This program is distributed in the hope that it will be useful,
//        but WITHOUT ANY WARRANTY; without even the implied warranty of
//        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//        GNU General Public License for more details.
//
//        You should have received a copy of the GNU General Public License
//        along with this program.  If not, see <https://www.gnu.org/licenses/>.
//*******************************************************************************************************


package org.picmg.redfish_server_template.config;


import org.picmg.redfish_server_template.RFmodels.custom.RedfishObject;
import org.picmg.redfish_server_template.config.converters.*;
import org.picmg.redfish_server_template.repository.RedfishObjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import static java.util.Arrays.asList;

@Configuration
public class MongoConfiguration {
    @Autowired
    private MongoDatabaseFactory dbFactory;

    @Autowired
    private MongoMappingContext mongoMappingContext;


    /*
    @Autowired
    private ReactiveMongoDatabaseFactory reactiveDbFactory;

    @Bean
    @Primary
    public ReactiveMongoTemplate reactiveMongoTemplate() {
        return new ReactiveMongoTemplate(reactiveDbFactory, mongoMappingConverter());
    }
*/
    @Bean
    @Primary
    public MappingMongoConverter mongoMappingConverter() {
        MappingMongoConverter mappingConverter =
                new MappingMongoConverter(new DefaultDbRefResolver(dbFactory), mongoMappingContext);

        // set up custom conversions for special types
        mappingConverter.setCustomConversions(customConversions());

        // when converting, replace dots with dots this is required for odata keys with
        mappingConverter.setMapKeyDotReplacement(".");
        return mappingConverter;
    }

    public MongoCustomConversions customConversions() {
        return new MongoCustomConversions(asList(
                new JsonNullableStringReadingConverter(),
                new JsonNullableReadingConverter(),
                new JsonNullableIntegerReadingConverter(),
                new JsonNullableDoubleReadingConverter(),
                new JsonNullableLongReadingConverter(),
                new MongoOffsetDateTimeReader(),
                new MongoOffsetDateTimeWriter()
        ));
    }
}

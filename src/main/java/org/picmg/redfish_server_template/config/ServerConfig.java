//******************************************************************************************************
// ServerConfig.java
//
// Configure HTTPS security and rerouting from HTTP to HTTPS
// Much of this code was based off of examples from the Spring Boot
// Reference document.
//
// Copyright (C) 2022, PICMG.
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

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration(proxyBeanMethods = false)
@EnableScheduling
public class ServerConfig {
    @Value ("${server.http_port}")
    Integer http_port;

    @Value ("${server.port}")
    Integer https_port;

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory>
    connectorCustomizer() {
        return (tomcat) -> tomcat.addAdditionalTomcatConnectors(createConnector());
    }
    private Connector createConnector() {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setScheme("http");
        connector.setPort(http_port);
        connector.setSecure(false);

        // our previously defined port on the other end of the connector
        connector.setRedirectPort(https_port);

        return connector;
    }
}
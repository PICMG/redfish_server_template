//******************************************************************************************************
// WebSecurityConfig.java
//
// Web Security configuration file.
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
import org.picmg.redfish_server_template.repository.PrivilegeTableRepository;
import org.picmg.redfish_server_template.repository.AccountService.RedfishAuthorizationManager;
import org.picmg.redfish_server_template.services.AccountService;
import org.picmg.redfish_server_template.services.jwt.JWTRequestFilters;
import org.picmg.redfish_server_template.services.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.OffsetDateTime;

@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    PrivilegeTableRepository privilegeTableRepository;

    @Autowired
    SessionService sessionService;

    @Autowired
    JWTRequestFilters jwtRequestFilters;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(sessionService).passwordEncoder(passwordEncoder());
    }

    @Autowired
    RedfishAuthorizationManager redfishAuthorizationManager;


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // if no user accounts exist, create an admin account with a default password of "test"
        http
                .requiresChannel(channel ->
                        channel.anyRequest().requiresSecure())
                .csrf((csrf) ->
                        csrf.disable()
                )
                .authorizeHttpRequests((authorizeHttpRequests) ->
                        authorizeHttpRequests
                                .antMatchers(HttpMethod.POST, "/redfish/v1/SessionService/Sessions").permitAll()
                                .antMatchers("/redfish").permitAll()
                                .antMatchers("/redfish/").permitAll()
                                .antMatchers("/redfish/v1").permitAll()
                                .antMatchers("/redfish/v1/").permitAll()
                                .antMatchers("/redfish/v1/odata").permitAll()
                                .antMatchers("/redfish/v1/$metadata").permitAll()
                                .antMatchers(HttpMethod.POST, "/redfish/v1/SessionService/Sessions").permitAll()
                                .anyRequest().access(redfishAuthorizationManager)
                )
                .httpBasic((configureHttpBasic) ->
                        configureHttpBasic.realmName("/redfish/v1/")
                )
                .sessionManagement((sessionManagement) ->
                        sessionManagement
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        //http.getSharedObject(AuthenticationManagerBuilder.class)
        //        .authenticationProvider(authProvider())
        //        .build();

        http.addFilterBefore(jwtRequestFilters, UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    // by default, hash passwords using this encoder
    public PasswordEncoder passwordEncoder(){
        //return NoOpPasswordEncoder.getInstance();
        return new BCryptPasswordEncoder(10);
    }

}

//******************************************************************************************************
// JWTRequestFilters.java
//
// JWT Request filter Service for account according to redfish.
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


package org.picmg.redfish_server_template.services.jwt;

import org.picmg.redfish_server_template.services.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class JWTRequestFilters extends OncePerRequestFilter {

    @Autowired
    SessionService sessionService;

    @Autowired
    JWTService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        String userName = null;
        String jwt = null;
        String userPassword;

        try {
            if(authHeader != null && authHeader.startsWith("Bearer")) {
                jwt = authHeader.substring(7);
                userName = jwtService.extractJWTUsername(jwt);
            } else if(authHeader != null && authHeader.startsWith("Basic")) {
                String creds = authHeader.substring("Basic".length()).trim();
                byte[] credsDecoded = Base64.getDecoder().decode(creds);
                String credString = new String(credsDecoded, StandardCharsets.UTF_8);
                userName = credString.substring(0,credString.indexOf(':'));
                userPassword = credString.substring(userName.length()+1);
                UserDetails userDetails = this.sessionService.loadUserByUsername(userName);
                if (userDetails.getPassword().equals(userPassword) && userDetails.isEnabled() &&
                        userDetails.isAccountNonLocked() && userDetails.isAccountNonExpired() &&
                        userDetails.isCredentialsNonExpired()) {
                    // here if password was authenticated

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // otherwise, check for Redfish Authentication
        if(userName!=null && SecurityContextHolder.getContext().getAuthentication()==null) {
            UserDetails userDetails = this.sessionService.loadUserByUsername(userName);
            try {
                if(jwtService.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
                    usernamePasswordAuthenticationToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        filterChain.doFilter(request, response);
    }
}

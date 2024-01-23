package org.picmg.redfish_server_template.repository.AccountService;

import org.picmg.redfish_server_template.RFmodels.custom.PrivilegeTableEntry;
import org.picmg.redfish_server_template.repository.PrivilegeTableRepository;
import org.picmg.redfish_server_template.services.PrivilegeTableService;
import org.picmg.redfish_server_template.services.SchemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

@Component
public final class RedfishAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {
    @Autowired
    PrivilegeTableService privilegeTableService;

    @Override
    public void verify(Supplier<Authentication> authentication, RequestAuthorizationContext object) {
        AuthorizationDecision decision = check(authentication, object);
        if (!decision.isGranted()) {
            if (!authentication.get().isAuthenticated()) {
                throw new AccessDeniedException("Access Denied");
            }
        }
    }

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext object) {
        // get the authorities for the authenticated user
        Collection<? extends GrantedAuthority> authorities = authentication.get().getAuthorities();
        if (authorities.isEmpty()) return new AuthorizationDecision(false);

        String method = object.getRequest().getMethod();
        String uri = object.getRequest().getRequestURI();

        // DEBUG: temporary for testing only
        if (uri.contains("IIoT")) {
            return new AuthorizationDecision(true);
        }
        if (uri.contains("/Actions/")&&(method.equals("POST"))) {
            // if the uri is for an action, and the request is for a post, use the privileges for the
            // base object
            PrivilegeTableEntry entry = privilegeTableService.getPrivilegeTableEntryFromUri(uri.substring(0,uri.indexOf("/Actions/")-1));
            if ((entry != null) && (entry.isAuthorized(method, authorities)))
                return new AuthorizationDecision(true);
        } else {
            // otherwise, use the privileges for the base object
            PrivilegeTableEntry entry = privilegeTableService.getPrivilegeTableEntryFromUri(uri);
            if ((entry != null) && (entry.isAuthorized(method, authorities)))
                return new AuthorizationDecision(true);
        }

        return new AuthorizationDecision(false);
    }

    @Bean
    public RedfishAuthorizationManager redfishAuthorizationManagerBean() throws Exception {
        return new RedfishAuthorizationManager();
    }
}

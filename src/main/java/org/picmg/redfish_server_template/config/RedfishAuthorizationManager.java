package org.picmg.redfish_server_template.config;

import org.picmg.redfish_server_template.RFmodels.custom.PrivilegeTableEntry;
import org.picmg.redfish_server_template.repository.AccountService.PrivilegeTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class RedfishAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {
    @Autowired
    PrivilegeTableRepository privilegeTableRepository;

    @Override
    public void verify(Supplier<Authentication> authentication, RequestAuthorizationContext object) {
        AuthorizationDecision decision = check(authentication, object);
        if (decision == null || !decision.isGranted()) {
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
        String localAddress = object.getRequest().getLocalAddr();
        String uri = object.getRequest().getRequestURI();

        List<PrivilegeTableEntry> x = privilegeTableRepository.findAll();
        for (PrivilegeTableEntry entry: x) {
            if (!entry.isMatchingUrl(uri)) continue;
            if (entry.isAuthorized(method, authorities)) new AuthorizationDecision(true);
        }
        return new AuthorizationDecision(false);
    }
}

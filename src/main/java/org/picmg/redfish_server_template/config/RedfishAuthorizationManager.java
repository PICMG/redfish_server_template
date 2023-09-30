package org.picmg.redfish_server_template.config;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.function.Supplier;

public class RedfishAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {
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
        boolean result = false;
        for (GrantedAuthority grantedAuthority: authorities) {
            // unauthenticated users will have a granted authority of ANONYMOUS
            if (grantedAuthority.getAuthority().equals("ROLE_ANONYMOUS")) continue;

            // here the authority needs to be checked against the privilege level for the redfish model
            // located at the url being accessed.
            result = true;
            String method = object.getRequest().getMethod();
            String localAddress = object.getRequest().getLocalAddr();
            String uri = object.getRequest().getRequestURI();
            break;
        }
        return new AuthorizationDecision(result);
    }
}

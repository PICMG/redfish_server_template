package org.picmg.redfish_server_template.repository.AccountService;

import org.picmg.redfish_server_template.RFmodels.custom.PrivilegeTableEntry;
import org.picmg.redfish_server_template.RFmodels.custom.RedfishObject;
import org.picmg.redfish_server_template.repository.PrivilegeTableRepository;
import org.picmg.redfish_server_template.repository.RedfishObjectRepository;
import org.picmg.redfish_server_template.services.AccountService;
import org.picmg.redfish_server_template.services.PasswordEncoderService;
import org.picmg.redfish_server_template.services.PrivilegeTableService;
import org.picmg.redfish_server_template.services.SchemaService;
import org.picmg.redfish_server_template.services.jwt.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

@Component
public final class RedfishAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {
    @Autowired
    PrivilegeTableService privilegeTableService;

    @Autowired
    RedfishObjectRepository objectRepository;

    @Autowired
    JWTService jwtService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AccountService accountService;

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
        if (method==null) return new AuthorizationDecision(false);
        String uri = object.getRequest().getRequestURI();

        String username = authentication.get().getName();
        RedfishObject userAccount = objectRepository.findFirstWithQuery(
                Criteria.where("_odata_type").is("ManagerAccount")
                        .and("UserName").is(username));
        HttpServletRequest req = object.getRequest();
        String requri = req.getRequestURI();

        if (userAccount == null) return new AuthorizationDecision(false);
        boolean restrictedAccess = accountService.isPasswordChangeRequired(userAccount);

        // first, see if an entry exists for the uri, and use that
        PrivilegeTableEntry entry = privilegeTableService.getPrivilegeTableEntryFromUri(uri);
        if ((entry != null) && (entry.isAuthorized(method, authorities)) && (!restrictedAccess))
            return new AuthorizationDecision(true);

        // otherwise, if the uri is an action, try to use the privileges for the base resource
        if (uri.contains("/Actions/")&&(method.equals("POST"))) {
            entry = privilegeTableService.getPrivilegeTableEntryFromUri(uri.substring(0,uri.indexOf("/Actions/")));
            if ((entry != null) && (entry.isAuthorized(method, authorities)) && (!restrictedAccess))
                return new AuthorizationDecision(true);
        }

        // otherwise, if the uri is an SSE request, try to use the privileges for the base's subscription resource
        if (uri.contains("/SSE")&&(method.equals("GET"))) {
            entry = privilegeTableService.getPrivilegeTableEntryFromUri(uri.substring(0,uri.indexOf("/SSE"))+"/Subscriptions");
            if ((entry != null) && (entry.isAuthorized(method, authorities)) && (!restrictedAccess))
                return new AuthorizationDecision(true);
        }

        // A PATCH operation on the ManagerAccount to change the PW (RequireChangePasswordAction=false)
        if ((req.getMethod().equals("PATCH")) && (userAccount.getAtOdataId().equals(requri)) &&
                (!accountService.isChangePasswordActionRequired()) && (!restrictedAccess))
        {
            return new AuthorizationDecision(true);
        }

        // check for accesses that are allowed with restricted access
        if (restrictedAccess) {
            String actionBase = "";
            String actionName = "";
            RedfishObject referencedResource = objectRepository.findFirstWithQuery(Criteria.where("_odata_id").is(requri));
            if (requri.contains("/Actions/")) {
                actionBase = requri.substring(0, requri.indexOf("/Actions/"));
                actionName = requri.substring(requri.lastIndexOf('.')+1);
            }

            // there are very few actions that are allowed with password change required.  According to the
            // redfish specification, these are:

            // A session login
            if ((req.getMethod().equals("POST"))&&(requri.matches("^/redfish/v1/SessionService/[^\\/]+$"))) {return new AuthorizationDecision(true);}
            // A GET operation on the ManagerAccount resource associated with the account.
            else if ((req.getMethod().equals("GET"))&&(userAccount.getAtOdataId().equals(requri))) {return new AuthorizationDecision(true);}
            // A PATCH operation on the ManagerAccount to change the PW (RequireChangePasswordAction=false)
            else if ((req.getMethod().equals("PATCH"))&&(userAccount.getAtOdataId().equals(requri))&&(!accountService.isChangePasswordActionRequired())) {return new AuthorizationDecision(true);}
            // A POST operation on the ChangePassword action
            else if ((req.getMethod().equals("POST"))&&(userAccount.getAtOdataId().equals(actionBase))&&(actionName.equals("ChangePassword"))) {return new AuthorizationDecision(true);}
            // A DELETE operation on Session resources representing open sessions associated with the account.
            else if ((req.getMethod().equals("DELETE"))&&
                    (referencedResource != null) && (referencedResource.getAtOdataType().equals("Session")) &&
                    (referencedResource.containsKey("UserName"))&&(referencedResource.getString("UserName").equals(username))) {
                return new AuthorizationDecision(true);
            }
        }

        return new AuthorizationDecision(false);
    }

    @Bean
    public RedfishAuthorizationManager redfishAuthorizationManagerBean() throws Exception {
        return new RedfishAuthorizationManager();
    }
}

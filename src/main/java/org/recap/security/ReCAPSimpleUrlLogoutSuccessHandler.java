package org.recap.security;

import org.recap.RecapConstants;
import org.recap.spring.PropertyValueProvider;
import org.recap.util.HelperUtil;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by sheiks on 20/01/17.
 */
public class ReCAPSimpleUrlLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        String institution = (String)((ServletRequestAttributes) requestAttributes).getRequest().getAttribute(RecapConstants.RECAP_INSTITUTION_ID);

        String urlProperty = RecapConstants.CAS + institution + RecapConstants.SERVICE_LOGOUT;
        PropertyValueProvider propertyValueProvider = HelperUtil.getBean(PropertyValueProvider.class);
        String url = propertyValueProvider.getProperty(urlProperty);
        String redirectUri= propertyValueProvider.getProperty(RecapConstants.REDIRECT_URI);

        String casLogoutUrl = url + redirectUri;

        UserInstitutionCache userInstitutionCache = HelperUtil.getBean(UserInstitutionCache.class);
        CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
        String token = csrfToken.getToken();
        userInstitutionCache.removeCsrf(token);
        return casLogoutUrl;
    }
}

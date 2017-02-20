package org.recap.security;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.recap.RecapConstants;
import org.recap.spring.PropertyValueProvider;
import org.recap.util.HelperUtil;
import org.recap.util.UserAuthUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Created by sheiks on 20/01/17.
 */
public class ReCAPSimpleUrlLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {


    private UserAuthUtil userAuthUtil;

    public ReCAPSimpleUrlLogoutSuccessHandler(UserAuthUtil userAuthUtil) {
        this.userAuthUtil = userAuthUtil;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        HttpSession session = request.getSession();
        Object attribute = session.getAttribute(UserManagement.USER_TOKEN);
        super.onLogoutSuccess(request, response, authentication);
        logger.info("Subject Logged out");
        try{
            userAuthUtil.authorizedUser(RecapConstants.SCSB_SHIRO_LOGOUT_URL,(UsernamePasswordToken) attribute);
        }catch (Exception e) {
            logger.error(RecapConstants.LOG_ERROR,e);
        }

        UserInstitutionCache userInstitutionCache = HelperUtil.getBean(UserInstitutionCache.class);
        CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
        String token = csrfToken.getToken();
        userInstitutionCache.removeCsrf(token);
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        String institution = (String)((ServletRequestAttributes) requestAttributes).getRequest().getAttribute(RecapConstants.RECAP_INSTITUTION_ID);

        String casLogoutUrl;
        if (StringUtils.equals(institution, "3")) {
            casLogoutUrl = "/"; // Todo : Need to get the corresponding logout url from NYPL
        } else {
            String urlProperty = RecapConstants.CAS + institution + RecapConstants.SERVICE_LOGOUT;
            PropertyValueProvider propertyValueProvider = HelperUtil.getBean(PropertyValueProvider.class);
            String url = propertyValueProvider.getProperty(urlProperty);
            String redirectUri= propertyValueProvider.getProperty(RecapConstants.REDIRECT_URI);

            casLogoutUrl = url + redirectUri;
        }
        return casLogoutUrl;
    }
}

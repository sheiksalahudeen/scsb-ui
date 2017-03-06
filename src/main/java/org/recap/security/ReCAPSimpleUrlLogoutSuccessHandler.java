package org.recap.security;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.recap.RecapConstants;
import org.recap.spring.PropertyValueProvider;
import org.recap.util.HelperUtil;
import org.recap.util.UserAuthUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
        Object attribute = request.getAttribute(UserManagement.USER_TOKEN);
        super.onLogoutSuccess(request, response, authentication);
        logger.info("Subject Logged out");
        try{
            userAuthUtil.authorizedUser(RecapConstants.SCSB_SHIRO_LOGOUT_URL,(UsernamePasswordToken) attribute);
        }catch (Exception e) {
            e.printStackTrace();
        }
        request.removeAttribute(UserManagement.USER_TOKEN);
        request.removeAttribute(RecapConstants.RECAP_INSTITUTION_CODE);

        Cookie[] cookies = request.getCookies();
        if (null != cookies) {
            for(Cookie cookie : cookies) {
                if(StringUtils.equals(cookie.getName(), RecapConstants.RECAP_INSTITUTION_CODE)) {
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                }
            }
        }
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        String institution = (String)((ServletRequestAttributes) requestAttributes).getRequest().getAttribute(RecapConstants.RECAP_INSTITUTION_CODE);

        String casLogoutUrl = null;
        if (StringUtils.equals(institution, "NYPL")) {
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

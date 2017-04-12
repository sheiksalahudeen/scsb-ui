package org.recap.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.recap.RecapConstants;
import org.recap.spring.ApplicationContextProvider;
import org.recap.spring.PropertyValueProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by sheiks on 20/01/17.
 */
public class HelperUtil {

    public static String getAttributeValueFromRequest(HttpServletRequest request, String key) {
        return (String) request.getAttribute(key);
    }

    public static <T> T getBean(Class<T> requiredType) {
        ApplicationContext applicationContext = ApplicationContextProvider.getInstance().getApplicationContext();
        return applicationContext.getBean(requiredType);
    }

    public static String getInstitutionFromRequest(HttpServletRequest request) {
        String institution = request.getParameter("institution");
        if(StringUtils.isBlank(institution)) {
            institution = (String) request.getAttribute(RecapConstants.RECAP_INSTITUTION_CODE);
        }
        return institution;
    }

    public static String getLogoutUrl(String institutionCode) {
        String casLogoutUrl;
        if (StringUtils.equals(institutionCode, RecapConstants.NYPL)) {
            casLogoutUrl = RecapConstants.VIEW_HOME; // Todo : Need to get the corresponding logout url from NYPL
        } else {
            String urlProperty = RecapConstants.CAS + institutionCode + RecapConstants.SERVICE_LOGOUT;
            PropertyValueProvider propertyValueProvider = HelperUtil.getBean(PropertyValueProvider.class);
            String url = propertyValueProvider.getProperty(urlProperty);
            String redirectUri= propertyValueProvider.getProperty(RecapConstants.REDIRECT_URI);
            casLogoutUrl = url + redirectUri;
        }
        return casLogoutUrl;
    }

    public static void logoutFromShiro(Object attribute) {
        if (null != attribute) {
            try{
                UserAuthUtil userAuthUtil = HelperUtil.getBean(UserAuthUtil.class);
                userAuthUtil.authorizedUser(RecapConstants.SCSB_SHIRO_LOGOUT_URL,(UsernamePasswordToken) attribute);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isAnonymousUser(Authentication auth) {
        if(StringUtils.equals(auth.getName(), RecapConstants.ANONYMOUS_USER)) {
            Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
            for (Iterator<? extends GrantedAuthority> iterator = authorities.iterator(); iterator.hasNext(); ) {
                GrantedAuthority grantedAuthority = iterator.next();
                if(StringUtils.equals(grantedAuthority.getAuthority(), RecapConstants.ROLE_ANONYMOUS)) {
                    return true;
                }
            }
        }
        return false;
    }
}


package org.recap.util;

import org.apache.commons.lang3.StringUtils;
import org.recap.RecapConstants;
import org.recap.spring.ApplicationContextProvider;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

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
        if(StringUtils.isBlank(institution)) {
            Cookie[] cookies = request.getCookies();
            if (null != cookies) {
                for(Cookie cookie : cookies) {
                    if(StringUtils.equals(cookie.getName(), RecapConstants.RECAP_INSTITUTION_CODE)) {
                        institution = cookie.getValue();
                    }
                }
            }
        }
        return institution;
    }
}


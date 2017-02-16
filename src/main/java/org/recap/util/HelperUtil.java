package org.recap.util;

import org.apache.commons.lang3.StringUtils;
import org.recap.spring.ApplicationContextProvider;
import org.springframework.context.ApplicationContext;

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
}


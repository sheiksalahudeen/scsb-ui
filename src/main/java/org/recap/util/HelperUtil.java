package org.recap.util;

import org.recap.spring.ApplicationContextProvider;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by sheiks on 20/01/17.
 */
public class HelperUtil {

    public static String getParamSingleValueFromRequest(HttpServletRequest request, String key) {
        String[] values = request.getParameterMap().get(key);
        if(null != values && values.length > 0) {
            return values[0];
        }
        return null;
    }

    public static <T> T getBean(Class<T> requiredType) {
        ApplicationContext applicationContext = ApplicationContextProvider.getInstance().getApplicationContext();
        return applicationContext.getBean(requiredType);
    }
}


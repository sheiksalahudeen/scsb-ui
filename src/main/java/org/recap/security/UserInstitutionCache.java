package org.recap.security;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sheiks on 23/01/17.
 */
@Component
public class UserInstitutionCache {

    private Map<String, String> csrfAndInstitutionType= new HashMap<>();

    public void addCsrf(String key, String value) {
        csrfAndInstitutionType.put(key, value);
    }

    public String getInstitutionForCsrf(String key) {
        return csrfAndInstitutionType.get(key);
    }

    public void removeCsrf(String key) {
        csrfAndInstitutionType.remove(key);
    }
}

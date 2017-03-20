package org.recap.util;

import org.apache.shiro.authc.UsernamePasswordToken;
import org.recap.RecapConstants;
import org.recap.model.usermanagement.UserDetailsForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dharmendrag on 4/1/17.
 */
@Service
public class UserAuthUtil {

    private static final Logger logger = LoggerFactory.getLogger(UserAuthUtil.class);

    @Value("${server.protocol}")
    String serverProtocol;

    @Value("${scsb.shiro}")
    String scsbShiro;


    public Map<String,Object> doAuthentication(UsernamePasswordToken token) throws Exception{
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<UsernamePasswordToken> requestEntity = new HttpEntity<>(token, RecapConstants.getHttpHeaders());
            return restTemplate.postForObject(serverProtocol + scsbShiro + RecapConstants.SCSB_SHIRO_AUTHENTICATE_URL, requestEntity, HashMap.class);
    }

    public Boolean authorizedUser(String serviceURL, UsernamePasswordToken token)
    {

        Boolean statusResponse = false;
        try {

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<UsernamePasswordToken> requestEntity = new HttpEntity<>(token, RecapConstants.getHttpHeaders());
            statusResponse = restTemplate.postForObject(serverProtocol + scsbShiro + serviceURL, requestEntity, Boolean.class);
        } catch (Exception e) {
            logger.error(RecapConstants.LOG_ERROR,e);
        }
        return statusResponse;
    }

    public UserDetailsForm getUserDetails(HttpSession session,String recapPermission)
    {
        UserDetailsForm userDetailsForm=new UserDetailsForm();
        userDetailsForm.setSuperAdmin((Boolean)session.getAttribute(RecapConstants.SUPER_ADMIN_USER));
        userDetailsForm.setRecapUser((Boolean)session.getAttribute(RecapConstants.RECAP_USER));
        userDetailsForm.setLoginInstitutionId((Integer)session.getAttribute(RecapConstants.USER_INSTITUTION));
        userDetailsForm.setRecapPermissionAllowed((Boolean) session.getAttribute(recapPermission));
        return userDetailsForm;
    }

}

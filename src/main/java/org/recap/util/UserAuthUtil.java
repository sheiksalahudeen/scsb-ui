package org.recap.util;

import org.apache.shiro.authc.UsernamePasswordToken;
import org.recap.RecapConstants;
import org.recap.model.userManagement.UserDetailsForm;
import org.recap.security.UserManagement;
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

    Logger logger = LoggerFactory.getLogger(UserAuthUtil.class);

    @Value("${server.protocol}")
    String serverProtocol;

    @Value("${scsb.shiro}")
    String scsbShiro;


    public Map<String,Object> doAuthentication(UsernamePasswordToken token) throws Exception{
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<UsernamePasswordToken> requestEntity = new HttpEntity<>(token, RecapConstants.getHttpHeaders());
            return restTemplate.postForObject(serverProtocol + scsbShiro + RecapConstants.SCSB_SHIRO_AUTHENTICATE_URL, requestEntity, HashMap.class);
    }

    public boolean authorizedUser(String serviceURL,UsernamePasswordToken token)
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
        userDetailsForm.setSuperAdmin((Boolean)session.getAttribute(UserManagement.SUPER_ADMIN_USER));
        userDetailsForm.setRecapUser((Boolean)session.getAttribute(UserManagement.ReCAP_USER));
        userDetailsForm.setLoginInstitutionId((Integer)session.getAttribute(UserManagement.USER_INSTITUTION));
        userDetailsForm.setRecapPermissionAllowed((Boolean) session.getAttribute(recapPermission));
        return userDetailsForm;
    }

}

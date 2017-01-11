package org.recap.util;

import org.apache.shiro.authc.UsernamePasswordToken;
import org.recap.RecapConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<UsernamePasswordToken> requestEntity = new HttpEntity<>(token, RecapConstants.getHttpHeaders());
            Map<String, Object> resultMap = restTemplate.postForObject(serverProtocol + scsbShiro + RecapConstants.SCSB_SHIRO_AUTHENTICATE_URL, requestEntity, HashMap.class);
            return resultMap;
        }catch(Exception e)
        {
            logger.error("Exception in Auth service call :"+e.getMessage());
            throw new Exception(e);
        }
    }

    public boolean authorizedUser(String serviceURL,UsernamePasswordToken token)
    {

        Boolean statusResponse = false;
        try {

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<UsernamePasswordToken> requestEntity = new HttpEntity<>(token, RecapConstants.getHttpHeaders());
            statusResponse = restTemplate.postForObject(serverProtocol + scsbShiro + serviceURL, requestEntity, Boolean.class);
            return  statusResponse;
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return false;
    }

}

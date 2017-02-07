package org.recap.util;

import org.apache.shiro.authc.UsernamePasswordToken;
import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.RecapConstants;
import org.recap.model.userManagement.UserForm;
import org.recap.security.UserManagement;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by dharmendrag on 7/2/17.
 */
public class UserAuthUtilUT extends BaseTestCase {

    @Autowired
    UserAuthUtil userAuthUtil;

    @Test
    public void testLoginAndAuthorization()throws Exception{
        Map<String,Object> resultmap=null;
        boolean authorized=false;
        UserForm userForm=new UserForm();
        userForm.setUsername("john");
        userForm.setInstitution(2);
        userForm.setPassword("123");
        UsernamePasswordToken token=new UsernamePasswordToken(userForm.getUsername()+ UserManagement.TOKEN_SPLITER.getValue()+userForm.getInstitution(),userForm.getPassword(),true);
        resultmap=(Map<String,Object>)userAuthUtil.doAuthentication(token);

        assertTrue((Boolean) resultmap.get("isAuthenticated"));
        assertEquals(userForm.getUsername(),resultmap.get("userName"));
        assertEquals(userForm.getInstitution(),resultmap.get(UserManagement.USER_ID));

        authorized=userAuthUtil.authorizedUser(RecapConstants.SCSB_SHIRO_COLLECTION_URL,token);

        assertTrue(authorized);

    }

}

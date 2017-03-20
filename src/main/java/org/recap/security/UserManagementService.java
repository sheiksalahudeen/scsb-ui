package org.recap.security;

import org.apache.shiro.authc.UsernamePasswordToken;
import org.recap.RecapConstants;
import org.slf4j.Logger;
import org.recap.repository.jpa.RolesDetailsRepositorty;
import org.recap.model.jpa.RoleEntity;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpSession;

/**
 * Created by dharmendrag on 15/12/16.
 */
@Service
public class UserManagementService {

    @Autowired
    RolesDetailsRepositorty rolesDetailsRepositorty;

    public static String unAuthorizedUser(HttpSession session, String moduleName, Logger logger)
    {
        final String loginScreen="redirect:/";
        logger.debug(moduleName+" authorization Rejected for :"+(UsernamePasswordToken)session.getAttribute(RecapConstants.USER_TOKEN));
        if(session!=null){
            session.invalidate();
        }
        return loginScreen;
    }

    public Integer getSuperAdminRoleId(){
        RoleEntity roleEntity=rolesDetailsRepositorty.findByRoleName("Super Admin");
        return roleEntity.getRoleId();
    }
}

package org.recap.controller;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.recap.RecapConstants;
import org.recap.model.userManagement.LoginValidator;
import org.recap.model.userManagement.UserForm;
import org.recap.security.UserManagement;
import org.recap.util.UserAuthUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.ResourceAccessException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.net.ConnectException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;


/**
 * Created by dharmendrag on 25/11/16.
 */
@Controller
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    private LoginValidator loginValidator=new LoginValidator();

    @Autowired
    private UserAuthUtil userAuthUtil;


    @Autowired
    private TokenStore tokenStore;


    @RequestMapping(value="/",method= RequestMethod.GET)
    public String loginScreen(HttpServletRequest request, Model model, @ModelAttribute UserForm userForm) {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        if(null != auth && !isAnonymousUser(auth)) {
//            return "redirect:/searchPage";
//        }
        logger.info("Login Screen called");
        return RecapConstants.VIEW_LOGIN;
    }



    @RequestMapping(value = "/login-scsb", method = RequestMethod.GET)
    public String login(@Valid @ModelAttribute UserForm userForm, HttpServletRequest request, Model model) {

        OAuth2Authentication auth = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
        String tokenString = "";
        OAuth2AccessToken accessToken = tokenStore.readAccessToken(tokenString);

        JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter();
        Map<String, ?> stringMap = jwtAccessTokenConverter.convertAccessToken(accessToken, auth);

        OAuth2Authentication oAuth2Authentication = tokenStore.readAuthentication(tokenString);

        String user = auth.getName();
        logger.info("passing in /login");
        model.addAttribute("user", user);
        final String loginScreen=RecapConstants.VIEW_LOGIN;
        Map<String,Object> resultmap=null;
        userForm.setUsername(user);
        userForm.setPassword("");
        try
        {
            UsernamePasswordToken token=new UsernamePasswordToken(userForm.getUsername()+ UserManagement.TOKEN_SPLITER.getValue()+userForm.getInstitution(),userForm.getPassword(),true);
            resultmap=userAuthUtil.doAuthentication(token);

            if(!(Boolean) resultmap.get("isAuthenticated"))
            {
                throw new Exception("Subject Authtentication Failed");
            }
            HttpSession session=request.getSession(true);
            session.setAttribute("token",token);
            session.setAttribute(UserManagement.USER_AUTH,resultmap);
            setValuesInSession(session,resultmap);

        }
        catch(Exception e)
        {
            logger.error(RecapConstants.LOG_ERROR,e);
            logger.error("Exception occured in authentication : "+e.getLocalizedMessage());
            return loginScreen;
        }


        return "redirect:/searchPage";
    }


    @RequestMapping(value="/",method= RequestMethod.POST)
    public String createSession(@Valid @ModelAttribute UserForm userForm, HttpServletRequest request, Model model, BindingResult error){
        loginValidator.validate(userForm,error);
        final String loginScreen=RecapConstants.VIEW_LOGIN;
        Map<String,Object> resultmap=null;
        if(userForm==null){
            return loginScreen;
        }
        try
        {
            if(error.hasErrors())
            {
                logger.debug("Login Screen validation failed");
                return loginScreen(request,model,userForm);
            }
            UsernamePasswordToken token=new UsernamePasswordToken(userForm.getUsername()+ UserManagement.TOKEN_SPLITER.getValue()+userForm.getInstitution(),userForm.getPassword(),true);
            resultmap=userAuthUtil.doAuthentication(token);

            if(!(Boolean) resultmap.get("isAuthenticated"))
            {
                throw new Exception("Subject Authtentication Failed");
            }
            HttpSession session=request.getSession();
            session.setAttribute(UserManagement.USER_TOKEN,token);
            session.setAttribute(UserManagement.USER_AUTH,resultmap);
            setValuesInSession(session,resultmap);
        }
        catch(ConnectException|ResourceAccessException e)
        {
            logger.error(RecapConstants.LOG_ERROR,e);
            error.rejectValue("wrongCredentials","error.invalid.credentials","Connection Error.Please contact our staff");
            logger.error("Exception occured in connection : "+e.getLocalizedMessage());
            return loginScreen;
        }
        catch(Exception e)
        {
            logger.error(RecapConstants.LOG_ERROR,e);
            error.rejectValue("wrongCredentials","error.invalid.credentials","Invalid Credentials");
            logger.debug("Exception occured in authentication Process : {} ",resultmap.get(UserManagement.USER_AUTH_ERRORMSG));
            logger.error("{} : {}",e.getLocalizedMessage(),resultmap.get(UserManagement.USER_AUTH_ERRORMSG));
            return loginScreen;
        }
        return "redirect:/searchPage";

    }

    @RequestMapping("/logout")
    public String logoutUser(HttpServletRequest request){
        logger.info("Subject Logged out");
        HttpSession session=null;
        try{
            session=request.getSession();
            userAuthUtil.authorizedUser(RecapConstants.SCSB_SHIRO_LOGOUT_URL,(UsernamePasswordToken)session.getAttribute(UserManagement.USER_TOKEN));
        }finally{
            session.invalidate();
            return "redirect:/";
        }
    }

    private void setValuesInSession(HttpSession session,Map<String,Object> authMap)
    {
        session.setAttribute("userName",(String)authMap.get("userName"));
        session.setAttribute(UserManagement.USER_ID,(Integer)authMap.get(UserManagement.USER_ID));
        session.setAttribute(UserManagement.USER_INSTITUTION,(Integer)authMap.get(UserManagement.USER_INSTITUTION));
        session.setAttribute(UserManagement.SUPER_ADMIN_USER,(Boolean)authMap.get(UserManagement.SUPER_ADMIN_USER));
        session.setAttribute(UserManagement.ReCAP_USER,(Boolean)authMap.get(UserManagement.ReCAP_USER));
        session.setAttribute(UserManagement.REQUEST_PRIVILEGE,(Boolean)authMap.get(UserManagement.REQUEST_PRIVILEGE));
        session.setAttribute(UserManagement.COLLECTION_PRIVILEGE,(Boolean)authMap.get(UserManagement.COLLECTION_PRIVILEGE));
        session.setAttribute(UserManagement.REPORTS_PRIVILEGE,(Boolean)authMap.get(UserManagement.REPORTS_PRIVILEGE));
        session.setAttribute(UserManagement.SEARCH_PRIVILEGE,(Boolean)authMap.get(UserManagement.SEARCH_PRIVILEGE));
        session.setAttribute(UserManagement.USER_ROLE_PRIVILEGE,(Boolean)authMap.get(UserManagement.USER_ROLE_PRIVILEGE));
        session.setAttribute(UserManagement.REQUEST_ALL_PRIVILEGE,(Boolean)authMap.get(UserManagement.REQUEST_ALL_PRIVILEGE));
        session.setAttribute(UserManagement.REQUEST_ITEM_PRIVILEGE,(Boolean)authMap.get(UserManagement.REQUEST_ITEM_PRIVILEGE));
        session.setAttribute(UserManagement.BARCODE_RESTRICTED_PRIVILEGE,(Boolean)authMap.get(UserManagement.BARCODE_RESTRICTED_PRIVILEGE));
        session.setAttribute(UserManagement.DEACCESSION_PRIVILEGE,(Boolean)authMap.get(UserManagement.DEACCESSION_PRIVILEGE));
        Object isSuperAdmin = session.getAttribute(UserManagement.SUPER_ADMIN_USER);
        if((boolean)isSuperAdmin == true){
            session.setAttribute(UserManagement.ROLE_FOR_SUPER_ADMIN,true);
        }
        else {
            session.setAttribute(UserManagement.ROLE_FOR_SUPER_ADMIN,false);
        }
    }

    private boolean isAnonymousUser(Authentication auth) {
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

package org.recap.controller;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.recap.RecapConstants;
import org.recap.model.usermanagement.LoginValidator;
import org.recap.model.usermanagement.UserForm;
import org.recap.util.UserAuthUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.token.TokenStore;
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
//            return "redirect:/search";
//        }
        logger.info("Login Screen called");
        return RecapConstants.VIEW_LOGIN;
    }

    @RequestMapping(value = "/login-scsb", method = RequestMethod.GET)
    public String login(@Valid @ModelAttribute UserForm userForm, HttpServletRequest request, Model model, BindingResult error) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            String institutionFromRequest = userForm.getInstitution();
            if (StringUtils.equals(institutionFromRequest, RecapConstants.NYPL)) {
                OAuth2Authentication oauth = (OAuth2Authentication) auth;
                String tokenString = ((OAuth2AuthenticationDetails) oauth.getDetails()).getTokenValue();
                OAuth2AccessToken accessToken = tokenStore.readAccessToken(tokenString);

                Map<String, Object> additionalInformation = accessToken.getAdditionalInformation();
                if (null != additionalInformation) {
                    username = (String) additionalInformation.get("sub");
                }
            }
            logger.info("passing in /login");
            userForm.setUsername(username);
            userForm.setPassword("");
            UsernamePasswordToken token = new UsernamePasswordToken(userForm.getUsername() + RecapConstants.TOKEN_SPLITER + userForm.getInstitution(), userForm.getPassword(), true);
            Map<String, Object> resultMap = userAuthUtil.doAuthentication(token);
            if (!(Boolean) resultMap.get(RecapConstants.IS_USER_AUTHENTICATED)) {
                String errorMessage = (String) resultMap.get(RecapConstants.USER_AUTH_ERRORMSG);
                userForm.setErrorMessage(errorMessage);
                error.rejectValue(RecapConstants.ERROR_MESSAGE, RecapConstants.ERROR_CODE_ERROR_MESSAGE, errorMessage);
                logger.error(RecapConstants.LOG_ERROR + errorMessage);
                return RecapConstants.VIEW_LOGIN;
            }
            HttpSession session = request.getSession(true);
            session.setAttribute(RecapConstants.TOKEN, token);
            session.setAttribute(RecapConstants.USER_AUTH, resultMap);
            setValuesInSession(session, resultMap);
        } catch (Exception exception) {
            logger.error(RecapConstants.LOG_ERROR, exception);
            logger.error("Exception occurred in authentication : " + exception.getLocalizedMessage());
            error.rejectValue(RecapConstants.ERROR_MESSAGE, RecapConstants.ERROR_CODE_ERROR_MESSAGE, exception.getMessage());
            return RecapConstants.VIEW_LOGIN;
        }
        return "redirect:/search";
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
            UsernamePasswordToken token=new UsernamePasswordToken(userForm.getUsername()+ RecapConstants.TOKEN_SPLITER +userForm.getInstitution(),userForm.getPassword(),true);
            resultmap=userAuthUtil.doAuthentication(token);

            if(!(Boolean) resultmap.get("isAuthenticated"))
            {
                throw new Exception("Subject Authtentication Failed");
            }
            HttpSession session=request.getSession();
            session.setAttribute(RecapConstants.USER_TOKEN,token);
            session.setAttribute(RecapConstants.USER_AUTH,resultmap);
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
            if(resultmap!=null) {
                logger.debug("Exception occured in authentication Process : {} ", resultmap.get(RecapConstants.USER_AUTH_ERRORMSG));
                logger.error("{} : {}", e.getLocalizedMessage(), resultmap.get(RecapConstants.USER_AUTH_ERRORMSG));
            }
            return loginScreen;
        }
        return "redirect:/search";

    }

    @RequestMapping("/logout")
    public String logoutUser(HttpServletRequest request){
        logger.info("Subject Logged out");
        HttpSession session=null;
        try{
            session=request.getSession();
            userAuthUtil.authorizedUser(RecapConstants.SCSB_SHIRO_LOGOUT_URL,(UsernamePasswordToken)session.getAttribute(RecapConstants.USER_TOKEN));
        }finally{
            if(session!=null) {
                session.invalidate();
            }
            return "redirect:/";
        }
    }

    private void setValuesInSession(HttpSession session,Map<String,Object> authMap)
    {
        session.setAttribute("userName",(String)authMap.get("userName"));
        session.setAttribute(RecapConstants.USER_ID,(Integer)authMap.get(RecapConstants.USER_ID));
        session.setAttribute(RecapConstants.USER_INSTITUTION,(Integer)authMap.get(RecapConstants.USER_INSTITUTION));
        session.setAttribute(RecapConstants.SUPER_ADMIN_USER,(Boolean)authMap.get(RecapConstants.SUPER_ADMIN_USER));
        session.setAttribute(RecapConstants.RECAP_USER,(Boolean)authMap.get(RecapConstants.RECAP_USER));
        session.setAttribute(RecapConstants.REQUEST_PRIVILEGE,(Boolean)authMap.get(RecapConstants.REQUEST_PRIVILEGE));
        session.setAttribute(RecapConstants.COLLECTION_PRIVILEGE,(Boolean)authMap.get(RecapConstants.COLLECTION_PRIVILEGE));
        session.setAttribute(RecapConstants.REPORTS_PRIVILEGE,(Boolean)authMap.get(RecapConstants.REPORTS_PRIVILEGE));
        session.setAttribute(RecapConstants.SEARCH_PRIVILEGE,(Boolean)authMap.get(RecapConstants.SEARCH_PRIVILEGE));
        session.setAttribute(RecapConstants.USER_ROLE_PRIVILEGE,(Boolean)authMap.get(RecapConstants.USER_ROLE_PRIVILEGE));
        session.setAttribute(RecapConstants.REQUEST_ALL_PRIVILEGE,(Boolean)authMap.get(RecapConstants.REQUEST_ALL_PRIVILEGE));
        session.setAttribute(RecapConstants.REQUEST_ITEM_PRIVILEGE,(Boolean)authMap.get(RecapConstants.REQUEST_ITEM_PRIVILEGE));
        session.setAttribute(RecapConstants.BARCODE_RESTRICTED_PRIVILEGE,(Boolean)authMap.get(RecapConstants.BARCODE_RESTRICTED_PRIVILEGE));
        session.setAttribute(RecapConstants.DEACCESSION_PRIVILEGE,(Boolean)authMap.get(RecapConstants.DEACCESSION_PRIVILEGE));
        Object isSuperAdmin = session.getAttribute(RecapConstants.SUPER_ADMIN_USER);
        if((boolean)isSuperAdmin){
            session.setAttribute(RecapConstants.ROLE_FOR_SUPER_ADMIN,true);
        }
        else {
            session.setAttribute(RecapConstants.ROLE_FOR_SUPER_ADMIN,false);
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

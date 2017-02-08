package org.recap.security;

import org.apache.shiro.authc.UsernamePasswordToken;
import org.slf4j.Logger;

import javax.servlet.http.HttpSession;

/**
 * Created by dharmendrag on 15/12/16.
 */
public enum UserManagement {

    TOKEN_SPLITER(":"),
    ReCAP("ReCAP"),
    SUPER_ADMIN(1);

    public static final String USER_AUTH="user_auth";

    public static final String ROLE_ID="roleId";

    public static final String USER_AUTHENTICATION="isAuthenticated";

    public static final String USER_ID="userId";

    public static final String USER_TOKEN="token";

    public static final String USER_INSTITUTION="userInstitution";

    public static final String REQUEST_PRIVILEGE="isRequestAllowed";

    public static final String REQUEST_ALL_PRIVILEGE="isRequestAllAllowed";

    public static final String REQUEST_ITEM_PRIVILEGE="isRequestItemAllowed";

    public static final String COLLECTION_PRIVILEGE="isCollectionAllowed";

    public static final String REPORTS_PRIVILEGE="isReportAllowed";

    public static final String SEARCH_PRIVILEGE = "isSearchAllowed";

    public static final String USER_ROLE_PRIVILEGE="isUserRoleAllowed";

    public static final String BARCODE_RESTRICTED_PRIVILEGE="isBarcodeRestricted";

    public static final String DEACCESSION_PRIVILEGE="isDeaccessionAllowed";

    public static final String SUPER_ADMIN_USER="isSuperAdmin";

    public static final String ReCAP_USER="isRecapUser";


    public static final String USER_AUTH_ERRORMSG="authErrorMsg";

    public static final String USER_NAME="userName";


    private String value;

    private int integerValues;

    public int getIntegerValues() {
        return integerValues;
    }

    UserManagement(String value)
    {
        this.value=value;
    }

    UserManagement(int value)
    {
        this.integerValues=value;
    }

    public String getValue()
    {
        return this.value;
    }


    public static String unAuthorizedUser(HttpSession session, String moduleName, Logger logger)
    {
        final String loginScreen="redirect:/";
        logger.debug(moduleName+" authorization Rejected for :"+(UsernamePasswordToken)session.getAttribute(USER_TOKEN));
        if(session!=null){
            session.invalidate();
        }
        return loginScreen;
    }






}

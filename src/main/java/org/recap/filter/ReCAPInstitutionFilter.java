package org.recap.filter;

import org.apache.commons.lang3.StringUtils;
import org.recap.RecapConstants;
import org.recap.security.UserInstitutionCache;
import org.recap.spring.PropertyValueProvider;
import org.recap.util.HelperUtil;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Created by sheiks on 23/01/17.
 */
public class ReCAPInstitutionFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String institutionCode = HelperUtil.getInstitutionFromRequest(request);

        UserInstitutionCache userInstitutionCache = HelperUtil.getBean(UserInstitutionCache.class);

        String requestedSessionId = request.getRequestedSessionId();
        HttpSession session = request.getSession(false);
        String sessionId = session.getId();
        String requestURI = request.getRequestURI();
        if(!StringUtils.equals(requestURI, RecapConstants.VIEW_HOME) && !StringUtils.equals(requestedSessionId, sessionId)) {
            institutionCode = userInstitutionCache.getInstitutionForRequestSessionId(requestedSessionId);
            userInstitutionCache.removeSessionId(requestedSessionId);
            String logoutUrl = HelperUtil.getLogoutUrl(institutionCode);
            try {
                response.sendRedirect(logoutUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            if(StringUtils.isNotBlank(institutionCode)) {
                userInstitutionCache.addRequestSessionId(requestedSessionId, institutionCode);
            }

            String institutionForCsrf = userInstitutionCache.getInstitutionForRequestSessionId(requestedSessionId);
            if(StringUtils.isNotBlank(institutionForCsrf)) {
                request.setAttribute(RecapConstants.RECAP_INSTITUTION_CODE, institutionForCsrf);
            }
            filterChain.doFilter(request, response);
        }
    }

}

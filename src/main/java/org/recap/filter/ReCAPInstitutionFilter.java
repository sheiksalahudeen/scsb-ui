package org.recap.filter;

import org.apache.commons.lang3.StringUtils;
import org.recap.RecapConstants;
import org.recap.security.UserInstitutionCache;
import org.recap.util.HelperUtil;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by sheiks on 23/01/17.
 */
public class ReCAPInstitutionFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Adding institution Id to the request attribute
        String institutionId = HelperUtil.getInstitutionFromRequest(request);

        if(StringUtils.isNotBlank(institutionId)) {
            request.setAttribute(RecapConstants.RECAP_INSTITUTION_ID, institutionId);
            Cookie cookie = new Cookie(RecapConstants.RECAP_INSTITUTION_ID, institutionId);
            cookie.setMaxAge(-1);
            cookie.setHttpOnly(false);
            cookie.setPath("/");
            response.addCookie(cookie);
        }
        filterChain.doFilter(request, response);
    }
}

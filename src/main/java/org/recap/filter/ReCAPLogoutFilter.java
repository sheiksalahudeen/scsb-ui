package org.recap.filter;

import org.recap.security.UserManagement;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Created by sheiks on 03/03/17.
 */
public class ReCAPLogoutFilter extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpSession session = ((HttpServletRequest)request).getSession();
        Object attribute = session.getAttribute(UserManagement.USER_TOKEN);
        request.setAttribute(UserManagement.USER_TOKEN,attribute);
        chain.doFilter(request,response);
    }
}

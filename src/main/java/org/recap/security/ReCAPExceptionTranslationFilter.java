package org.recap.security;

import org.apache.commons.lang3.StringUtils;
import org.recap.RecapConstants;
import org.recap.spring.PropertyValueProvider;
import org.recap.util.HelperUtil;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.util.ThrowableAnalyzer;
import org.springframework.security.web.util.ThrowableCauseExtractor;
import org.springframework.util.Assert;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by sheiks on 25/01/17.
 */
public class ReCAPExceptionTranslationFilter extends GenericFilterBean {

    // ~ Instance fields
    // ================================================================================================

    private AccessDeniedHandler accessDeniedHandler = new AccessDeniedHandlerImpl();
    private AuthenticationEntryPoint authenticationEntryPoint;
    private AuthenticationTrustResolver authenticationTrustResolver = new AuthenticationTrustResolverImpl();
    private ThrowableAnalyzer throwableAnalyzer = new DefaultThrowableAnalyzer();

    private RequestCache requestCache = new HttpSessionRequestCache();

    public ReCAPExceptionTranslationFilter(AuthenticationEntryPoint authenticationEntryPoint) {
        this(authenticationEntryPoint, new HttpSessionRequestCache());
    }

    public ReCAPExceptionTranslationFilter(AuthenticationEntryPoint authenticationEntryPoint,
                                      RequestCache requestCache) {
        Assert.notNull(authenticationEntryPoint,
                "authenticationEntryPoint cannot be null");
        Assert.notNull(requestCache, "requestCache cannot be null");
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.requestCache = requestCache;
    }

    // ~ Methods
    // ========================================================================================================

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(authenticationEntryPoint,
                "authenticationEntryPoint must be specified");
    }

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        try {
            chain.doFilter(request, response);

            logger.debug("Chain processed normally");
        }
        catch (IOException ex) {
            throw ex;
        }
        catch (Exception ex) {
            // Try to extract a SpringSecurityException from the stacktrace
            Throwable[] causeChain = throwableAnalyzer.determineCauseChain(ex);
            RuntimeException ase = (AuthenticationException) throwableAnalyzer
                    .getFirstThrowableOfType(AuthenticationException.class, causeChain);

            if (ase == null) {
                ase = (AccessDeniedException) throwableAnalyzer.getFirstThrowableOfType(
                        AccessDeniedException.class, causeChain);
            }

            if (ase != null) {
                handleSpringSecurityException(request, response, chain, ase);
            }
            else {
                // Rethrow ServletExceptions and RuntimeExceptions as-is
                if (ex instanceof ServletException) {
                    throw (ServletException) ex;
                }
                else if (ex instanceof RuntimeException) {
                    throw (RuntimeException) ex;
                }

                // Wrap other Exceptions. This shouldn't actually happen
                // as we've already covered all the possibilities for doFilter
                throw new RuntimeException(ex);
            }
        }
    }

    public AuthenticationEntryPoint getAuthenticationEntryPoint() {
        return authenticationEntryPoint;
    }

    protected AuthenticationTrustResolver getAuthenticationTrustResolver() {
        return authenticationTrustResolver;
    }

    private void handleSpringSecurityException(HttpServletRequest request,
                                               HttpServletResponse response, FilterChain chain, RuntimeException exception)
            throws IOException, ServletException {
        if (exception instanceof AuthenticationException) {
            logger.debug(
                    "Authentication exception occurred; redirecting to authentication entry point",
                    exception);

            sendStartAuthentication(request, response, chain,
                    (AuthenticationException) exception);
        }
        else if (exception instanceof AccessDeniedException) {
            if (authenticationTrustResolver.isAnonymous(SecurityContextHolder
                    .getContext().getAuthentication())) {
                logger.debug(
                        "Access is denied (user is anonymous); redirecting to authentication entry point",
                        exception);

                sendStartAuthentication(
                        request,
                        response,
                        chain,
                        new InsufficientAuthenticationException(
                                "Full authentication is required to access this resource"));
            }
            else {
                logger.debug(
                        "Access is denied (user is not anonymous); delegating to AccessDeniedHandler",
                        exception);

                accessDeniedHandler.handle(request, response,
                        (AccessDeniedException) exception);
            }
        }
    }

    protected void sendStartAuthentication(HttpServletRequest request,
                                           HttpServletResponse response, FilterChain chain,
                                           AuthenticationException reason) throws ServletException, IOException {
        // SEC-112: Clear the SecurityContextHolder's Authentication, as the
        // existing Authentication is no longer considered valid
        SecurityContextHolder.getContext().setAuthentication(null);
        requestCache.saveRequest(request, response);
        logger.debug("Calling Authentication entry point.");
        String institution = HelperUtil.getAttributeValueFromRequest(request, RecapConstants.RECAP_INSTITUTION_ID);
        CasAuthenticationEntryPoint originalEntryPoint = (CasAuthenticationEntryPoint) this.authenticationEntryPoint;
        if(StringUtils.isBlank(institution)) {
            originalEntryPoint.commence(request,response,reason);
        } else {
            String urlProperty = RecapConstants.CAS + institution + RecapConstants.SERVICE_LOGIN;
            String url = HelperUtil.getBean(PropertyValueProvider.class).getProperty(urlProperty);

            //Calling cas entry point based on institution type.
            CasAuthenticationEntryPoint casAuthenticationEntryPoint = new CasAuthenticationEntryPoint();
            casAuthenticationEntryPoint.setLoginUrl(url);
            casAuthenticationEntryPoint.setServiceProperties(originalEntryPoint.getServiceProperties());
            casAuthenticationEntryPoint.commence(request, response, reason);
        }

    }

    public void setAccessDeniedHandler(AccessDeniedHandler accessDeniedHandler) {
        Assert.notNull(accessDeniedHandler, "AccessDeniedHandler required");
        this.accessDeniedHandler = accessDeniedHandler;
    }

    public void setAuthenticationTrustResolver(
            AuthenticationTrustResolver authenticationTrustResolver) {
        Assert.notNull(authenticationTrustResolver,
                "authenticationTrustResolver must not be null");
        this.authenticationTrustResolver = authenticationTrustResolver;
    }

    public void setThrowableAnalyzer(ThrowableAnalyzer throwableAnalyzer) {
        Assert.notNull(throwableAnalyzer, "throwableAnalyzer must not be null");
        this.throwableAnalyzer = throwableAnalyzer;
    }

    /**
     * Default implementation of <code>ThrowableAnalyzer</code> which is capable of also
     * unwrapping <code>ServletException</code>s.
     */
    private static final class DefaultThrowableAnalyzer extends ThrowableAnalyzer {
        /**
         * @see ThrowableAnalyzer#initExtractorMap()
         */
        protected void initExtractorMap() {
            super.initExtractorMap();

            registerExtractor(ServletException.class, new ThrowableCauseExtractor() {
                public Throwable extractCause(Throwable throwable) {
                    ThrowableAnalyzer.verifyThrowableHierarchy(throwable,
                            ServletException.class);
                    return ((ServletException) throwable).getRootCause();
                }
            });
        }

    }

}

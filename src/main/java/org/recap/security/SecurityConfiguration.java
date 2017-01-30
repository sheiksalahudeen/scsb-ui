package org.recap.security;

import org.recap.filter.CsrfCookieGeneratorFilter;
import org.recap.filter.ReCAPInstitutionFilter;
import org.recap.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.inject.Inject;

/**
 * Created by sheiks on 17/01/17.
 */
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

   private static final String CAS_URL_LOGIN = "default.cas.service.login";
    private static final String CAS_URL_LOGOUT = "default.cas.service.logout";
    private static final String CAS_URL_PREFIX = "default.cas.url.prefix";
    private static final String CAS_SERVICE_URL = "app.service.security";
    private static final String APP_SERVICE_HOME = "app.service.home";
    private static final String APP_ADMIN_USER_NAME = "app.admin.userName";
    private static final String APP_SERVICE_LOGOUT = "app.service.logout.scsb";

    @Inject
    private Environment env;

    @Bean
    public ServiceProperties serviceProperties() {
        ServiceProperties serviceProperties = new ServiceProperties();
        serviceProperties.setService("http://localhost:9091/j_spring_cas_security_check");
        serviceProperties.setSendRenew(false);
        return serviceProperties;
    }

    @Bean
    public CasAuthenticationProvider casAuthenticationProvider() {
        CasAuthenticationProvider casAuthenticationProvider = new CasAuthenticationProvider();
        casAuthenticationProvider.setAuthenticationUserDetailsService(authenticationUserDetailsService());
        casAuthenticationProvider.setServiceProperties(serviceProperties());
        casAuthenticationProvider.setTicketValidator(cas20ServiceTicketValidator());
        casAuthenticationProvider.setKey("an_id_for_this_auth_provider_only");
        return casAuthenticationProvider;
    }

    @Bean
    public AuthenticationUserDetailsService authenticationUserDetailsService() {
        return new CustomUserDetailsService();
    }

    @Bean
    public ReCAPCas20ServiceTicketValidator cas20ServiceTicketValidator() {
        return new ReCAPCas20ServiceTicketValidator("http://localhost:8080/cas");
    }

    @Bean
    public CasAuthenticationFilter casAuthenticationFilter() throws Exception {
        CasAuthenticationFilter casAuthenticationFilter = new CasAuthenticationFilter();
        casAuthenticationFilter.setFilterProcessesUrl("/j_spring_cas_security_check");
        casAuthenticationFilter.setAuthenticationManager(authenticationManager());
        return casAuthenticationFilter;
    }

    @Bean
    public CasAuthenticationEntryPoint casAuthenticationEntryPoint() {
        CasAuthenticationEntryPoint casAuthenticationEntryPoint = new CasAuthenticationEntryPoint();
        casAuthenticationEntryPoint.setLoginUrl("http://localhost:8080/cas/login");
        casAuthenticationEntryPoint.setServiceProperties(serviceProperties());
        return casAuthenticationEntryPoint;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.addFilterAfter(new CsrfCookieGeneratorFilter(), CsrfFilter.class)
                .addFilterAfter(new ReCAPInstitutionFilter(), CsrfCookieGeneratorFilter.class)
                .addFilterAfter(new ReCAPExceptionTranslationFilter(casAuthenticationEntryPoint()), ExceptionTranslationFilter.class)
                .exceptionHandling()
                .authenticationEntryPoint(casAuthenticationEntryPoint()).and().addFilter(casAuthenticationFilter())
                .addFilterBefore(requestCasGlobalLogoutFilter(), LogoutFilter.class);

        http.authorizeRequests().antMatchers("/").permitAll()
                .antMatchers("*").authenticated().anyRequest().authenticated();

        http.logout().logoutUrl("/logout").logoutSuccessUrl("/").invalidateHttpSession(true)
                .deleteCookies("JSESSIONID");

    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .authenticationProvider(casAuthenticationProvider());
    }


    @Bean
    public LogoutFilter requestCasGlobalLogoutFilter() {
        String logoutSuccessUrl = env.getRequiredProperty(CAS_URL_LOGOUT) + "?service="
                + env.getRequiredProperty(APP_SERVICE_LOGOUT);

        ReCAPSimpleUrlLogoutSuccessHandler reCAPSimpleUrlLogoutSuccessHandler = new ReCAPSimpleUrlLogoutSuccessHandler();
        reCAPSimpleUrlLogoutSuccessHandler.setDefaultTargetUrl(logoutSuccessUrl);

        LogoutFilter logoutFilter = new LogoutFilter(reCAPSimpleUrlLogoutSuccessHandler, new SecurityContextLogoutHandler());
        logoutFilter.setLogoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"));


        return logoutFilter;
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/fonts/**").antMatchers("/images/**").antMatchers("/js/**")
                .antMatchers("/css/**").antMatchers("/lib/**");
    }
}

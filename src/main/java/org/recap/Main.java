package org.recap;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.recap.security.SessionFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;

import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
public class Main {

	@Value("${server.protocol}")
	String serverProtocol;

	@Value("${tomcat.maxParameterCount}")
	Integer tomcatMaxParameterCount;

	@Value("${server.secure}")
	boolean tomcatSecure;

	@Bean
	public EmbeddedServletContainerFactory servletContainerFactory() {
		TomcatEmbeddedServletContainerFactory factory = new TomcatEmbeddedServletContainerFactory();
		factory.addConnectorCustomizers(new TomcatConnectorCustomizer() {
			@Override
			public void customize(Connector connector) {
				connector.setMaxParameterCount(tomcatMaxParameterCount);
				connector.setSecure(tomcatSecure);
			}
		});
		return factory;
	}
	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);
	}


	@Bean
	public FilterRegistrationBean getFileterRegisteredBean(){
		FilterRegistrationBean filterRegistrationBean= new FilterRegistrationBean();
		Set<String> urlPatterns=new HashSet<>();
		urlPatterns.add("/*");
		filterRegistrationBean.setUrlPatterns(urlPatterns);
		filterRegistrationBean.setFilter(new SessionFilter());
		return filterRegistrationBean;
	}

	@Bean
	public EmbeddedServletContainerCustomizer containerCustomizer() {
		return new EmbeddedServletContainerCustomizer() {
			@Override
			public void customize(ConfigurableEmbeddedServletContainer container) {
				if (container.getClass().isAssignableFrom(TomcatEmbeddedServletContainerFactory.class)) {
					TomcatEmbeddedServletContainerFactory tomcatContainer = (TomcatEmbeddedServletContainerFactory) container;
					tomcatContainer.addContextCustomizers(new ContextSecurityCustomizer());
				}
			}
		};
	}

	private static class ContextSecurityCustomizer implements TomcatContextCustomizer {
		@Override
		public void customize(Context context) {
			SecurityConstraint constraint = new SecurityConstraint();
			SecurityCollection securityCollection = new SecurityCollection();
			securityCollection.setName("restricted_methods");
			securityCollection.addPattern("/*");
			securityCollection.addMethod(HttpMethod.OPTIONS.toString());
			securityCollection.addMethod(HttpMethod.HEAD.toString());
			securityCollection.addMethod(HttpMethod.PUT.toString());
			securityCollection.addMethod(HttpMethod.PATCH.toString());
			securityCollection.addMethod(HttpMethod.DELETE.toString());
			securityCollection.addMethod(HttpMethod.TRACE.toString());
			constraint.addCollection(securityCollection);
			constraint.setAuthConstraint(true);
			context.addConstraint(constraint);
		}
	}

}

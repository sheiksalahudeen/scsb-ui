package org.recap;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;

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
}

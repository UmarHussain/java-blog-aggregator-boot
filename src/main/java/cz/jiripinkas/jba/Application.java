package cz.jiripinkas.jba;

import java.util.Arrays;
import java.util.EnumSet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.SessionTrackingMode;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.dozer.DozerBeanMapper;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.ErrorPage;
import org.springframework.boot.context.embedded.ServletContextInitializer;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.client.RestTemplate;

@ImportResource("classpath:security.xml")
@SpringBootApplication
@EnableCaching
@EnableScheduling
public class Application {

	public static void main(String[] args) {
		new SpringApplicationBuilder(Application.class).headless(false).run(args);
	}

	@Bean
	public DozerBeanMapper dozerBeanMapper() {
		return new DozerBeanMapper();
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	public CacheManager cacheManager() {
		SimpleCacheManager cacheManager = new SimpleCacheManager();
		cacheManager.setCaches(Arrays.asList(new ConcurrentMapCache("icons"), new ConcurrentMapCache("configuration"),
				new ConcurrentMapCache("categories"), new ConcurrentMapCache("blogCount"),
				new ConcurrentMapCache("itemCount"), new ConcurrentMapCache("userCount"),
				new ConcurrentMapCache("blogCountUnapproved")));
		return cacheManager;
	}

	@Bean(destroyMethod = "close")
	public CloseableHttpClient httpClient() {
		return HttpClients.createDefault();
	}

	@Bean
	public ThreadPoolTaskScheduler scheduler() {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setThreadPriority(Thread.MIN_PRIORITY);
		return scheduler;
	}

	// http://www.sporcic.org/2014/05/custom-error-pages-with-spring-boot/
	@Bean
	public EmbeddedServletContainerCustomizer containerCustomizer() {

		return new EmbeddedServletContainerCustomizer() {
			@Override
			public void customize(ConfigurableEmbeddedServletContainer container) {
				ErrorPage error404Page = new ErrorPage(HttpStatus.NOT_FOUND, "/404");
				container.addErrorPages(error404Page);
				container.addInitializers(new ServletContextInitializer() {

					@Override
					public void onStartup(ServletContext servletContext) throws ServletException {
						servletContext.setSessionTrackingModes(EnumSet.of(SessionTrackingMode.COOKIE));
					}
				});
			}
		};
	}

}

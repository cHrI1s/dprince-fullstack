package com.dprince;

import com.dprince.configuration.files.config.LocalFileConfig;
import com.dprince.configuration.sms.SmsConfiguration;
import com.dprince.configuration.whatsapp.WhatsappConfiguration;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.omg.CORBA.Object;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.ZoneId;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@EnableScheduling
@EnableTransactionManagement
@EnableConfigurationProperties({ LocalFileConfig.class, SmsConfiguration.class, WhatsappConfiguration.class })
@EnableJpaRepositories(basePackages = { "com.dprince.entities.repositories" })
@SpringBootApplication(exclude = { UserDetailsServiceAutoConfiguration.class, SecurityAutoConfiguration.class, })
public class AppApplication {
	private static final Logger logger = LoggerFactory.getLogger(AppApplication.class);

	public static void main(String[] args) {
		try {
			TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("Asia/Calcutta")));
			SpringApplication.run(AppApplication.class, args);
			logger.info("Application has started successfully!");
		} catch (Exception ignored) {
		}
	}

	@Bean
	public JavaMailSender getJavaMailSender() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost("mail.dnote.ai");
		mailSender.setPort(587);

		mailSender.setUsername("no-reply@dnote.ai");
		mailSender.setPassword("Dprince#Broadcast@2019#600048");

		Properties props = mailSender.getJavaMailProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.debug", "true");
		return mailSender;
	}

	@Bean
	public ObjectMapper configureMapper() {
		SimpleModule module = new SimpleModule();

		// return mapper;
		ObjectMapper theMapper = JsonMapper.builder().enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
				.enable(MapperFeature.ALLOW_COERCION_OF_SCALARS, MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
				.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
				.configure(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS, true)
				.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true).addModule(module).build();
		theMapper.setTimeZone(TimeZone.getDefault());
		return theMapper;
	}

	@Bean
	public ScheduledTaskRegistrar scheduledTaskRegistrar(TaskScheduler taskScheduler) {
		ScheduledTaskRegistrar taskRegistrar = new ScheduledTaskRegistrar();
		taskRegistrar.setTaskScheduler(taskScheduler);
		return taskRegistrar;
	}

	@Bean
	public TaskScheduler taskScheduler() {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(30);
		scheduler.setThreadNamePrefix("MyScheduler-");
		scheduler.initialize();
		return scheduler;
	}

	@Bean
	public ExecutorService executorService() {
		int corePoolSize = Runtime.getRuntime().availableProcessors();
		return Executors.newFixedThreadPool(corePoolSize); // Adjust the pool size as needed
	}
}

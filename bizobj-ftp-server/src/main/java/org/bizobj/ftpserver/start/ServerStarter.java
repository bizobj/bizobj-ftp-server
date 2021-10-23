package org.bizobj.ftpserver.start;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.ssl.SslConfigurationFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.bizobj.ftpserver.cfg.ConfigProperties;
import org.bizobj.ftpserver.ftp.auth.core.CustomAuthPropertiesUserManagerFactory;
import org.bizobj.ftpserver.ftp.fs.mapping.MappingNativeFileSystemFactory;
import org.bizobj.ftpserver.ftp.staticservice.FtpUtilService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import com.alibaba.fastjson.JSON;

@Component
public class ServerStarter {
	private static final Logger log = LoggerFactory.getLogger(ServerStarter.class);
	
	private ConfigProperties config;
	private ResourceLoader resourceLoader;
	public ServerStarter(ConfigProperties config, ResourceLoader resourceLoader) {
		this.config = config;
		this.resourceLoader = resourceLoader;
	}
	
	@EventListener(ApplicationReadyEvent.class)
	public boolean start() {
		try {
			ListenerFactory factory = new ListenerFactory();
			// set the port and other attributes of the listener
			factory.setPort(config.getPort());
			log.info("FTP Port = {}", factory.getPort());
			factory.setIdleTimeout(config.getMaxIdleSeconds());
			log.info("FTP IdleTimeout = {} seconds", factory.getIdleTimeout());
			
			// define SSL configuration
			SslConfigurationFactory ssl = new SslConfigurationFactory();
			ssl.setKeystoreFile(resource2File(config.getKeystore().getResourcePath(), "keystore.jks"));
			ssl.setKeystorePassword(config.getKeystore().getPassword());
//			ssl.setKeyAlias("bizobj");
//			ssl.setKeyPassword("20211023");
//			ssl.setTruststoreFile(resource2File("classpath:keystore/bizobj-truststore.jks", "truststore.jks"));
//			ssl.setTruststorePassword("20211023");
			// set the SSL configuration for the listener
			factory.setSslConfiguration(ssl.createSslConfiguration());
			//factory.setImplicitSsl(true);		//FIXME: Implicit SSL not support - setImplicitSsl(true) should hold client connection
			
			FtpServerFactory serverFactory = new FtpServerFactory();

			// replace the default listener
			serverFactory.addListener("default", factory.createListener());
			
			// Check and initialize user properties file
			File userRepoFile = FtpUtilService.getUserProperties();
			log.info("User repo properties file is {} .", userRepoFile.getCanonicalPath());
			//Customize user mananger
			PropertiesUserManagerFactory userManagerFactory = new CustomAuthPropertiesUserManagerFactory();
			userManagerFactory.setFile(userRepoFile);
			String adminName = config.getAdmin().getName();
			String adminPassword = config.getAdmin().getPassword();
			userManagerFactory.setAdminName(adminName);
			UserManager userManager = userManagerFactory.createUserManager();
			serverFactory.setUserManager(userManager);
			
			// Check and initialize admin account
			log.info("Begin to initialize it and create defaule admin user ...");
			BaseUser admin = FtpUtilService.buildWritePermissionUser(adminName,adminPassword);
			userManager.save(admin);
			log.info("Admin user created, name="+admin.getName()+", password="+admin.getPassword()+".");
			// Initialize mapping file system for admin
			MappingNativeFileSystemFactory mfsf = FtpUtilService.buildMappingFileSystemFactory(admin);
			serverFactory.setFileSystem(mfsf);
			log.info("Create filesystem with mapping: \n" + JSON.toJSONString(mfsf.getMappings(), true));

			// start the server
			FtpServer server = serverFactory.createServer(); 
			server.start();

			return true;
		}catch(Exception ex) {
			return ExceptionUtils.rethrow(ex);
		}
	}
	
	private File resource2File(String resourcePath, String fileName) throws IOException {
		if (resourcePath.startsWith("file:")) {
			return ResourceUtils.getFile(resourcePath);
		}
		Resource resource = resourceLoader.getResource(resourcePath);
		return FtpUtilService.getTempResourceFile(resource, fileName);
	}
}

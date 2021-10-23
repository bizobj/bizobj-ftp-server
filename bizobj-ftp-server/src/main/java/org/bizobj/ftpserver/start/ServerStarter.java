package org.bizobj.ftpserver.start;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
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
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import com.alibaba.fastjson.JSON;

@Component
public class ServerStarter {
	private static final Logger log = LoggerFactory.getLogger(ServerStarter.class);
	
	private ConfigProperties config;
	public ServerStarter(ConfigProperties config) {
		this.config = config;
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
			ssl.setKeystoreFile(resource2File(config.getKeystore().getResourcePath()));
			ssl.setKeystorePassword(config.getKeystore().getPassword());
			// set the SSL configuration for the listener
			factory.setSslConfiguration(ssl.createSslConfiguration());
			//factory.setImplicitSsl(true);		//FIXME: setImplicitSsl(true) should cause client connection problem
			
			// Check and initialize user properties file
			File userRepoFile = new File(new File(new File(config.getFtpHome()), "conf"), "user.properties");
			log.info("User repo properties file is {} .", userRepoFile.getCanonicalPath());
			if (! userRepoFile.exists()){
				log.warn("User repo properties file is not exist, begin to create it ...");
				userRepoFile.getParentFile().mkdirs();
				FileUtils.write(userRepoFile, "", StandardCharsets.UTF_8);
			}
			
			FtpServerFactory serverFactory = new FtpServerFactory();

			// replace the default listener
			serverFactory.addListener("default", factory.createListener());
			PropertiesUserManagerFactory userManagerFactory = new CustomAuthPropertiesUserManagerFactory();
			userManagerFactory.setFile(userRepoFile);
			String adminName = config.getAdmin().getName();
			String adminPassword = config.getAdmin().getPassword();
			userManagerFactory.setAdminName(adminName);
			UserManager userManager = userManagerFactory.createUserManager();
			serverFactory.setUserManager(userManager);
			// Check and initialize user properties file
			log.info("Begin to initialize it and create defaule admin user ...");
			BaseUser admin = FtpUtilService.buildWritePermissionUser(adminName,adminPassword);
			userManager.save(admin);
			log.info("Admin user created, name="+admin.getName()+", password="+admin.getPassword()+".");
			// Initialize mapping file system
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
	
	private File resource2File(String resourcePath) throws FileNotFoundException {
		return ResourceUtils.getFile(resourcePath);
	}
}

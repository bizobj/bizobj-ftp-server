package org.bizobj.ftp.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.ssl.SslConfigurationFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.log4j.Logger;
import org.bizobj.ftp.auth.core.CustomAuthPropertiesUserManagerFactory;

public class Server {
	private static final Logger log = Logger.getLogger(Server.class);
	
	private FtpServer server;
	
	public void start(){
		FtpServer server;
		try {
			FtpServerFactory serverFactory = new FtpServerFactory();
			ListenerFactory factory = new ListenerFactory();
			// set the port and other attributes of the listener
			factory.setPort(CONFIG.getPort());
			log.info("FTP Port = " + factory.getPort());
			factory.setIdleTimeout(CONFIG.getMaxIdleTime());
			log.info("FTP IdleTimeout = " + factory.getIdleTimeout());
			// define SSL configuration
			SslConfigurationFactory ssl = new SslConfigurationFactory();
			ssl.setKeystoreFile(CONFIG.getKeystoreFile());
			ssl.setKeystorePassword(CONFIG.getKeystorePassword());
			// set the SSL configuration for the listener
			factory.setSslConfiguration(ssl.createSslConfiguration());
			//factory.setImplicitSsl(true);		//FIXME: setImplicitSsl(true) should cause client connection problem
			// Check and initialize user properties file
			File userRepoFile = CONFIG.getUserRepoFile();
			log.info("User repo properties file is " + userRepoFile.getCanonicalPath());
			if (! userRepoFile.exists()){
				log.warn("User repo properties file is not exist, begin to create it ...");
				userRepoFile.getParentFile().mkdirs();
				OutputStream os = new FileOutputStream(userRepoFile);
				os.write(' ');
				os.close();
			}
			// replace the default listener
			serverFactory.addListener("default", factory.createListener());
			PropertiesUserManagerFactory userManagerFactory = new CustomAuthPropertiesUserManagerFactory();
			userManagerFactory.setFile(userRepoFile);
			String adminName = CONFIG.getAdminName();
			String adminPassword = CONFIG.getAdminPassword();
			userManagerFactory.setAdminName(adminName);
			UserManager userManager = userManagerFactory.createUserManager();
			serverFactory.setUserManager(userManager);
			// Check and initialize user properties file
			log.info("Begin to initialize it and create defaule admin user ...");
			BaseUser admin = CONFIG.buildWritePermissionUser(adminName,adminPassword);
			userManager.save(admin);
			log.info("Admin user created, name="+admin.getName()+", password="+admin.getPassword()+".");
			// start the server
			server = serverFactory.createServer(); 
			server.start();
			this.server = server;
		} catch (FtpException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void stop(){
		if (null!=this.server){
			this.server.stop();
		}
	}
}

package org.bizobj.ftp.auth.core;

import java.io.File;
import java.net.URL;


import org.apache.ftpserver.ftplet.Authentication;
import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.usermanager.PasswordEncryptor;
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;
import org.apache.ftpserver.usermanager.impl.PropertiesUserManager;
import org.bizobj.ftp.auth.intf.UserPasswordAuthChecker;
import org.bizobj.ftp.server.CONFIG;

public class CustomAuthPropertiesUserManager extends PropertiesUserManager {

	private UserPasswordAuthChecker authChecker;

	public CustomAuthPropertiesUserManager(PasswordEncryptor passwordEncryptor, File userDataFile, String adminName) {
		super(passwordEncryptor, userDataFile, adminName);
	}

	public CustomAuthPropertiesUserManager(PasswordEncryptor passwordEncryptor, URL userDataPath, String adminName) {
		super(passwordEncryptor, userDataPath, adminName);
	}

	public void registerAuthChecker(UserPasswordAuthChecker checker){
		this.authChecker = checker;
	}

	@Override
	public User authenticate(Authentication authentication) throws AuthenticationFailedException {
		if (authentication instanceof UsernamePasswordAuthentication) {
            UsernamePasswordAuthentication upauth = (UsernamePasswordAuthentication) authentication;
            String username = upauth.getUsername();
			String password = upauth.getPassword();
			boolean pass = this.authChecker.check(username, password);
            if (pass){
            	User user = super.getUserByName(username);
            	if (null==user){
            		//Support "user with default setting", these users wouldn't need user properties file to manage it
            		user = CONFIG.buildWritePermissionUser(username, password);
            	}
            	return user;
            }else{
            	return super.authenticate(authentication);
            }
		}else{
			return super.authenticate(authentication);
		}
	}
	
	
}

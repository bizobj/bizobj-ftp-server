package org.bizobj.ftp.auth.core;

import java.net.URL;


import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.UserManagerFactory;
import org.bizobj.ftp.auth.intf.UserPasswordAuthChecker;
import org.bizobj.ftp.server.CONFIG;

/**
 * {@link UserManagerFactory} based on {@link PropertiesUserManagerFactory} and support customized authentication method
 * @author root
 *
 */
public class CustomAuthPropertiesUserManagerFactory extends PropertiesUserManagerFactory {
	@Override
	public UserManager createUserManager() {
		UserPasswordAuthChecker checker = CONFIG.getAuthChecker();
		if (null==checker){
			return super.createUserManager();
		}else{
			CustomAuthPropertiesUserManager mgr;
			URL userDataURL = this.getUrl();
	        if (userDataURL != null) {
	            mgr = new CustomAuthPropertiesUserManager(this.getPasswordEncryptor(), userDataURL, this.getAdminName());
	        } else {
	            mgr = new CustomAuthPropertiesUserManager(this.getPasswordEncryptor(), this.getFile(), this.getAdminName());
	        }
	        mgr.registerAuthChecker(checker);
	        return mgr;
		}
	}
	
}

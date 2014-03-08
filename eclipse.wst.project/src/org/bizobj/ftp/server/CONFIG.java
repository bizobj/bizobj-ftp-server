package org.bizobj.ftp.server;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.bizobj.ftp.auth.core.CustomAuthPropertiesUserManagerFactory;
import org.bizobj.ftp.auth.intf.UserPasswordAuthChecker;

/**
 * The configuration and other related procedure
 * @author root
 *
 */
public class CONFIG {
	/** The FTP Port number, default is 21 */
	public static String KEY_PORT = "Port";
	/** The base folder for a FTP site, default is ~/.bizobj.org/ftp */
	public static String KEY_FTP_BASE = "FtpBase";
	/** The folder for every user's FTP home directory, default is ${FtpBase}/home */
	public static String KEY_HOME_BASE = "HomeBase";
	/** The user definition properties file, default is ${FtpBase}/conf/user.properties */
	public static String KEY_USER_FILE = "UserFile";
	/** The administrator user's name, default is "admin" */
	public static String KEY_ADMIN_NAME = "AdminName";
	/** The administrator user's password, default is "password" */
	public static String KEY_ADMIN_PWD = "AdminPwd";
	
	/** The max idle time(in second) of connection, default is 300 seconds */
	public static String KEY_MAX_IDLE_TIME = "MaxIdleTime";
	
	/** The customized user authentication check program's class name, see {@link CustomAuthPropertiesUserManagerFactory} for detail */
	public static String KEY_AUTH_CHECKER = "AuthChecker";
	
	public static String getConfigVar(Class<?> clazz, String key, String defValue){
		String propKey = clazz.getName() + "_" + key;
		String envKey = propKey.replace(".", "_");
		
		String val = System.getenv(envKey);
		if (null==val){
			val = System.getProperty(propKey);
		}
		if (null==val){
			val = defValue;
		}
		return val;
	}
	private static String getVar(String key, String defValue){
		return getConfigVar(CONFIG.class, key, defValue);
	}
	
	private static String getFtpBaseDir(){
		String userHome = System.getProperty("user.home");
		String defaultFtpBase = userHome + "/.bizobj.org/ftp";
		
		String ftpBase = getVar(KEY_FTP_BASE, defaultFtpBase);
		return ftpBase;
	}
	
	public static int getPort(){
		String ps = getVar(KEY_PORT, "21");
		int p = Integer.valueOf(ps);
		return p;
	}
	
	public static File getUserRepoFile(){
		String f = getVar(KEY_USER_FILE, null);
		if (null==f){
			f = getFtpBaseDir() + "/conf/user.properties";
		}
		File uf = new File(f);
		return uf;
	}
	
	public static File getHome(String userName){
		String hBase = getVar(KEY_HOME_BASE, getFtpBaseDir()+"/home");
		String h = hBase + "/" + userName;
		File fh = new File(h);
		return fh;
	}
	
	public static int getMaxIdleTime(){
		String ts = getVar(KEY_MAX_IDLE_TIME, "300");
		int t = Integer.valueOf(ts);
		return t;
	}
	
	public static String getAdminName(){
		return getVar(KEY_ADMIN_NAME, "admin");
	}
	
	public static String getAdminPassword(){
		return getVar(KEY_ADMIN_PWD, "password");
	}
	
	public static File getKeystoreFile(){
		try {
			URL r = CONFIG.class.getResource("/keystore/ftpserver.jks");
			File f = new File(r.toURI());
			return f;
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	public static String getKeystorePassword(){
		return "password";
	}
	
	public static UserPasswordAuthChecker getAuthChecker(){
		String className = getVar(KEY_AUTH_CHECKER, null);
		if (null==className){
			return null;
		}else{
			try {
				UserPasswordAuthChecker c = (UserPasswordAuthChecker)Class.forName(className).newInstance();
				return c;
			} catch (InstantiationException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public static BaseUser buildWritePermissionUser(String userName, String password) {
		try {
			BaseUser user = new BaseUser();
			List<Authority> auth = new ArrayList<Authority>();
			auth.add(new WritePermission());
			File userHome = CONFIG.getHome(userName);
			if (! userHome.exists()){
				userHome.mkdirs();
			}
			user.setAuthorities(auth);
			user.setEnabled(true);
			user.setHomeDirectory(userHome.getCanonicalPath());
			user.setMaxIdleTime(CONFIG.getMaxIdleTime());
			user.setName(userName);
			user.setPassword(password);
			return user;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	

}

package org.bizobj.ftpserver.cfg;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="ftp.server")
public class ConfigProperties {
	/** The FTP Port number, default is 21 */
	private int port;
	/** The max idle time(in second) of connection */
	private int maxIdleSeconds;
	/** The customized user authentication check program's class name, see {@link CustomAuthPropertiesUserManagerFactory} for detail */
	private String authCheckerClass;

	/** The base folder for a FTP site */
	private String ftpBase;
	/** The folder for every user's FTP home directory */
	private String ftpHome;

	/** The administrator user's name and password */
	private AdminConfig admin;
	
	/** The keystore path and password */
	private KeystoreConfig keystore;

	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public int getMaxIdleSeconds() {
		return maxIdleSeconds;
	}
	public void setMaxIdleSeconds(int maxIdleSeconds) {
		this.maxIdleSeconds = maxIdleSeconds;
	}
	public String getAuthCheckerClass() {
		return authCheckerClass;
	}
	public void setAuthCheckerClass(String authCheckerClass) {
		this.authCheckerClass = authCheckerClass;
	}
	public String getFtpBase() {
		return ftpBase;
	}
	public void setFtpBase(String ftpBase) {
		this.ftpBase = ftpBase;
	}
	public String getFtpHome() {
		return ftpHome;
	}
	public void setFtpHome(String ftpHome) {
		this.ftpHome = ftpHome;
	}
	public AdminConfig getAdmin() {
		return admin;
	}
	public void setAdmin(AdminConfig admin) {
		this.admin = admin;
	}
	public KeystoreConfig getKeystore() {
		return keystore;
	}
	public void setKeystore(KeystoreConfig keystore) {
		this.keystore = keystore;
	}

	public static class AdminConfig {
		private String name;
		private String password;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getPassword() {
			return password;
		}
		public void setPassword(String password) {
			this.password = password;
		}
	}
	
	public static class KeystoreConfig {
		private String resourcePath;
		private String password;
		public String getResourcePath() {
			return resourcePath;
		}
		public void setResourcePath(String resourcePath) {
			this.resourcePath = resourcePath;
		}
		public String getPassword() {
			return password;
		}
		public void setPassword(String password) {
			this.password = password;
		}
	}
}

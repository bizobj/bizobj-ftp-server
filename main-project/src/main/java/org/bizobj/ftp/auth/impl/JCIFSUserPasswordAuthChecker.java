package org.bizobj.ftp.auth.impl;

import java.net.UnknownHostException;

import jcifs.UniAddress;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbSession;

import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.log4j.Logger;
import org.bizobj.ftp.auth.intf.UserPasswordAuthChecker;
import org.bizobj.ftp.server.CONFIG;

/**
 * Check user name and password from Active Directory Server
 * @author thinkbase
 */
public class JCIFSUserPasswordAuthChecker implements UserPasswordAuthChecker {
	/** The key of environment variable or java system property to indicate the AD Server Address  */
	public static final String KEY_AD_SERVER_ADDR = "AD_SERVER_ADDR";
	
	private static final Logger log = Logger.getLogger(JCIFSUserPasswordAuthChecker.class);
	
	@Override
	public boolean check(String userName, String password) throws AuthenticationFailedException {
		String adAddr = CONFIG.getConfigVar(getClass(), KEY_AD_SERVER_ADDR, null);
		if (null==adAddr || adAddr.trim().length()<=0){
			log.warn("Windows AD Server Address is not configurated, disable AD User Authentication.");
			return false;
		}
		
		log.info("Begin to check user ["+userName+"] from Windows AD Server: "+adAddr);
		UniAddress dc;
		try {
			dc = UniAddress.getByName(adAddr);
		} catch (UnknownHostException e) {
			log.error("UnknownHostException: "+adAddr + ", "+e.getMessage(), e);
			return false;
		}
		NtlmPasswordAuthentication na = new NtlmPasswordAuthentication(null, userName, password);
		try {
			SmbSession.logon(dc, na);
			log.info("User ["+userName+"] check from Windows AD Server: "+adAddr + " success");
			return true;
		} catch (SmbException e) {
			log.error("SmbException: "+e.getMessage(), e);
			return false;
		}
	}

}

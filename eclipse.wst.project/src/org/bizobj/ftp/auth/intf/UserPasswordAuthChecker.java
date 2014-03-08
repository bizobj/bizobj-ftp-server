package org.bizobj.ftp.auth.intf;

import org.apache.ftpserver.ftplet.AuthenticationFailedException;

public interface UserPasswordAuthChecker {
	/**
	 * Check user authentication by name and password
	 * @param user
	 * @param password
	 * @return true - check OK, false: check fail
	 * @throws AuthenticationFailedException User name and password mismatch
	 */
	public boolean check(String user, String password) throws AuthenticationFailedException;
}

package org.bizobj.test.debugonly;

import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.bizobj.ftp.auth.intf.UserPasswordAuthChecker;

/**
 * The implementation of {@link UserPasswordAuthChecker} for debug purpose only, which password is always "debug"
 * @author thinkbase
 */
public class DebugOnlyUserPasswordAuthChecker implements UserPasswordAuthChecker {

	@Override
	public boolean check(String user, String password) throws AuthenticationFailedException {
		return "debug".equals(password);
	}

}

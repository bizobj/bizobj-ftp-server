This FTP Server is based on apache ftp server(http://mina.apache.org/ftpserver-project)
and use it's PropertiesUserManager(http://mina.apache.org/ftpserver-project/configuration_user_manager_file.html)
as the user manager.

As a new feature, bizobj-ftp-server support customized user authentication(password
check), the user could both from user properties(File based user manager) and other
system such as Windows AD.
 * Case 1: User and it's password are all defined in user properties file;
 * Case 2: User is defined both in properties file and other system, so the password
           in properties file should be override by other system, but other user
           properties(such as home folder, max idle time, etc.) should follow properties
           file.
 * Case 3: User is only defined in other system, in this case, user's properties should
           following the default rule of bizobj-ftp-server.

Another new feature is "Virtual directory"(FileSystem Mapping), with this function, 
administrator can define virtual directories for ftp users, these virtual directories
could be mapped to folders in file system.
 * The mapping setting file named ".ftp-mapping.json", which stored in admin user's FTP
   site;
 * In default, FTP server should mapping "/public" and "/public/upload" to the folders
   in admin user's FTP site(for every ftp user, "/public" is readonly, and redirected to
   the folder "${admin's home}/public"; "/public/upload" is writable, redirected to folder
   "${admin's home}/public/upload"); Server should create a default ".ftp-mapping.json",
   so if you want to define your owner, read the default first;

1. The target runtime environment is Tomcat 7+, If deploy to other application
   server, modification of log4j.properties(Always in WEB-INF/classes) may needed;

2. By default, the ftp user properties file and user's ftp home are stored in
   "$HOME/.biaobj.org" folder, for example:
    - $HOME/.biaobj.org/conf/user.properties
    - $HOME/.biaobj.org/home/user1

3. You can use java system properties to change default settings, these property
   usually started with "org.bizobj.ftp.server.CONFIG_", for detail you can read
   the source of "org.bizobj.ftp.server.CONFIG.java";

4. User authentication by Windows AD is supported, to enable this feature, java
   system property "org.bizobj.ftp.server.CONFIG_AuthChecker" should be set to
   "org.bizobj.ftp.auth.impl.JCIFSUserPasswordAuthChecker", and use system property
   "org.bizobj.ftp.auth.impl.JCIFSUserPasswordAuthChecker_AD_SERVER_ADDR" to specify
   the ip address of AD Server;

5. Following is the example to customize FTP server in tomcat:
    export JAVA_OPTS="$JAVA_OPTS -Dorg.bizobj.ftp.server.CONFIG_FtpBase=/u01/ftp"
    export JAVA_OPTS="$JAVA_OPTS -Dorg.bizobj.ftp.server.CONFIG_AdminName=ftpadmin"
    export JAVA_OPTS="$JAVA_OPTS -Dorg.bizobj.ftp.server.CONFIG_AdminPwd=1@345^7*"
    export JAVA_OPTS="$JAVA_OPTS -Dorg.bizobj.ftp.server.CONFIG_AuthChecker=org.bizobj.ftp.auth.impl.JCIFSUserPasswordAuthChecker"
    export JAVA_OPTS="$JAVA_OPTS -Dorg.bizobj.ftp.auth.impl.JCIFSUserPasswordAuthChecker_AD_SERVER_ADDR=192.168.1.1"

END
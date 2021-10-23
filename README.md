bizobj-ftp-server
=================

The Java based FTP Server build upon apache ftp server(http://mina.apache.org/ftpserver-project/), focus on usability and manageability.

Please read [Embedding FtpServer in 5 minutes](http://mina.apache.org/ftpserver-project/embedding_ftpserver.html) first.

## Configuration

### application.yml

**NOTE**:

- Default port is `2223`;
- Default FTP base folder is `${user.dir}/ftp`;
- You can change Administrator's account and password;
- Default Authentication Program is `DebugOnlyUserPasswordAuthChecker`, which allow all user's password is `debug`;

### User manager

Based on [File based user manager](http://mina.apache.org/ftpserver-project/configuration_user_manager_file.html) and support customized authentication method, read `CustomAuthPropertiesUserManagerFactory.java` and `CustomAuthPropertiesUserManager.java` for detail.

### FileSystem Mapping

Every user has standalone file system, read `MappingNativeFileSystemFactory.java` for detail. And `admin` user has additional mapping configuration, see `MappingModel.java` for detail.

## FTPS(*FTP over SSL*) Support

### Prepare keystore

The following command to create build-in keystore file:

```bash
# Create keystore file "bizobj-keystore.jks", with password "20211023", 10 years(validity 3650)
keytool -genkey -keyalg RSA -dname "CN=bizobj.org, OU=bizobj.org, O=bizobj.org, L=bizobj.org, ST=bizobj.org, C=CN" -alias bizobj -keypass 20211023 -storepass 20211023 -validity 3650 -keystore bizobj-keystore.jks
# Read keystore informations
keytool -list -v -keystore bizobj-keystore.jks
```

You can also create truststore file(the truststore is not required, following commands are just for reference):

```bash
keytool -export -alias bizobj -keystore bizobj-keystore.jks -rfc -file bizobj-selfsigned.cer
keytool -import -alias bizobj -file bizobj-selfsigned.cer -keystore bizobj-truststore.jks -storepass 20211023
```

### Explicit vs Implicit Mode

**Implicit Mode is not support in current version!**

### JDK Version

Oracle JDK is recommended, **OpenJDK not support FTPS!**

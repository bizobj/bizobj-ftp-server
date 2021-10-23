package org.bizobj.ftpserver.ftp.staticservice;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.bizobj.ftpserver.cfg.ConfigProperties;
import org.bizobj.ftpserver.ftp.auth.intf.UserPasswordAuthChecker;
import org.bizobj.ftpserver.ftp.fs.mapping.MappingModel;
import org.bizobj.ftpserver.ftp.fs.mapping.MappingNativeFileSystemFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

@Component
public class FtpUtilService {
	/** The json (see {@link MappingModel}) file in admin's home, to define the FTP Mappings */
	private static String FIXED_FTP_MAPPING_CONFIG_FILE_OF_ADMIN = ".ftp-mapping.json";
	
	private static ConfigProperties CONFIG;
	@Autowired
	public void setConfig(ConfigProperties config) {
		FtpUtilService.CONFIG = config;
	}
	
	public static File getHome(String userName){
		String hBase = CONFIG.getFtpHome();
		String h = hBase + "/" + userName;
		File fh = new File(h);
		return fh;
	}
	
	public static BaseUser buildWritePermissionUser(String userName, String password) {
		try {
			BaseUser user = new BaseUser();
			List<Authority> auth = new ArrayList<Authority>();
			auth.add(new WritePermission());
			File userHome = getHome(userName);
			if (! userHome.exists()){
				userHome.mkdirs();
			}
			user.setAuthorities(auth);
			user.setEnabled(true);
			user.setHomeDirectory(userHome.getCanonicalPath());
			user.setMaxIdleTime(CONFIG.getMaxIdleSeconds());
			user.setName(userName);
			user.setPassword(password);
			return user;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static UserPasswordAuthChecker getAuthChecker() {
		String className = CONFIG.getAuthCheckerClass();
		if (null==className){
			return null;
		}else{
			UserPasswordAuthChecker c;
			try {
				c = (UserPasswordAuthChecker)Class.forName(className).getDeclaredConstructor().newInstance();
				return c;
			} catch (ReflectiveOperationException e) {
				return ExceptionUtils.rethrow(e);
			}
		}
	}
	
	public static MappingNativeFileSystemFactory buildMappingFileSystemFactory(BaseUser admin){
		try {
			String mappingJson = admin.getHomeDirectory() + "/" + FIXED_FTP_MAPPING_CONFIG_FILE_OF_ADMIN;
			MappingModel mm;
			File mjFile = new File(mappingJson);
			if (! mjFile.exists()){
				mm = initDefaultFileSystemMappingModel(admin, mjFile);
			}else{
				String json = FileUtils.readFileToString(mjFile, StandardCharsets.UTF_8);
				try {
					mm = JSON.parseObject(json, MappingModel.class);
				} catch (Throwable e) {
					mm = new MappingModel();	//If error return BLANK;
				}
			}
			return new MappingNativeFileSystemFactory(mm);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	private static MappingModel initDefaultFileSystemMappingModel(BaseUser admin, File mjFile){
		try {
			File pub = new File(admin.getHomeDirectory() + "/public");
			File upl = new File(admin.getHomeDirectory() + "/public/upload");
			MappingModel mm = new MappingModel();
			
			// /public/upload is writable
			mm.addMapping(".*", "/public/upload", upl, true);
			// /public is readonly
			mm.addMapping(".*", "/public", pub, false);
			String json = JSON.toJSONString(mm, SerializerFeature.PrettyFormat);
			
			FileUtils.write(mjFile, json, StandardCharsets.UTF_8);
			
			return mm;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}

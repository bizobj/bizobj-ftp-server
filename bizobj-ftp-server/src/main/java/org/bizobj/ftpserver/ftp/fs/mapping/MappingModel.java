package org.bizobj.ftpserver.ftp.fs.mapping;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.ftpserver.ftplet.User;

public class MappingModel {
	private Map<String, List<FSMapping>> mappings = new LinkedHashMap<String, List<FSMapping>>();
	
	public Map<String, List<FSMapping>> getMappings() {
		return mappings;
	}
	public void setMappings(Map<String, List<FSMapping>> mappings) {
		this.mappings = mappings;
	}
	public void addMapping(String userRegex, String ftpPath, File realPath, boolean writable){
		List<FSMapping> ms = mappings.get(userRegex);
		if (null==ms){
			ms = new ArrayList<FSMapping>();
			mappings.put(userRegex, ms);
		}
		try {
			ms.add(new FSMapping(ftpPath, realPath.getCanonicalPath(), writable));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static class FSMapping {
		private String ftpPath;
		private String realFile;
		private boolean writable = false;
		public FSMapping(){
			//fastjson(com.alibaba) need this default constructor
		}
		public FSMapping(String ftpPath, String realFile, boolean writable){
			this.ftpPath = ftpPath;
			this.realFile = realFile;
			this.writable = writable;
		}
		public boolean isWritable() {
			return writable;
		}
		public void setWritable(boolean writable) {
			this.writable = writable;
		}
		public String getFtpPath() {
			return ftpPath;
		}
		public String getRealFile() {
			return realFile;
		}
		public void setFtpPath(String ftpPath) {
			this.ftpPath = ftpPath;
		}
		public void setRealFile(String realFile) {
			this.realFile = realFile;
		}
	}
	
	public static class FSMappingResult {
		private File file;
		private boolean writable = false;
		public FSMappingResult(File file, boolean writable){
			this.file = file;
			this.writable = writable;
		}
		public File getFile() {
			return file;
		}
		public boolean isWritable() {
			return writable;
		}
	}
	
	public FSMappingResult getMappedFile(User user, String ftpPath){
		if (! ftpPath.endsWith("/")){
			ftpPath = ftpPath+"/";
		}
		String userId = user.getName();
		for(Map.Entry<String, List<FSMapping>> en: this.mappings.entrySet()){
			String userRegex = en.getKey();
			if (userId.matches(userRegex)){
				List<FSMapping> mList = en.getValue();
				for (FSMapping mapping: mList){
					String mpFtpPath = mapping.ftpPath;
					if (! mpFtpPath.endsWith("/")){
						mpFtpPath = mpFtpPath+"/";
					}
					if (ftpPath.equals(mpFtpPath)){
						return new FSMappingResult(
								new File(mapping.realFile), false /*writable is always false for the mapping root*/);
					}else if (ftpPath.startsWith(mpFtpPath)){
						String path = mapping.realFile + "/" + ftpPath.substring(mpFtpPath.length());
						File tmp = new File(path);
						return new FSMappingResult(tmp, mapping.writable);
					}
				}
			}
		}
		return new FSMappingResult(null, false);
	}
}

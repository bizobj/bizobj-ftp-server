package org.bizobj.ftpserver.ftp.fs.mapping;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.ftpserver.filesystem.nativefs.NativeFileSystemFactory;
import org.apache.ftpserver.filesystem.nativefs.impl.NativeFileSystemView;
import org.apache.ftpserver.filesystem.nativefs.impl.NativeFtpFile;
import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.impl.DefaultFtpServerContext;
import org.bizobj.ftpserver.ftp.fs.mapping.MappingModel.FSMappingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NativeFileSystemFactory} which support mapping, so it can redirect FTP request to a mapped file,
 * for example. redirect /public of all users to admin's "public" directory;
 * <br/>
 * The default {@link FileSystemFactory} was defined in {@link DefaultFtpServerContext#getFileSystemManager()}.
 * @author thinkbase
 *
 */
public class MappingNativeFileSystemFactory extends NativeFileSystemFactory {
	private static final Logger log = LoggerFactory.getLogger(MappingNativeFileSystemFactory.class);
	
	private MappingModel mappings;
	
	public MappingNativeFileSystemFactory(MappingModel mappings){
		super();
		this.mappings = mappings;
	}

	public MappingModel getMappings() {
		return mappings;
	}

	@Override
	public FileSystemView createFileSystemView(final User user) throws FtpException {
		final NativeFileSystemView nfsv = (NativeFileSystemView)super.createFileSystemView(user);
		FileSystemView result = new FileSystemView(){
			@Override
			public boolean changeWorkingDirectory(String dir) throws FtpException {
				boolean found = nfsv.changeWorkingDirectory(dir);
				if (!found && null!=mappings){
					//If dir not found, check if it's a mapped folder
					NativeFtpFile nf = (NativeFtpFile) nfsv.getFile(dir);
					String absFtpPath = nf.getAbsolutePath();
					FSMappingResult result = mappings.getMappedFile(user, absFtpPath);
					File realFile = result.getFile();
					if (null!=realFile){
						found = realFile.isDirectory() && realFile.exists();
					}
					if (found){
						log.info("* CHANGE DIR: (user="+user.getName()+"):["+dir+"] ==> ["+absFtpPath+"] = ["+realFile+"]");
						//HACK: Force change currDir field
						try {
							Field currDir = NativeFileSystemView.class.getDeclaredField("currDir");
							currDir.setAccessible(true);
							currDir.set(nfsv, absFtpPath + "/");
						} catch (Exception e) {
							return ExceptionUtils.rethrow(e);
						}
					}else{
						log.warn("* CHANGE DIR Fail: (user="+user.getName()+"):["+dir+"] ==> ["+absFtpPath+"] = ["+realFile+"]");
					}
				}
				return found;
			}
			@Override
			public void dispose() {
				nfsv.dispose();
			}
			@Override
			public FtpFile getFile(String file) throws FtpException {
				NativeFtpFile nf = (NativeFtpFile) nfsv.getFile(file);
				String absFile = nf.getAbsolutePath();
				//HACK: Wrapping NativeFtpFile and apply mapping
				if (null==mappings){
					return nf;
				}
				FSMappingResult result = mappings.getMappedFile(user, absFile);
				File realFile = result.getFile();
				if (null==realFile){
					return nf;
				}else if (realFile.equals(nf.getPhysicalFile())){
					return nf;
				}else{
					try {
						log.info("* MAPPING user "+user.getName()+"'s ["+file+"] ==> ["+absFile+"] = ["+realFile+"] ...");
						Constructor<NativeFtpFile> cons =
								NativeFtpFile.class.getDeclaredConstructor(String.class, File.class, User.class);
						cons.setAccessible(true);
						final NativeFtpFile hacked = cons.newInstance(absFile, realFile, user);
						if (!result.isWritable()){
							log.info("* READONLY: User "+user.getName()+"'s ["+absFile+"("+realFile+").");
							//Use dynamic class to override "isWritable" and "isRemovable"
							FtpFile ff = new FtpFile(){
								public boolean isWritable() {
									return false;
								}
								public boolean isRemovable() {
									return false;
								}
								public String getAbsolutePath() {
									return hacked.getAbsolutePath();
								}
								public String getName() {
									return hacked.getName();
								}
								public boolean isHidden() {
									return hacked.isHidden();
								}
								public boolean isDirectory() {
									return hacked.isDirectory();
								}
								public boolean isFile() {
									return hacked.isFile();
								}
								public boolean doesExist() {
									return hacked.doesExist();
								}
								public long getSize() {
									return hacked.getSize();
								}
								public String getOwnerName() {
									return hacked.getOwnerName();
								}
								public String getGroupName() {
									return hacked.getGroupName();
								}
								public int getLinkCount() {
									return hacked.getLinkCount();
								}
								public long getLastModified() {
									return hacked.getLastModified();
								}
								public boolean setLastModified(long time) {
									return hacked.setLastModified(time);
								}
								public boolean isReadable() {
									return hacked.isReadable();
								}
								public boolean delete() {
									return hacked.delete();
								}
								public boolean move(FtpFile dest) {
									return hacked.move(dest);
								}
								public boolean mkdir() {
									return hacked.mkdir();
								}
								public List<FtpFile> listFiles() {
									return hacked.listFiles();
								}
								public OutputStream createOutputStream(long offset) throws IOException {
									return hacked.createOutputStream(offset);
								}
								public InputStream createInputStream(long offset) throws IOException {
									return hacked.createInputStream(offset);
								}
								public Object getPhysicalFile() {
									return hacked.getPhysicalFile();
								}
							};
							return ff;
						}else{
							log.info("* WRITABLE: User "+user.getName()+"'s ["+absFile+"("+realFile+").");
							return hacked;
						}
					} catch (Exception e) {
						return ExceptionUtils.rethrow(e);
					}
				}
			}
			@Override
			public FtpFile getHomeDirectory() throws FtpException {
				return nfsv.getHomeDirectory();
			}
			@Override
			public FtpFile getWorkingDirectory() throws FtpException {
				return nfsv.getWorkingDirectory();
			}
			@Override
			public boolean isRandomAccessible() throws FtpException {
				return nfsv.isRandomAccessible();
			}
		};
		return result;
	}
}

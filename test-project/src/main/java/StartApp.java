import java.io.File;
import java.net.URL;

import org.bizobj.jetty.ContextStarter;
import org.bizobj.test.debugonly.DebugOnlyUserPasswordAuthChecker;

/**
 * Start the test App.
 * @author root
 */
public class StartApp {

    public static void main(String[] args) throws Exception {
    	System.setProperty(ContextStarter.VAR_CTX_PATH, "ftp");
    	
        //log4j.properties (As a place-holder) should be compiled into main-project/target/classes ...
    	URL holder = StartApp.class.getResource("/log4j.properties");
        File fh = new File(holder.toURI());
        String warFolder = fh.getParentFile().getParentFile().getParentFile().getCanonicalPath() + "/src/main/webapp";
        String serverBase = fh.getParentFile().getParentFile().getCanonicalPath();
    	//Set ftp port to 2121
    	System.setProperty("org.bizobj.ftp.server.CONFIG_Port", "2121");
    	//Set ftp base folder
    	System.setProperty("org.bizobj.ftp.server.CONFIG_FtpBase", serverBase);
    	//Setup a debug-purpose AuthChecker, just for debugging only
    	System.setProperty("org.bizobj.ftp.server.CONFIG_AuthChecker", DebugOnlyUserPasswordAuthChecker.class.getName());
        //Start App ...
        ContextStarter.startServer(warFolder);
    }
}

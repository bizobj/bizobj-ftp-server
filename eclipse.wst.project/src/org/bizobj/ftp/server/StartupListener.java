package org.bizobj.ftp.server;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

/**
 * Auto-start ftp server when web context starting
 */
public class StartupListener implements ServletContextListener {
	private static final String KEY_SERVER_IN_ATTRIBUTES = StartupListener.class.getName()+":"+Server.class.getName();
	
	private static final Logger log = Logger.getLogger(StartupListener.class);
	private Server server;
	
    /**
     * Default constructor. 
     */
    public StartupListener() {
        // Do nothing
    }

	/**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent e) {
    	log.info("Context Initialized, begin to startup FTP Server ...");
    	server = new Server();
    	server.start();
    	//Remember server into context
    	e.getServletContext().setAttribute(KEY_SERVER_IN_ATTRIBUTES, server);
    }

	/**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent e) {
        if (null!=this.server){
        	this.server.stop();
        	log.info("Context Destroyed, FTP Server stopped.");
        }
    }
	
    
    public static Server getFtpServer(ServletContext context){
    	return (Server)context.getAttribute(KEY_SERVER_IN_ATTRIBUTES);
    }
}

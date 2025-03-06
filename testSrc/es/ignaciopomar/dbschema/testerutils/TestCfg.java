
package es.ignaciopomar.dbschema.testerutils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.util.Properties;


public class TestCfg
{
	protected static final String CFG_FILE_NAME	   = "dbschema.cfg";

	// Envoirement configuration variables
	private boolean				  isJarFile;
	private String				  jarPath;
	private Object				  jarName;
	private String				  mainClass;

	private boolean				  isLoadedFromFile = false;

	// Database configuration variables
	public String				  server;
	public int					  port;
	public String				  user;
	public String				  password;
	public String				  dbName;

	//
	public String				  dbNameSecondary;

	/**
	 * Constructor: check if we are in a jar file or in a development environment
	 */
	public TestCfg ()
	{
		boolean isInsideDebugger = java.lang.management.ManagementFactory.getRuntimeMXBean ().getInputArguments ()
		        .toString ().indexOf ("-agentlib:jdwp") > 0;

		boolean isJarFile = this.setJarPath ();

		if (this.mainClass == null)
		{
			// we only set this in the first execution
			this.mainClass = getMainClassName ();
		}

		if (this.jarName == null)
		{
			this.jarName = this.mainClass;
		}

		if (isInsideDebugger && !isJarFile)
		{
			// We are in a development environment
			this.setDebugBinFolder ();
		}

	}

	/**
	 * Set the debug bin folder
	 */
	private void setDebugBinFolder ()
	{
		File jarFile = new File (jarPath);
		jarPath = jarFile.getParentFile ().getParentFile ().getPath ();
		jarPath += File.separator;

		// this.makeSureFolderExists (jarPath);

		// change the current path to the jar path
		// YAGNI: Maybe we should use JNI https://stackoverflow.com/a/59397607/74785
		File directory = new File (jarPath).getAbsoluteFile ();
		System.setProperty ("user.dir", directory.getAbsolutePath ());

	}

	/**
	 * Get the main class name
	 * 
	 * @return
	 */
	private static String getMainClassName ()
	{
		StackTraceElement trace[] = Thread.currentThread ().getStackTrace ();
		if (trace.length > 0)
		{
			return trace[trace.length - 1].getClassName ();
		}
		return "";
	}

	/**
	 * Find if exists a jar file and set the path accordingly
	 * 
	 * @return
	 */
	private boolean setJarPath ()
	{
		this.isJarFile = false;

		try
		{
			CodeSource codeSource = this.getClass ().getProtectionDomain ().getCodeSource ();
			this.jarPath = codeSource.getLocation ().toURI ().getPath ();
			this.jarName = null;

			// In development environment, the jar file does not exist, and the path is different
			if (jarPath.endsWith (".jar"))
			{
				File jarFile = new File (jarPath);
				jarPath = jarFile.getParentFile ().getPath ();
				this.jarName = jarFile.getName ();
				isJarFile = true;
			}

			if (!(jarPath.endsWith ("\\") || jarPath.endsWith ("/")))
			{
				jarPath = jarPath + File.separator;
			}

			// Removving Windows shit
			String os = System.getProperty ("os.name").toLowerCase ();
			if (os.indexOf ("win") >= 0)
			{
				jarPath = jarPath.replace ("\\", "/");
				if (jarPath.charAt (0) == '/' && jarPath.charAt (2) == ':')
				{
					jarPath = jarPath.substring (1);
				}

			}

		}
		catch (URISyntaxException e)
		{
			// this should not happen
			e.printStackTrace ();
			jarPath = "." + File.separator;
		}
		return isJarFile;

	}

	public boolean isConfigured ()
	{

		return this.isLoadedFromFile && server != null && !server.isEmpty () && port != 0 && user != null &&
		       !user.isEmpty () && password != null && !password.isEmpty () && dbName != null && !dbName.isEmpty ();
	}

	/**
	 * Loads the configuration from varios possible paths
	 */
	public void loadCfg ()
	{
		// Potential cfgFolder
		File jarFile = new File (jarPath);
		String jarPath2 = jarFile.getParentFile ().getPath ();
		jarPath2 += File.separator;

		String[] folders = {jarPath, jarPath2 };

		for (String folderPath : folders)
		{
			File folder = new File (folderPath);
			File file = new File (folder, CFG_FILE_NAME);

			// Check if the file exists and is a regular file
			if (file.exists () && file.isFile ())
			{
				this.loadCfg (file.getAbsolutePath ());
				break; // Exit loop once the file is found
			}
		}
	}

	/**
	 * Loads the configuration from a specific file
	 * 
	 * @param absolutePath
	 */
	private void loadCfg (String cfgFileName)
	{
		Properties prop = new Properties ();

		this.isLoadedFromFile = false;
		try
		{
			FileInputStream is = new FileInputStream (cfgFileName);
			prop.load (is);
			is.close ();
			this.isLoadedFromFile = true;

			this.server = prop.getProperty ("mysql.server");
			this.port = Integer.parseInt (prop.getProperty ("mysql.port"));
			this.user = prop.getProperty ("mysql.user");
			this.password = prop.getProperty ("mysql.password");
			this.dbName = prop.getProperty ("mysql.dbname");
			this.dbNameSecondary = prop.getProperty ("mysql.dbnameAlt");

		}
		catch (java.io.FileNotFoundException e)
		{
			// We have checked that the file exists, so this should not happen
		}
		catch (IOException e)
		{
			System.out.println (e.toString ());
		}

	}

	public String getDbname ()
	{
		return this.dbName;
	}

	public String getSrv ()
	{
		return this.server;
	}

	public int getPort ()
	{

		return this.port;
	}

	public String getUser ()
	{
		return this.user;
	}

	public String getPass ()
	{
		return this.password;
	}

	public String getDbname2 ()
	{
		return this.dbNameSecondary;
	}

	public String getBasePath ()
	{
		return this.jarPath;
	}

}

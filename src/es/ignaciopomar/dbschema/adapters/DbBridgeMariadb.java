
package es.ignaciopomar.dbschema.adapters;

import es.ignaciopomar.dbschema.DbBridge;
import es.ignaciopomar.dbschema.SchemaObserver;
import es.ignaciopomar.dbschema.types.DbErrorCode;
import es.ignaciopomar.dbschema.types.Field;


public class DbBridgeMariadb implements DbBridge
{
	public boolean			   hasPreexitingConnection = false;
	public DbErrorCode		   errCode				   = DbErrorCode.DB_NO_ERROR;
	public static final String errMsg				   = "";
	public static final String sql					   = "";

	// Real database connection data
	private String			   server;
	private int				   port;
	private String			   user;
	private String			   password;
	private String			   dbName;

	public DbBridgeMariadb (String srv, int port, String user, String pass, String dbname)
	{
		this.server = srv;
		this.port = port;
		this.user = user;
		this.password = pass;
		this.dbName = dbname;
	}

	@Override
	public boolean execute (String query, String schema)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean checkIfTableExists (String tableName, String schema)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getTableName (String tableName, String schema)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getColumnType (Field field)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sendTableDescription (String tableName, String schema, SchemaObserver schemaObserver)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isCompatible (Field schemaFld, Field dbFld)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void dropAllTablesFromSchema (String schema)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public DbErrorCode getLastErrCode ()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getErrorDescription ()
	{
		// TODO Auto-generated method stub
		return null;
	}

}

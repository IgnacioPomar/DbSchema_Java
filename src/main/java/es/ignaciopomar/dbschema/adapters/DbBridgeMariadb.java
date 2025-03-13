
package es.ignaciopomar.dbschema.adapters;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import es.ignaciopomar.dbschema.DbBridge;
import es.ignaciopomar.dbschema.SchemaObserver;
import es.ignaciopomar.dbschema.types.DbErrorCode;
import es.ignaciopomar.dbschema.types.Field;
import es.ignaciopomar.dbschema.types.FieldType;


public class DbBridgeMariadb implements DbBridge
{
	public boolean								 hasPreexitingConnection = false;
	public DbErrorCode							 errCode				 = DbErrorCode.DB_NO_ERROR;
	public String								 errMsg					 = "";
	public String								 sql					 = "";

	// Real database connection data
	private String								 server;
	private int									 port;
	private String								 user;
	private String								 password;
	private String								 dbName;
	private Connection							 conn					 = null;

	private static final Map <String, FieldType> fieldTypeMapping		 = new HashMap <> ();

	static
	{
		fieldTypeMapping.put ("int", FieldType.INTEGER);
		fieldTypeMapping.put ("double", FieldType.REAL);
		fieldTypeMapping.put ("decimal", FieldType.DECIMAL);
		fieldTypeMapping.put ("tinyint", FieldType.BOOL);
		fieldTypeMapping.put ("bit", FieldType.BOOL);
		fieldTypeMapping.put ("varchar", FieldType.STRING);
		fieldTypeMapping.put ("text", FieldType.TEXT);
		fieldTypeMapping.put ("longtext", FieldType.JSON);
		fieldTypeMapping.put ("varbinary", FieldType.BINARY);
		fieldTypeMapping.put ("blob", FieldType.BLOB);
		fieldTypeMapping.put ("date", FieldType.DATE);
		fieldTypeMapping.put ("time", FieldType.TIME);
		fieldTypeMapping.put ("timestamp", FieldType.TIMESTAMP);
		fieldTypeMapping.put ("inet6", FieldType.INET6);
	}

	/**
	 * Constructor.
	 * 
	 * @param srv
	 *            servidor
	 * @param port
	 *            puerto
	 * @param user
	 *            usuario
	 * @param pass
	 *            contraseña
	 * @param dbname
	 *            nombre de la base de datos
	 */
	public DbBridgeMariadb (String srv, int port, String user, String pass, String dbname)
	{
		this.server = srv;
		this.port = port;
		this.user = user;
		this.password = pass;
		this.dbName = dbname;

		this.initializeConnection ();

	}

	/**
	 * Inicializa la conexión JDBC a la base de datos.
	 */
	private void initializeConnection ()
	{
		String dbUrl = "jdbc:mariadb://" + this.server + ":" + this.port + "/" + this.dbName;
		Properties properties = new Properties ();
		properties.setProperty ("user", this.user);
		properties.setProperty ("password", this.password);
		try
		{
			this.conn = DriverManager.getConnection (dbUrl, properties);
		}
		catch (SQLException e)
		{
			this.conn = null;
			this.errMsg = e.getMessage ();
			this.errCode = DbErrorCode.CONNECT;
		}
	}

	@Override
	public boolean execute (String query, String schema)
	{
		try (Statement stmt = this.conn.createStatement ())
		{
			stmt.executeUpdate (query);
			return true;
		}
		catch (SQLException e)
		{
			this.errMsg = e.getMessage ();
			this.errCode = DbErrorCode.QUERY;
			this.sql = query;
			return false;
		}

		// return false;
	}

	@Override
	public boolean checkIfTableExists (String tableName, String schema)
	{
		if (this.errCode != DbErrorCode.DB_NO_ERROR)
		{
			return false;
		}
		String sql = "SELECT 1 FROM " + getTableName (tableName, schema) + " LIMIT 1;";

		try (Statement stmt = this.conn.createStatement ())
		{
			boolean hasResultSet = stmt.execute (sql);
			return hasResultSet;

		}
		catch (SQLException e)
		{
			// If it doenst exists, it will throw an exception, but it is not an error
			if (e.getErrorCode () == 1146)
			{
				// table does not exist: is not an error
				this.errCode = DbErrorCode.DB_NO_ERROR;
				this.errMsg = "";
				this.sql = "";

			}
			else
			{
				// other error: is a real error
				this.errCode = DbErrorCode.QUERY;
				this.errMsg = e.getMessage ();
				this.sql = sql;

			}
			return false;

		}

	}

	@Override
	public String getTableName (String tableName, String schema)
	{
		return (schema == null || schema.isEmpty ())? tableName : schema + "." + tableName;
	}

	@Override
	public String getColumnType (Field field)
	{
		switch (field.type)
		{
		case INTEGER:
			return field.isAutoIncrement? " int NOT NULL AUTO_INCREMENT " : " int ";
		case REAL:
			return " double ";
		case DECIMAL:
			return " DECIMAL(20, 6) ";
		case BOOL:
			return " BIT ";
		case STRING:
			return (field.size > 0)? " VARCHAR(" + field.size + ") " : " TEXT ";
		case TEXT:
			return " TEXT ";
		case JSON:
			return " json ";
		case BINARY:
			return (field.size > 0)? " VARBINARY(" + field.size + ") " : " BLOB ";
		case BLOB:
			return " BLOB ";
		case DATE:
			return " DATE ";
		case TIME:
			return " TIME ";
		case DATETIME:
			return " DATETIME ";
		case TIMESTAMP:
			return " TIMESTAMP ";
		case INET6:
			return " INET6 ";
		case UUID:
			return " UUID ";
		default:
			return "VARCHAR(255)";
		}

	}

	@Override
	public void sendTableDescription (String tableName, String schema, SchemaObserver schemaObserver)
	{
		if (this.errCode != DbErrorCode.DB_NO_ERROR)
		{
			return;
		}
		sendFieldsDescription (tableName, schema, schemaObserver);
		sendIndexesDescription (tableName, schema, schemaObserver);
		schemaObserver.finish ();

	}

	private void sendFieldsDescription (String tableName, String schema, SchemaObserver schemaObserver)
	{
		String sql = "DESCRIBE " + getTableName (tableName, schema) + ";";
		try (Statement stmt = this.conn.createStatement (); ResultSet rs = stmt.executeQuery (sql))
		{
			while (rs.next ())
			{
				Field fld = new Field ();
				fld.name = rs.getString ("Field");

				String type = rs.getString ("Type");
				int pos = type.indexOf ('(');
				if (pos != -1)
				{
					int posClose = type.indexOf (')', pos);
					String sizeStr = type.substring (pos + 1, posClose);
					try
					{
						fld.size = Integer.parseInt (sizeStr);
					}
					catch (NumberFormatException ex)
					{
						fld.size = 0;
					}
					type = type.substring (0, pos);
				}
				else
				{
					fld.size = 0;
				}
				fld.type = getFieldType (type);
				schemaObserver.addField (fld);
			}
		}
		catch (SQLException e)
		{
			this.errMsg = e.getMessage ();
			this.errCode = DbErrorCode.QUERY;
			this.sql = sql;
		}

	}

	private void sendIndexesDescription (String tableName, String schema, SchemaObserver schemaObserver)
	{
		String sql = "SHOW INDEX FROM " + getTableName (tableName, schema) + ";";
		try (Statement stmt = this.conn.createStatement (); ResultSet rs = stmt.executeQuery (sql))
		{
			while (rs.next ())
			{
				String indexName = rs.getString ("Key_name");
				String columnName = rs.getString ("Column_name");
				if ("PRIMARY".equals (indexName))
				{
					schemaObserver.addPrimaryIdxField (columnName);
				}
				else
				{
					schemaObserver.addIdxField (indexName, columnName);
				}
			}
		}
		catch (SQLException e)
		{
			this.errMsg = e.getMessage ();
			this.errCode = DbErrorCode.QUERY;
			this.sql = sql;
		}

	}

	@Override
	public boolean isCompatible (Field schemaFld, Field dbFld)
	{
		switch (schemaFld.type)
		{
		case INTEGER:
		case REAL:
		case DECIMAL:
		case BOOL:
		case TEXT:
		case JSON:
		case BLOB:
		case DATE:
		case TIME:
		case TIMESTAMP:
		case INET6:
			return dbFld.type == schemaFld.type;

		case STRING:
			return (schemaFld.size == 0 && dbFld.type == FieldType.TEXT) ||
			       (schemaFld.size > 0 && dbFld.type == FieldType.STRING && schemaFld.size == dbFld.size);

		case BINARY:
			return (schemaFld.size == 0 && dbFld.type == FieldType.BLOB) ||
			       (schemaFld.size > 0 && dbFld.type == FieldType.BINARY && schemaFld.size == dbFld.size);

		default:
			return false;
		}
	}

	@Override
	public void dropAllTablesFromSchema (String schema)
	{
		if (schema == null || schema.isEmpty ())
		{
			schema = this.dbName;
		}

		String sqlShow = "SHOW TABLES FROM " + schema + ";";
		try (Statement stmt = this.conn.createStatement (); ResultSet rs = stmt.executeQuery (sqlShow))
		{
			while (rs.next ())
			{
				String tableName = rs.getString (1);
				String dropSql = "DROP TABLE " + getTableName (tableName, schema) + ";";
				try (Statement dropStmt = this.conn.createStatement ())
				{
					dropStmt.execute (dropSql);
				}
				catch (SQLException e)
				{
					this.errMsg = e.getMessage ();
					this.errCode = DbErrorCode.QUERY;
				}
			}
		}
		catch (SQLException e)
		{
			this.errMsg = e.getMessage ();
			this.errCode = DbErrorCode.QUERY;
		}

	}

	@Override
	public DbErrorCode getLastErrCode ()
	{
		return this.errCode;
	}

	@Override
	public String getErrorDescription ()
	{
		return this.errMsg;
	}

	/**
	 * Cierra la conexión JDBC a la base de datos.
	 */
	@Override
	public void close ()
	{
		if (this.conn != null)
		{
			try
			{
				this.conn.close ();
			}
			catch (SQLException e)
			{
				// Se puede registrar el error si se requiere.
			}
		}

	}

	public FieldType getFieldType (String type)
	{
		if (type == null)
		{
			return FieldType.STRING;
		}
		// Se consulta el mapa, utilizando la cadena en minúsculas para evitar problemas con mayúsculas.
		FieldType fieldType = fieldTypeMapping.get (type.toLowerCase ());
		return (fieldType != null)? fieldType : FieldType.STRING;
	}

}

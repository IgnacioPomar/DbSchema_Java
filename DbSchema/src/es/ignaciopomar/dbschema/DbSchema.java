
package es.ignaciopomar.dbschema;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import es.ignaciopomar.dbschema.types.DbErrorCode;


/**
 * Representa el esquema de la base de datos.
 * Convertido de C++ a Java siguiendo los estándares de Java.
 */
public class DbSchema
{
	// Se asume que estas clases están definidas en tu proyecto
	private SchemaVariables		schemaVariables;

	private List <DbBridge>		dbBridges = new ArrayList <DbBridge> ();

	// private MariadbInfo mariadbInfo;
	// private SQliteInfo sqliteInfo;

	private static final Logger	logger	  = Logger.getLogger (DbSchema.class.getName ());

	// Constructor
	public DbSchema ()
	{
		this.schemaVariables = new SchemaVariables ();
		// this.mariadbInfo = new MariadbInfo ();
		// this.sqliteInfo = new SQliteInfo ();
	}

	public void addDbBridge (DbBridge dbBridge)
	{
		this.dbBridges.add (dbBridge);
	}

	/**
	 * Agrega una variable de esquema.
	 *
	 * @param variableName
	 *            nombre de la variable
	 * @param schemaName
	 *            nombre del esquema
	 */
	public void addVarSchema (String variableName, String schemaName)
	{
		this.schemaVariables.put (variableName, schemaName);
	}

	/**
	 * Crea o actualiza el esquema basado en los archivos JSON encontrados en el directorio indicado.
	 *
	 * @param schemaPath
	 *            ruta del directorio del esquema
	 * @return true si la operación fue exitosa; false en caso contrario.
	 */
	public boolean createOrUpdate (Path schemaPath)
	{
		logger.fine ("----- Updating Schema: Start -----");

		boolean retVal = false;
		boolean hasAnyBridge = false;
		for (DbBridge dbBridge : this.dbBridges)
		{
			if (!hasAnyBridge)
			{
				hasAnyBridge = true;
				retVal = true;
			}

			retVal = retVal && this.createOrUpdate (schemaPath, dbBridge);
		}

		return retVal;

	}

	private boolean createOrUpdate (Path schemaPath, DbBridge dbBridge)
	{
		boolean retVal = true;
		try
		{
			if (Files.exists (schemaPath) && Files.isDirectory (schemaPath))
			{
				try (DirectoryStream <Path> stream = Files.newDirectoryStream (schemaPath))
				{
					for (Path entry : stream)
					{
						if (Files.isRegularFile (entry) && entry.getFileName ().toString ().endsWith (".json"))
						{
							TableSchema table = new TableSchema (entry, this.schemaVariables);
							if (table.isValid ())
							{ // Se asume que TableSchema posee el método isValid()
								if (dbBridge.errCode == DbErrorCode.DB_NO_ERROR)
								{
									retVal = retVal && table.createOrUpdate (dbBridge);
								}
								if (this.sqliteInfo.isEnabled && this.sqliteInfo.errCode == DbErrorCode.DB_NO_ERROR)
								{
									retVal = retVal && table.createOrUpdate (bridgeSqlite);
								}
							}
						}
					}
				}
				logger.fine ("----- Updating Schema: END -----");
				// Comentario: se puede definir un comportamiento para el caso de tablas que existan en la base de datos
				return retVal;
			}
			else
			{
				logger.severe ("Schema Folder does not exist or is not accessible");
				return false;
			}
		}
		catch (IOException e)
		{
			logger.severe ("Filesystem error: " + e.getMessage ());
			return false;
		}
		catch (Exception e)
		{
			logger.severe ("General error: " + e.getMessage ());
			return false;
		}

		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Elimina todas las tablas del esquema.
	 * Destinado únicamente para pruebas unitarias.
	 *
	 * @param schemaName
	 *            nombre del esquema
	 */
	public void dropAllTablesFromSchemaForUnitTest (String schemaName)
	{
		for (DbBridge dbBridge : this.dbBridges)
		{
			dbBridge.dropAllTablesFromSchema (schemaName);

		}

	}
}

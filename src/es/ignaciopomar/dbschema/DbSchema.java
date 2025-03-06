
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
	private SchemaVariables		 schemaVariables = new SchemaVariables ();

	private List <DbBridge>		 dbBridges		 = new ArrayList <DbBridge> ();

	// private MariadbInfo mariadbInfo;
	// private SQliteInfo sqliteInfo;

	private final DbSchemaLogger logger;

	// Constructor
	public DbSchema (DbSchemaLogger logger)
	{
		this.logger = logger;
	}

	public void addDbBridge (DbBridge dbBridge)
	{
		if (dbBridge != null)
		{
			this.dbBridges.add (dbBridge);
		}
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
		this.logger.info ("----- Updating Schema: Start -----");

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
							TableSchema table = new TableSchema (entry, this.schemaVariables, this.logger);

							if (dbBridge.getLastErrCode () == DbErrorCode.DB_NO_ERROR)
							{
								retVal = retVal && table.createOrUpdate (dbBridge);
							}
						}
					}
				}
				this.logger.info ("----- Updating Schema: END -----");
				// Comentario: se puede definir un comportamiento para el caso de tablas que existan en la base de datos
				return retVal;
			}
			else
			{
				this.logger.error ("Schema Folder does not exist or is not accessible");
				return false;
			}
		}
		catch (IOException e)
		{
			this.logger.error ("Filesystem error: " + e.getMessage ());
			return false;
		}
		catch (Exception e)
		{
			this.logger.error ("General error: " + e.getMessage ());
			return false;
		}

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

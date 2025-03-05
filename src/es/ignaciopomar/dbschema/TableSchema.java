
package es.ignaciopomar.dbschema;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import es.ignaciopomar.dbschema.types.FieldType;


/**
 * TableSchema: Carga la definición de una tabla desde un archivo JSON y la traduce a comandos SQL.
 * Extiende TableStructure.
 */
public class TableSchema extends TableStructure
{

	// Indica si el esquema es válido (se pudo parsear correctamente el JSON).
	public boolean				 isValid;
	private final DbSchemaLogger logger;

	/**
	 * Constructor que carga el esquema de la tabla desde el archivo ubicado en tablePath.
	 *
	 * @param tablePath
	 *            Ruta al archivo JSON con la definición de la tabla.
	 * @param schemaVariables
	 *            Variables de esquema para resolver nombres (en caso de "varSchema").
	 * @param logger
	 */
	public TableSchema (Path tablePath, SchemaVariables schemaVariables, DbSchemaLogger logger)
	{
		this.logger = logger;
		isValid = true;
		try
		{
			// Leer el contenido completo del archivo
			String content = new String (Files.readAllBytes (tablePath));
			JSONObject jobj = new JSONObject (content);

			// Obtener el nombre de la tabla
			String aux = jobj.optString ("tablename", null);
			if (aux != null)
			{
				this.setTableName (aux);
			}
			else
			{
				isValid = false;
				return;
			}

			// Obtener el nombre del esquema
			this.setSchemaName ("");
			aux = jobj.optString ("schema", null);
			if (aux != null)
			{
				this.setSchemaName (aux);
			}
			else
			{
				aux = jobj.optString ("varSchema", null);
				if (aux != null)
				{
					this.setSchemaName (schemaVariables.getVarSchema (aux));
				}
			}

			// Procesar el array "fields"
			int position = 0;
			JSONArray fieldsArray = jobj.optJSONArray ("fields");
			if (fieldsArray != null)
			{
				for (int i = 0; i < fieldsArray.length (); i++)
				{
					Object objElem = fieldsArray.get (i);
					if (objElem instanceof JSONObject)
					{
						JSONObject fieldObj = (JSONObject) objElem;
						String fieldName = fieldObj.optString ("name", null);
						if (fieldName != null)
						{
							Field fld = new Field ();
							fld.name = fieldName;
							// Parsear el tipo de campo (usa el método privado parseString)
							String typeStr = fieldObj.optString ("type", "STRING");
							fld.type = FieldType.fromString (typeStr);
							// Nota: se utiliza "lenght" (según el JSON original) para obtener el tamaño
							fld.size = fieldObj.optInt ("lenght", 0);
							// El campo "notnull" está comentado en el original
							fld.isAutoIncrement = fieldObj.optBoolean ("autoincrement", false);
							fld.position = position++;
							// Agregar el campo a la lista de fields heredada de TableStructure
							this.getFields ().add (fld);
						}
					}
				}
			}

			// Procesar el array "indexes"
			int idxNum = 0;
			JSONArray indexesArray = jobj.optJSONArray ("indexes");
			if (indexesArray != null)
			{
				for (int i = 0; i < indexesArray.length (); i++)
				{
					Object objElem = indexesArray.get (i);
					if (objElem instanceof JSONObject)
					{
						JSONObject indexObj = (JSONObject) objElem;
						boolean isPrimary = indexObj.optBoolean ("primary", false);
						Index idx;
						if (isPrimary)
						{
							idx = this.getPrimary (); // Se asume que TableStructure posee el índice primario
						}
						else
						{
							idx = new Index ();
							this.getIdxs ().add (idx);
						}
						if (!isPrimary)
						{
							String idxName = indexObj.optString ("name", null);
							if (idxName == null)
							{
								idx.setName (this.getTableName () + "_" + idxNum);
								idxNum++;
							}
							else
							{
								idx.setName (idxName);
							}
						}
						// Procesar los campos del índice
						JSONArray idxFields = indexObj.optJSONArray ("fields");
						if (idxFields != null)
						{
							for (int j = 0; j < idxFields.length (); j++)
							{
								if (idxFields.get (j) instanceof String)
								{
									String idxField = idxFields.getString (j);
									idx.getFields ().add (idxField);
								}
							}
						}
					}
				}
			}
		}
		catch (IOException e)
		{
			logger.error ("Error reading file: " + e.getMessage ());
			isValid = false;
		}
		catch (Exception e)
		{
			logger.error ("Error parsing JSON: " + e.getMessage ());
			isValid = false;
		}
	}

	/**
	 * Crea o actualiza la tabla en la base de datos.
	 *
	 * @param dbBridge
	 *            Objeto Bridge para interactuar con la base de datos.
	 * @return true si la operación fue exitosa; false en caso contrario.
	 */
	public boolean createOrUpdate (DbBridge dbBridge)
	{
		if (dbBridge.checkIfTableExists (this.getTableName (), this.getSchemaName ()))
		{
			return update (dbBridge);
		}
		else
		{
			return create (dbBridge);
		}
	}

	/**
	 * Crea la tabla en la base de datos.
	 *
	 * @param dbBridge
	 *            Objeto Bridge para interactuar con la base de datos.
	 * @return true si se creó correctamente; false en caso de error.
	 */
	public boolean create (DbBridge dbBridge)
	{
		String tablename = dbBridge.getTableName (this.getTableName (), this.getSchemaName ());
		StringBuilder sql = new StringBuilder ("CREATE TABLE " + tablename + " (");
		String separator = "";
		// Recorrer cada campo y generar la parte de definición de la columna.
		for (Field field : this.getFields ())
		{
			sql.append (separator).append (field.name).append (dbBridge.getColumnType (field));
			separator = ", ";
		}
		// Agregar clave primaria si se definieron campos en el índice primario.
		if (!this.getPrimary ().getFields ().isEmpty ())
		{
			StringBuilder pk = new StringBuilder ();
			String sep = "";
			pk.append (", PRIMARY KEY (");
			for (String idxField : this.getPrimary ().getFields ())
			{
				pk.append (sep).append (idxField);
				sep = ", ";
			}
			pk.append (")");
			sql.append (pk.toString ());
		}
		sql.append (");");

		boolean created = dbBridge.execute (sql.toString (), this.getSchemaName ());
		if (!created)
		{
			logger.error (tablename + " " + dbBridge.getErrorDescription ());
			return false;
		}
		// Crear índices secundarios.
		for (Index idx : this.getIdxs ())
		{
			StringBuilder idxSql = new StringBuilder ("CREATE INDEX " + idx.getName ());
			idxSql.append (" ON " + tablename + " (");
			String sep = "";
			for (String idxField : idx.getFields ())
			{
				idxSql.append (sep).append (idxField);
				sep = ", ";
			}
			idxSql.append (");");
			created = created && dbBridge.execute (idxSql.toString (), this.getSchemaName ());
		}
		if (!created)
		{
			logger.error (tablename + " " + dbBridge.getErrorDescription ());
		}
		else
		{
			logger.info ("CREATED:\t" + tablename);
		}
		return created;
	}

	/**
	 * Actualiza la estructura de la tabla en la base de datos.
	 *
	 * @param dbBridge
	 *            Objeto Bridge para interactuar con la base de datos.
	 * @return true si la actualización fue exitosa; false en caso de error.
	 */
	public boolean update (DbBridge dbBridge)
	{
		// Obtener la definición actual de la tabla desde la base de datos.
		TableDbDefinition dbDef = new TableDbDefinition ();
		dbBridge.sendTableDescription (this.getTableName (), this.getSchemaName (), dbDef);

		boolean keepingPrimaryKey = false;
		String tablename = dbBridge.getTableName (this.getTableName (), this.getSchemaName ());
		StringBuilder sql = new StringBuilder ();

		// Revisar la clave primaria existente
		if (!dbDef.getPrimary ().getFields ().isEmpty ())
		{
			if (!this.getPrimary ().getFields ().equals (dbDef.getPrimary ().getFields ()))
			{
				sql.append ("ALTER TABLE ").append (tablename).append (" DROP PRIMARY KEY; ");
			}
			else
			{
				keepingPrimaryKey = true;
			}
		}

		// Comparar todos los índices
		// Se asume que dbDef dispone de un mapa (idxsMap) de índices por nombre.
		Map <String, Index> dbIdxsMap = dbDef.getIdxsMap ();
		for (Index idx : this.getIdxs ())
		{
			Index dbIdx = dbIdxsMap.get (idx.getName ());
			if (dbIdx != null)
			{
				if (dbIdx.getFields ().equals (idx.getFields ()))
				{
					dbIdx.setFound (true);
					idx.setFound (true);
				}
			}
		}

		// Eliminar índices obsoletos
		for (Index idx : dbDef.getIdxs ())
		{
			if (!idx.isFound ())
			{
				sql.append ("ALTER TABLE ").append (tablename).append (" DROP INDEX ").append (idx.getName ())
				        .append ("; ");
			}
		}

		StringBuilder colChanges = new StringBuilder ();
		String colChangeSeparator = "";
		String lastFldName = "";

		// Comparar cada campo para determinar si se debe agregar, modificar o eliminar
		Map <String, Field> dbFldsMap = dbDef.getFieldsMap ();
		for (Field field : this.getFields ())
		{
			Field dbFld = dbFldsMap.get (field.name);
			if (dbFld == null)
			{
				// Campo nuevo
				colChanges.append (colChangeSeparator).append ("ADD COLUMN ").append (field.name)
				        .append (dbBridge.getColumnType (field));
				if (lastFldName.isEmpty ())
				{
					colChanges.append (" FIRST");
				}
				else
				{
					colChanges.append (" AFTER ").append (lastFldName);
				}
				colChangeSeparator = ", ";
			}
			else
			{
				dbFld.found = true;
				if (dbFld.position != field.position || !dbBridge.isCompatible (field, dbFld))
				{
					// Campo modificado
					colChanges.append (colChangeSeparator).append ("CHANGE COLUMN ").append (dbFld.name).append (" ")
					        .append (field.name).append (dbBridge.getColumnType (field));
					if (lastFldName.isEmpty ())
					{
						colChanges.append (" FIRST");
					}
					else
					{
						colChanges.append (" AFTER ").append (lastFldName);
					}
					colChangeSeparator = ", ";
				}
			}
			lastFldName = field.name;
		}
		// Eliminar campos obsoletos
		for (Field fld : dbDef.getFields ())
		{
			if (!fld.found)
			{
				colChanges.append (colChangeSeparator).append ("DROP COLUMN ").append (fld.name);
				colChangeSeparator = ", ";
			}
		}
		if (colChanges.length () > 0)
		{
			sql.append ("ALTER TABLE ").append (tablename).append (" ").append (colChanges.toString ()).append ("; ");
		}
		// Si se modificó la clave primaria
		if (!this.getPrimary ().getFields ().isEmpty () && !keepingPrimaryKey)
		{
			StringBuilder pk = new StringBuilder ();
			String sep = "";
			pk.append ("ALTER TABLE ").append (this.getTableName ()).append (" ADD PRIMARY KEY (");
			for (String idxField : this.getPrimary ().getFields ())
			{
				pk.append (sep).append (idxField);
				sep = ",";
			}
			pk.append ("); ");
			sql.append (pk.toString ());
		}
		// Crear índices que no se han marcado como existentes
		for (Index idx : this.getIdxs ())
		{
			if (idx.isFound ())
			{
				continue;
			}
			StringBuilder idxSql = new StringBuilder ("CREATE INDEX " + idx.getName ());
			idxSql.append (" ON " + tablename + " (");
			String sep = "";
			for (String idxField : idx.getFields ())
			{
				idxSql.append (sep).append (idxField);
				sep = ",";
			}
			idxSql.append ("); ");
			sql.append (idxSql.toString ());
		}

		// Ejecutar los comandos SQL si existen cambios
		if (sql.length () == 0)
		{
			logger.info ("KEEP_AS_IS:\t" + tablename);
			return true;
		}
		else
		{
			boolean retVal = dbBridge.executeBatch (sql.toString (), this.getSchemaName ());
			if (!retVal)
			{
				logger.error (tablename + " " + dbBridge.getErrorDescription ());
			}
			else
			{
				logger.info ("UPDATED:\t" + tablename);
			}
			return retVal;
		}
	}
}

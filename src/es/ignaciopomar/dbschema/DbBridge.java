
package es.ignaciopomar.dbschema;

import es.ignaciopomar.dbschema.types.DbErrorCode;
import es.ignaciopomar.dbschema.types.Field;


public interface DbBridge
{

	/**
	 * Ejecuta un lote de comandos SQL separados por ';' sobre un esquema dado.
	 *
	 * @param query
	 *            Consulta SQL completa que puede contener múltiples comandos separados por ';'
	 * @param schema
	 *            Esquema sobre el cual se ejecutan los comandos
	 * @return true si todos los comandos se ejecutaron correctamente, false en caso contrario
	 */
	default boolean executeBatch (String query, String schema)
	{
		boolean retVal = true;
		// Se separa la cadena por el delimitador ';'
		String[] commands = query.split (";");
		for (String sqlCommand : commands)
		{
			// Se eliminan espacios en blanco al inicio y al final
			sqlCommand = sqlCommand.trim ();
			if (!sqlCommand.isEmpty ())
			{
				retVal = execute (sqlCommand, schema);
				if (!retVal)
				{
					break;
				}
			}
		}
		return retVal;
	}

	boolean execute (String query, String schema);

	boolean checkIfTableExists (String tableName, String schema);

	String getTableName (String tableName, String schema);

	String getColumnType (Field field);

	void sendTableDescription (String tableName, String schema, SchemaObserver schemaObserver);

	boolean isCompatible (Field schemaFld, Field dbFld);

	void dropAllTablesFromSchema (String schema);

	DbErrorCode getLastErrCode ();

	String getErrorDescription ();

}

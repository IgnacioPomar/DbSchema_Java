
package es.ignaciopomar.dbschema;

import java.util.HashMap;
import java.util.Map;


/**
 * The schema can have defined variables, for example:
 * - if we work with multiple schemas, we can have a variable for the shema name.
 * - If the tables have a prefix, we can have a variable for the prefix.
 */
public class SchemaVariables
{
	// Almacena las variables del esquema
	public Map <String, String> schemaVars;

	// Constructor que inicializa el mapa
	public SchemaVariables ()
	{
		this.schemaVars = new HashMap <> ();
	}

	/**
	 * Obtiene el valor de la variable del esquema asociada a varName.
	 *
	 * @param varName
	 *            el nombre de la variable a buscar
	 * @return el valor de la variable o una cadena vacía si no existe
	 */
	public String getVarSchema (String varName)
	{
		return this.schemaVars.getOrDefault (varName, "");
	}

	public void put (String variableName, String schemaName)
	{
		this.schemaVars.put (variableName, schemaName);
	}
}

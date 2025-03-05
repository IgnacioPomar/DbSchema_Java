
package es.ignaciopomar.dbschema.types;

import java.util.HashMap;
import java.util.Map;


public enum FieldType
{
	INTEGER, REAL, DECIMAL, BOOL, STRING, TEXT, JSON, BINARY, BLOB, DATE, TIME, TIMESTAMP, INET6;

	private static final Map <String, FieldType> lookup = new HashMap <> ();

	// Bloque estático para inicializar el mapa
	static
	{
		for (FieldType type : FieldType.values ())
		{
			lookup.put (type.name (), type);
		}
	}

	/**
	 * Convierte una cadena a FieldType.
	 * Si la cadena es null o no coincide, retorna STRING por defecto.
	 *
	 * @param str
	 *            La cadena a convertir.
	 * @return El FieldType correspondiente.
	 */
	public static FieldType fromString (String str)
	{
		if (str == null)
		{
			return STRING;
		}
		FieldType type = lookup.get (str.toUpperCase ());
		return (type != null)? type : STRING;
	}
}

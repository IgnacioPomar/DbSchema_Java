
package es.ignaciopomar.dbschema.types;

/**
 * Database error codes.
 */
public enum DbErrorCode
{

	DB_NO_ERROR ("No error"), INIT ("Error inicializando la base de datos"), CONNECT ("Error conecting"),
	QUERY ("Error al ejecutar la consulta"), PREPARE ("Error al preparar la consulta");

	private final String description;

	private DbErrorCode (String description)
	{
		this.description = description;
	}

	public String getDescription ()
	{
		return description;
	}

	@Override
	public String toString ()
	{
		return description;
	}
}

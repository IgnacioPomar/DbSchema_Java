
package es.ignaciopomar.dbschema;

import es.ignaciopomar.dbschema.types.FieldType;


public class Field
{

	public String	 name;
	public FieldType type;
	public int		 size;
	// public boolean allowNull;
	// public String defaultValue;
	public boolean	 isAutoIncrement;
	public int		 position;

	// Indica si el campo fue encontrado en la definición de la base de datos, inicializado en false.
	public boolean	 found = false;

}

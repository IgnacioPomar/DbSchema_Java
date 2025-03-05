
package es.ignaciopomar.dbschema.types;

import java.util.ArrayList;
import java.util.List;


/**
 * Representa un índice en una tabla (excepto el índice primario).
 */
public class Index
{
	public String		 name;
	// Los campos opcionales (como isUnique o isFullText) pueden agregarse si son necesarios.
	public List <String> fields	= new ArrayList <> ();
	// Indica si, para propósitos de actualización, el campo del índice también se encuentra en el esquema.
	public boolean		 found	= false;

}

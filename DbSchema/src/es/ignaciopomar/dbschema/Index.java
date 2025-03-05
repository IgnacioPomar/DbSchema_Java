
package es.ignaciopomar.dbschema;

import java.util.ArrayList;
import java.util.List;


/**
 * Representa un índice en una tabla (excepto el índice primario).
 */
public class Index
{
	String				  name;
	// Los campos opcionales (como isUnique o isFullText) pueden agregarse si son necesarios.
	private List <String> fields = new ArrayList <> ();
	// Indica si, para propósitos de actualización, el campo del índice también se encuentra en el esquema.
	private boolean		  found	 = false;

	public Index ()
	{
	}

	public String getName ()
	{
		return name;
	}

	public void setName (String name)
	{
		this.name = name;
	}

	public List <String> getFields ()
	{
		return fields;
	}

	public void setFields (List <String> fields)
	{
		this.fields = fields;
	}

	public boolean isFound ()
	{
		return found;
	}

	public void setFound (boolean found)
	{
		this.found = found;
	}
}

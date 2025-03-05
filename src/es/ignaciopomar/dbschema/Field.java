
package es.ignaciopomar.dbschema;

import es.ignaciopomar.dbschema.types.FieldType;


public class Field
{

	private String	  name;
	private FieldType type;
	private int		  size;
	// private boolean allowNull; // Comentado, igual que en el original
	// private String defaultValue; // Comentado, igual que en el original
	private boolean	  isAutoIncrement;
	private int		  position;
	// Indica si el campo fue encontrado en la definición de la base de datos, inicializado en false.
	private boolean	  found	= false;

	// Constructor por defecto
	public Field ()
	{
		// El valor de 'found' ya se inicializa en false.
	}

	// Getters y setters

	public String getName ()
	{
		return name;
	}

	public void setName (String name)
	{
		this.name = name;
	}

	public FieldType getType ()
	{
		return type;
	}

	public void setType (FieldType type)
	{
		this.type = type;
	}

	public int getSize ()
	{
		return size;
	}

	public void setSize (int size)
	{
		this.size = size;
	}

	public boolean isAutoIncrement ()
	{
		return isAutoIncrement;
	}

	public void setAutoIncrement (boolean isAutoIncrement)
	{
		this.isAutoIncrement = isAutoIncrement;
	}

	public int getPosition ()
	{
		return position;
	}

	public void setPosition (int position)
	{
		this.position = position;
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

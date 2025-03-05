
package es.ignaciopomar.dbschema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import es.ignaciopomar.dbschema.types.Field;
import es.ignaciopomar.dbschema.types.Index;


/**
 * Mantiene la definición de la tabla en la base de datos.
 * Extiende TableStructure e implementa SchemaObserver.
 */
public class TableDbDefinition extends TableStructure implements SchemaObserver
{

	// Conjunto para evitar duplicados de nombres de índices.
	private Set <String>	   idxsSet		 = new HashSet <> ();

	// Contador para asignar posición a cada campo.
	public int				   fieldPosition = 0;

	// Mapas finales para acceso rápido a campos e índices.
	public Map <String, Field> fieldsMap	 = new HashMap <> ();
	public Map <String, Index> idxsMap		 = new HashMap <> ();

	// Listas internas para almacenar campos e índices durante la construcción.
	private List <Field>	   fields		 = new ArrayList <> ();
	private List <Index>	   indexes		 = new ArrayList <> ();

	// Índice primario. Se asume que Index tiene un atributo "fields" (lista de nombres de campo).
	private Index			   primary		 = new Index ();

	/**
	 * Agrega un campo al índice primario.
	 * 
	 * @param fldName
	 *            nombre del campo a agregar al índice primario.
	 */
	public void addPrimaryIdxField (String fldName)
	{
		primary.getFields ().add (fldName);
	}

	/**
	 * Agrega un campo a un índice específico.
	 * 
	 * Si el índice no existe, se crea y se añade a la lista de índices.
	 * Si ya existe, se agrega el campo a la lista correspondiente.
	 *
	 * @param idxName
	 *            nombre del índice.
	 * @param fldName
	 *            nombre del campo a agregar.
	 */
	public void addIdxField (String idxName, String fldName)
	{
		if (!idxsSet.contains (idxName))
		{
			Index idx = new Index ();
			idx.name = idxName;
			idx.getFields ().add (fldName);
			indexes.add (idx);
			idxsSet.add (idxName);
		}
		else
		{
			for (Index idx : indexes)
			{
				if (idx.name.equals (idxName))
				{
					idx.getFields ().add (fldName);
					break;
				}
			}
		}
	}

	/**
	 * Agrega un campo a la definición de la tabla.
	 *
	 * Asigna la posición al campo y lo añade a la lista interna.
	 *
	 * @param field
	 *            objeto Field a agregar.
	 */
	public void addField (Field field)
	{
		field.setPosition (fieldPosition);
		fieldPosition++;
		fields.add (field);
		// La asignación en fieldsMap se realiza en el método finish().
	}

	/**
	 * Finaliza la construcción de la definición de la tabla.
	 *
	 * Recorre las listas internas de índices y campos para asignarlos a los mapas correspondientes.
	 */
	public void finish ()
	{
		for (Index idx : indexes)
		{
			idxsMap.put (idx.name, idx);
		}
		for (Field fld : fields)
		{
			fieldsMap.put (fld.getName (), fld);
		}
	}

	/**
	 * Retorna el índice primario.
	 * 
	 * @return el índice primario.
	 */
	public Index getPrimaryIndex ()
	{
		return primary;
	}

}

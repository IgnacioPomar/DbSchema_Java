
package es.ignaciopomar.dbschema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.ignaciopomar.dbschema.types.Field;
import es.ignaciopomar.dbschema.types.Index;


/**
 * Almacena la estructura de una tabla, incluyendo sus campos, índices y el índice primario.
 */
public class TableStructure
{
	private String		 tableName;
	private String		 schemaName;
	private List <Field> fields	 = new ArrayList <> ();
	private List <Index> indexes = new ArrayList <> ();
	private Index		 primary = new Index ();

	public TableStructure ()
	{
	}

	public String getTableName ()
	{
		return tableName;
	}

	public void setTableName (String tableName)
	{
		this.tableName = tableName;
	}

	public String getSchemaName ()
	{
		return schemaName;
	}

	public void setSchemaName (String schemaName)
	{
		this.schemaName = schemaName;
	}

	public List <Field> getFields ()
	{
		return fields;
	}

	public void setFields (List <Field> fields)
	{
		this.fields = fields;
	}

	public List <Index> getIdxs ()
	{
		return indexes;
	}

	public void setIndexes (List <Index> indexes)
	{
		this.indexes = indexes;
	}

	public Index getPrimary ()
	{
		return primary;
	}

	public void setPrimary (Index primary)
	{
		this.primary = primary;
	}

	public Map <String, Index> getIdxsMap ()
	{
		Map <String, Index> idxsMap = new HashMap <> ();
		for (Index idx : indexes)
		{
			idxsMap.put (idx.name, idx);
		}

		return idxsMap;

	}

	public Map <String, Field> getFieldsMap ()
	{
		Map <String, Field> fieldsMap = new HashMap <> ();
		for (Field fld : fields)
		{
			fieldsMap.put (fld.name, fld);
		}

		return fieldsMap;
	}

}

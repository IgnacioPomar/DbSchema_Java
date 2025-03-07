
package es.ignaciopomar.dbschema;

import es.ignaciopomar.dbschema.types.Field;

public interface SchemaObserver
{
	void addPrimaryIdxField (String fldName);

	void addIdxField (String idxName, String fldName);

	void addField (Field field);

	void finish ();
}

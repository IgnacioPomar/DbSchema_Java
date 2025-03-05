
package es.ignaciopomar.dbschema;

public interface SchemaObserver
{
	void addPrimaryIdxField (String fldName);

	void addIdxField (String idxName, String fldName);

	void addField (Field field);

	void finish ();
}

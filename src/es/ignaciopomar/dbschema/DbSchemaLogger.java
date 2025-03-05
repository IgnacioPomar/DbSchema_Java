
package es.ignaciopomar.dbschema;

public interface DbSchemaLogger
{
	void info (String message);

	void warn (String message);

	void error (String message);
}

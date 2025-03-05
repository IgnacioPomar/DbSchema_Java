
package es.ignaciopomar.dbschema;

import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;


class TableCreation
{

	@Test
	void test ()
	{
		DbSchema dbSchema = new DbSchema ();

		dbSchema.addDbBridge (null);

		dbSchema.addVarSchema ("clientDbname", "pruebasOther");

		// ------------------- RESET DATA: START -------------------
		dbSchema.dropAllTablesFromSchemaForUnitTest ("pruebas");
		dbSchema.dropAllTablesFromSchemaForUnitTest ("pruebasOther");
		// ------------------- RESET DATA: END ---------------------

		Path schemaPath_v1 = Path.of ("../test/data/v1/");
		// Create the tables
		dbSchema.createOrUpdate (schemaPath_v1);
		UNIT_CHECK (logger.numCreated == 6);
		UNIT_CHECK (logger.numErrors == 0);
		UNIT_CHECK (logger.numUpdated == 0);
		UNIT_CHECK (logger.numIgnored == 0);

		// Update
		// logger.reset();
		Path schemaPath_v2 = Path.of ("../test/data/v2/");

		dbSchema.createOrUpdate (schemaPath_v2);
		UNIT_CHECK (logger.numErrors == 0);
		UNIT_CHECK (logger.numCreated == 1);
		UNIT_CHECK (logger.numUpdated == 2);
		UNIT_CHECK (logger.numIgnored == 4);

		fail ("Not yet implemented");
	}

}

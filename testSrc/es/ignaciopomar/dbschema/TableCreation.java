
package es.ignaciopomar.dbschema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import es.ignaciopomar.dbschema.testerutils.FakeLogger;


class TableCreation
{

	@Test
	void test ()
	{
		FakeLogger logger = new FakeLogger ();

		DbSchema dbSchema = new DbSchema (logger);

		// dbSchema.addDbBridge (null);

		dbSchema.addVarSchema ("clientDbname", "pruebasOther");

		// ------------------- RESET DATA: START -------------------
		dbSchema.dropAllTablesFromSchemaForUnitTest ("pruebas");
		dbSchema.dropAllTablesFromSchemaForUnitTest ("pruebasOther");
		// ------------------- RESET DATA: END ---------------------

		Path schemaPath_v1 = Path.of ("../test/data/v1/");
		// Create the tables
		dbSchema.createOrUpdate (schemaPath_v1);

		assertEquals (logger.numErrors, 0);
		assertEquals (logger.numCreated, 6);
		assertEquals (logger.numUpdated, 0);
		assertEquals (logger.numIgnored, 0);

		// Update
		// logger.reset();
		Path schemaPath_v2 = Path.of ("../test/data/v2/");

		dbSchema.createOrUpdate (schemaPath_v2);
		assertEquals (logger.numErrors, 0);
		assertEquals (logger.numCreated, 1);
		assertEquals (logger.numUpdated, 2);
		assertEquals (logger.numIgnored, 4);

		fail ("Not yet implemented");
	}

}

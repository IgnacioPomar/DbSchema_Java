
package es.ignaciopomar.dbschema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import es.ignaciopomar.dbschema.adapters.DbBridgeMariadb;
import es.ignaciopomar.dbschema.testerutils.FakeLogger;
import es.ignaciopomar.dbschema.testerutils.TestCfg;


class TableCreation
{

	@Test
	void test ()
	{
		// Check the configuration
		TestCfg cfg = new TestCfg ();
		cfg.loadCfg ();

		if (!cfg.isConfigured ())
		{
			fail ("Configuration not found or incomplete");
			// This test requires a configuration file with mariadb connection data
			// In the database must exist two schemas
		}

		FakeLogger logger = new FakeLogger ();

		DbSchema dbSchema = new DbSchema (logger);

		dbSchema.addDbBridge (new DbBridgeMariadb (cfg.getSrv (), cfg.getPort (), cfg.getUser (), cfg.getPass (),
		        cfg.getDbname ()));

		dbSchema.addVarSchema ("clientDbname", cfg.getDbname2 ());

		// ------------------- RESET DATA: START -------------------
		dbSchema.dropAllTablesFromSchemaForUnitTest (null);
		dbSchema.dropAllTablesFromSchemaForUnitTest (cfg.getDbname2 ());
		// ------------------- RESET DATA: END ---------------------

		Path schemaPath_v1 = Path.of (cfg.getBasePath () + "testData/v1/");
		// Create the tables
		dbSchema.createOrUpdate (schemaPath_v1);

		assertEquals (logger.numErrors, 0);
		assertEquals (logger.numCreated, 6);
		assertEquals (logger.numUpdated, 0);
		assertEquals (logger.numIgnored, 0);

		// Update
		// logger.reset();
		Path schemaPath_v2 = Path.of (cfg.getBasePath () + "testData/v2/");

		dbSchema.createOrUpdate (schemaPath_v2);
		assertEquals (logger.numErrors, 0);
		assertEquals (logger.numCreated, 1);
		assertEquals (logger.numUpdated, 2);
		assertEquals (logger.numIgnored, 4);

	}

}

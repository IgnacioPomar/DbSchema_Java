
package es.ignaciopomar.dbschema.testerutils;

import es.ignaciopomar.dbschema.DbSchemaLogger;


public class FakeLogger implements DbSchemaLogger
{
	public enum LogLevel
	{
		INFO, WARN, ERROR, DEBUG
	}

	public int numErrors  = 0;
	public int numCreated = 0;
	public int numUpdated = 0;
	public int numIgnored = 0;

	private void onLogEvent (String message, LogLevel logLevel)
	{
		if (logLevel == LogLevel.ERROR)
		{
			numErrors++;
		}
		else if (logLevel == LogLevel.INFO)
		{
			if (message.startsWith ("CREATE"))
			{
				numCreated++;
			}
			else if (message.startsWith ("UPDATE"))
			{
				numUpdated++;
			}
			else if (message.startsWith ("KEEP"))
			{
				numIgnored++;
			}

		}
		else if (logLevel == LogLevel.DEBUG)
		{
			// numIgnored++;
		}
		System.out.println (message);

	}

	public void reset ()
	{
		numErrors = 0;
		numCreated = 0;
		numUpdated = 0;
		numIgnored = 0;
	}

	@Override
	public void info (String message)
	{
		onLogEvent (message, LogLevel.INFO);
	}

	@Override
	public void warn (String message)
	{
		onLogEvent (message, LogLevel.WARN);

	}

	@Override
	public void error (String message)
	{
		onLogEvent (message, LogLevel.ERROR);
		numErrors++;
	}

	@Override
	public void debug (String message)
	{
		onLogEvent (message, LogLevel.DEBUG);

	}

}

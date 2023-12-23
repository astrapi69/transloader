package com.googlecode.transloader.test;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import com.googlecode.transloader.test.fixture.IndependentClassLoader;

import junit.framework.TestCase;

public abstract class BaseTestCase extends TestCase
{

	protected void dump(Object object)
	{
		// System.err.println("<" + getName() + "> " + object.toString());
	}

	protected void assertEqualExceptForClassLoader(String originalDumpString, Object clone)
	{
		String originalClassLoaderString = this.getClass().getClassLoader().toString();
		String cloneClassLoaderString = IndependentClassLoader.INSTANCE.toString();
		assertNotEquals(originalClassLoaderString, cloneClassLoaderString);
		String expectedCloneString = StringUtils.replace(originalDumpString,
			originalClassLoaderString, cloneClassLoaderString);
		String cloneString = clone.toString();
		dump(originalDumpString);
		dump(cloneString);
		assertEquals(expectedCloneString, cloneString);
	}

	protected static void assertNotEquals(Object notExpected, Object actual)
	{
		if (notExpected == null)
			assertNotNull(actual);
		else
			assertFalse(
				"Expected: not '" + notExpected + "'. Actual: '" + actual + "' which is equal.",
				notExpected.equals(actual));
	}

	protected static void assertMatches(Throwable expected, Throwable actual)
	{
		assertTrue(getComparisonFailedMessage("type", expected.getClass().getName(), actual),
			expected.getClass().isAssignableFrom(actual.getClass()));
		assertTrue(getComparisonFailedMessage("message", expected.getMessage(), actual),
			expected.getMessage() == null || actual.getMessage().startsWith(expected.getMessage()));
		Throwable expectedCause = ExceptionUtils.getCause(expected);
		if (expectedCause != null && expectedCause != expected)
		{
			assertMatches(expectedCause, ExceptionUtils.getCause(actual));
		}
	}

	private static String getComparisonFailedMessage(String attributeName, String expectedValue,
		Throwable actual)
	{
		return "Expected " + attributeName + ": '" + expectedValue + "'. Actual: '"
			+ ExceptionUtils.getFullStackTrace(actual) + "'.";
	}

	protected static void assertThrows(Throwable expected, Thrower thrower)
	{
		try
		{
			thrower.executeUntilThrow();
		}
		catch (Throwable actual)
		{
			assertMatches(expected, actual);
			return;
		}
		fail("Expected: '" + expected + "'. Actual: nothing thrown.");
	}

	protected static interface Thrower
	{
		void executeUntilThrow() throws Throwable;
	}
}
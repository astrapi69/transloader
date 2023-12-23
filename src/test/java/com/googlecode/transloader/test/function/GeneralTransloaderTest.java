package com.googlecode.transloader.test.function;

import com.googlecode.transloader.ClassWrapper;
import com.googlecode.transloader.InvocationDescription;
import com.googlecode.transloader.ObjectWrapper;
import com.googlecode.transloader.Transloader;
import com.googlecode.transloader.clone.CloningStrategy;
import com.googlecode.transloader.except.TransloaderException;
import com.googlecode.transloader.test.BaseTestCase;
import com.googlecode.transloader.test.Triangulate;
import com.googlecode.transloader.test.fixture.IndependentClassLoader;
import com.googlecode.transloader.test.fixture.NotCommonJavaObject;
import com.googlecode.transloader.test.fixture.NotCommonJavaType;
import com.googlecode.transloader.test.fixture.NotCommonJavaTypeWithMethods;
import com.googlecode.transloader.test.fixture.fields.WithMapFields;
import com.googlecode.transloader.test.fixture.fields.WithMethods;
import com.googlecode.transloader.test.fixture.fields.WithPrimitiveFields;
import com.googlecode.transloader.test.fixture.fields.WithStringField;

import junit.extensions.ActiveTestSuite;
import junit.framework.Test;

public class GeneralTransloaderTest extends BaseTestCase
{
	Object foreignObject;
	Object foreignObjectWithMethods;
	Transloader transloader = Transloader.DEFAULT;
	ClassLoader dummyClassLoader = (ClassLoader)Triangulate.anyInstanceOf(ClassLoader.class);

	public static Test suite() throws Exception
	{
		return new ActiveTestSuite(GeneralTransloaderTest.class);
	}

	protected void setUp() throws Exception
	{
		foreignObject = getNewInstanceFromOtherClassLoader(WithMapFields.class);
		foreignObjectWithMethods = getNewInstanceFromOtherClassLoader(WithMethods.class);
	}

	private static Object getNewInstanceFromOtherClassLoader(Class clazz) throws Exception
	{
		return IndependentClassLoader.INSTANCE.loadClass(clazz.getName()).newInstance();
	}

	public void testReportsIsNullWhenGivenNull() throws Exception
	{
		assertTrue(transloader.wrap(null).isNull());
	}

	public void testReportsIsNotNullWhenGivenNonNullObject() throws Exception
	{
		assertFalse(transloader.wrap(new Object()).isNull());
	}

	public void testReportsIsNotInstanceOfUnrelatedType() throws Exception
	{
		assertFalse(transloader.wrap(new Object()).isInstanceOf(NotCommonJavaType.class.getName()));
	}

	public void testReportsIsInstanceOfSameClass() throws Exception
	{
		assertTrue(
			transloader.wrap(foreignObject).isInstanceOf(foreignObject.getClass().getName()));
	}

	public void testReportsIsInstanceOfSuperClass() throws Exception
	{
		assertTrue(
			transloader.wrap(foreignObject).isInstanceOf(NotCommonJavaObject.class.getName()));
	}

	public void testReportsIsInstanceOfImplementedInterface() throws Exception
	{
		assertTrue(transloader.wrap(foreignObject).isInstanceOf(NotCommonJavaType.class.getName()));
	}

	public void testReturnsNullWhenAskedToCloneNull() throws Exception
	{
		assertNull(transloader.wrap((Object)null).cloneWith(dummyClassLoader));
	}

	public void testReturnsCloneReturnedFromGivenCloningStrategy() throws Exception
	{
		final Object expectedOriginal = new Object();
		final ClassLoader expectedClassloader = new ClassLoader()
		{
		};
		final Object expectedClone = new Object();
		CloningStrategy cloningStrategy = new CloningStrategy()
		{
			public Object cloneObjectUsing(ClassLoader cloneClassLoader, Object original)
				throws Exception
			{
				assertSame(expectedOriginal, original);
				assertSame(expectedClassloader, cloneClassLoader);
				return expectedClone;
			}
		};
		assertSame(expectedClone, new ObjectWrapper(expectedOriginal, cloningStrategy,
			ClassWrapper.getClassLoaderFrom(expectedOriginal)).cloneWith(expectedClassloader));
	}

	public void testWrapsExceptionThrownByGivenCloningStrategy() throws Exception
	{
		final Object expectedOriginal = new Object();
		final Exception expectedException = new Exception(Triangulate.anyString());
		final CloningStrategy throwingCloningStrategy = new CloningStrategy()
		{
			public Object cloneObjectUsing(ClassLoader cloneClassLoader, Object original)
				throws Exception
			{
				throw expectedException;
			}
		};
		Thrower thrower = new Thrower()
		{
			public void executeUntilThrow() throws Throwable
			{
				new ObjectWrapper(expectedOriginal, throwingCloningStrategy,
					ClassWrapper.getClassLoaderFrom(expectedOriginal)).cloneWith(dummyClassLoader);
			}
		};
		assertThrows(new TransloaderException("Unable to clone '" + expectedOriginal + "'.",
			expectedException), thrower);
	}

	public void testProvidesWrappedObjectOnRequest() throws Exception
	{
		final Object expected = new Object();
		assertSame(expected, transloader.wrap(expected).getUnwrappedSelf());
	}

	public void testPassesAndReturnsStringsToAndFromInvocations() throws Exception
	{
		ObjectWrapper objectWrapper = transloader.wrap(foreignObjectWithMethods);
		String expectedStringFieldValue = Triangulate.anyString();
		objectWrapper.invoke(new InvocationDescription("setStringField", expectedStringFieldValue));
		assertEquals(expectedStringFieldValue,
			objectWrapper.invoke(new InvocationDescription("getStringField")));
	}

	public void testClonesParametersOfNonCommonJavaTypesInInvocations() throws Exception
	{
		NotCommonJavaType first = new WithStringField(Triangulate.anyString());
		NotCommonJavaType second = new WithPrimitiveFields();
		String expected = new WithMethods().concatenate(first, second);
		Class[] paramTypes = { NotCommonJavaType.class, NotCommonJavaType.class };
		Object[] params = { first, second };
		String actual = (String)transloader.wrap(foreignObjectWithMethods)
			.invoke(new InvocationDescription("concatenate", paramTypes, params));
		assertEqualExceptForClassLoader(expected, actual);
	}

	public void testCreatesAnImplementationOfAGivenInterfaceThatCallsThroughToTheWrappedObject()
		throws Exception
	{
		String expectedStringFieldValue = Triangulate.anyString();
		Transloader.DEFAULT.wrap(foreignObjectWithMethods)
			.invoke(new InvocationDescription("setStringField", expectedStringFieldValue));
		NotCommonJavaTypeWithMethods withMethods = (NotCommonJavaTypeWithMethods)transloader
			.wrap(foreignObjectWithMethods).makeCastableTo(NotCommonJavaTypeWithMethods.class);
		assertEquals(expectedStringFieldValue, withMethods.getStringField());
	}
}

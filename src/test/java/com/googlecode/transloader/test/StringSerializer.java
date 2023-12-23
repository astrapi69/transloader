package com.googlecode.transloader.test;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;

import com.googlecode.transloader.reference.DefaultReflecter;
import com.googlecode.transloader.reference.Reference;
import com.googlecode.transloader.reference.ReferenceReflecter;
import com.googlecode.transloader.test.CyclicReferenceSafeTraverser.Operation;

public class StringSerializer
{
	private static final String FIELD_SEPERATOR = " ";
	private static final String OPEN_BRACKET = "[" + FIELD_SEPERATOR;
	private static final String CLOSE_BRACKET = "]";
	private static final CyclicReferenceSafeTraverser CYCLIC_REFERENCE_TRAVERSER = new CyclicReferenceSafeTraverser();
	public static final ReferenceReflecter REFLECTER = new DefaultReflecter();

	public static String toString(Object object)
	{
		StringBuffer buffer = new StringBuffer();
		append(object, buffer);
		return buffer.toString();
	}

	private static void append(Object object, final StringBuffer buffer)
	{
		Operation toStringOperation = new Operation()
		{
			public Object performOn(Object currentObject, Map referenceHistory) throws Exception
			{
				referenceHistory.put(currentObject,
					getName(currentObject.getClass()) + "<cyclic reference>");
				append(currentObject.getClass(), buffer);
				appendReferencesOf(currentObject, buffer);
				return buffer.toString();
			}
		};
		try
		{
			CYCLIC_REFERENCE_TRAVERSER.performOperationWithoutLoopingOn(object, toStringOperation);
		}
		catch (Exception e)
		{
			throw new NestableRuntimeException(e);
		}
	}

	private static String getName(Class aClass)
	{
		return ClassUtils.getShortClassName(aClass);
	}

	private static void appendReferencesOf(Object object, StringBuffer buffer)
		throws IllegalAccessException
	{
		buffer.append(OPEN_BRACKET);
		Reference[] references = REFLECTER.reflectReferencesFrom(object);
		for (int i = 0; i < references.length; i++)
		{
			buffer.append(references[i].getDescription().getName()).append("=");
			append(references[i], buffer);
			buffer.append(FIELD_SEPERATOR);
		}
		buffer.append(CLOSE_BRACKET);
	}

	private static void append(Reference reference, StringBuffer buffer)
	{
		if (reference.getValue() == null)
			buffer.append("null");
		else if (reference.getDescription().isTransient())
			buffer.append("<transient>");
		else if (reference.getDescription().isOfPrimitiveType()
			|| referenceBasedStringIsNotDeterministic(reference.getValue()))
			buffer.append(reference.getValue());
		else
			append(reference.getValue(), buffer);
	}

	private static void append(Class clazz, StringBuffer buffer)
	{
		buffer.append(getName(clazz)).append('(').append(clazz.getClassLoader()).append(')');
	}

	private static boolean referenceBasedStringIsNotDeterministic(Object fieldValue)
	{
		return fieldValue instanceof String || fieldValue instanceof Map
			|| fieldValue instanceof Set;
	}
}

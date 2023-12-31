package com.googlecode.transloader.reference.field;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.ObjectStreamField;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.googlecode.transloader.InvocationDescription;
import com.googlecode.transloader.except.Assert;
import com.googlecode.transloader.primitive.WrapperConverter;

/**
 * @author jeremywales
 */
public final class SerializationSetter implements FieldSetter
{
	private static final Class[] FIELD_IDS_PARAMTYPES = { ObjectStreamField[].class, long[].class,
			long[].class };
	private static final Class[] PRIMITIVE_FIELDS_PARAMTYPES = { Object.class, long[].class,
			char[].class, byte[].class };
	private static final Class[] OBJECT_FIELD_PARAMTYPES = { Object.class, long.class, Class.class,
			Object.class };
	private static final long[] IRRELEVANT = new long[0];
	private final WrapperConverter converter = new WrapperConverter();
	private final Method getFieldIds;
	private final Method setPrimitiveFields;
	private final Method setObjectField;
	private final Field fieldField;

	public SerializationSetter() throws NoSuchMethodException, NoSuchFieldException
	{
		getFieldIds = InvocationDescription.getMethod("getFieldIDs", FIELD_IDS_PARAMTYPES,
			ObjectStreamClass.class);
		setPrimitiveFields = InvocationDescription.getMethod("setPrimitiveFieldValues",
			PRIMITIVE_FIELDS_PARAMTYPES, ObjectInputStream.class);
		setObjectField = InvocationDescription.getMethod("setObjectFieldValue",
			OBJECT_FIELD_PARAMTYPES, ObjectInputStream.class);
		fieldField = getField("field", ObjectStreamField.class);
	}

	public void set(Object value, Field field, Object referer)
		throws IllegalAccessException, InvocationTargetException, IOException, NoSuchMethodException
	{
		Assert.areNotNull(value, field, referer);

		boolean primitive = field.getType().isPrimitive();
		ObjectStreamField streamField = getStreamField(field);
		long[] single = new long[1];

		if (primitive)
		{
			fillIdOf(streamField, single, IRRELEVANT);
			setPrimitiveInto(streamField, single, referer, value);
		}
		else
		{
			fillIdOf(streamField, IRRELEVANT, single);
			setObjectInto(field, single, referer, value);
		}
	}

	private ObjectStreamField getStreamField(Field field) throws IllegalAccessException
	{
		ObjectStreamField streamField = new ObjectStreamField(field.getName(), field.getType());
		fieldField.set(streamField, field);
		return streamField;
	}

	private void fillIdOf(ObjectStreamField streamField, long[] primitiveId, long[] objectId)
		throws IllegalAccessException, InvocationTargetException
	{
		ObjectStreamField[] fields = { streamField };
		Object[] params = { fields, primitiveId, objectId };
		invoke(getFieldIds, params);
	}

	private void setPrimitiveInto(ObjectStreamField streamField, long[] primitiveIds,
		Object referer, Object value)
		throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException
	{
		byte[] bytes = converter.asBytes(value);
		char[] typeCodes = { streamField.getTypeCode() };
		Object[] params = { referer, primitiveIds, typeCodes, bytes };
		invoke(setPrimitiveFields, params);
	}

	private void setObjectInto(Field field, long[] objectIds, Object referer, Object value)
		throws IllegalAccessException, InvocationTargetException
	{
		Object[] params = { referer, destructure(objectIds), field.getType(), value };
		invoke(setObjectField, params);
	}

	private static Long destructure(long[] objectId)
	{

		return objectId[0];
	}

	private static Object invoke(Method method, Object[] params)
		throws IllegalAccessException, InvocationTargetException
	{
		return method.invoke(null, params);
	}

	private static Field getField(String name, Class owner) throws NoSuchFieldException
	{
		Field field = owner.getDeclaredField(name);
		field.setAccessible(true);
		return field;
	}
}

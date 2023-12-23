package com.googlecode.transloader.reference.element;

import java.lang.reflect.Array;

import com.googlecode.transloader.except.Assert;
import com.googlecode.transloader.reference.ReferenceDescription;

/**
 * Describes an array element by its index and whether or not it is of a primitive type.
 *
 * @author Jeremy Wales
 */
public final class ElementDescription implements ReferenceDescription
{
	private final int elementIndex;
	private final boolean primitive;

	/**
	 * Constructs an <code>ArrayElementDescription</code> with the given element index.
	 *
	 * @param elementIndex
	 *            the index within the array of element being described
	 * @param primitive
	 *            indicator of whether the reference is to a primitive rather than an {@link Object}
	 */
	public ElementDescription(int elementIndex, boolean primitive)
	{
		this.elementIndex = elementIndex;
		this.primitive = primitive;
	}

	/**
	 * Retrieves the value from the given array at the element index in <code>this</code>
	 * description.
	 *
	 * @param array
	 *            the array from which to retrieve the value
	 * @return the value at the relevant element index
	 */
	public Object getValueFrom(Object array)
	{
		return Array.get(Assert.isArray(array), elementIndex);
	}

	/**
	 * Assigns the given value to the field matching <code>this</code> description on the given
	 * object.
	 *
	 * @param array
	 *            the object on which to set the field
	 * @param value
	 *            the value to set (can be <code>null</code>)
	 */
	public void setValueIn(Object array, Object value)
	{
		Assert.areNotNull(array, value);
		Array.set(Assert.isArray(array), elementIndex, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isOfPrimitiveType()
	{
		return primitive;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isTransient()
	{
		return false;
	}

	/**
	 * Retreives the element index.
	 *
	 * @return the index within the array of the element reference.
	 */
	public String getName()
	{
		return elementIndex + "";
	}
}
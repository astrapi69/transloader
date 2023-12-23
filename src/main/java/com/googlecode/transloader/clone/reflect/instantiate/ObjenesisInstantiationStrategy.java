package com.googlecode.transloader.clone.reflect.instantiate;

import java.io.Serializable;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisSerializer;
import org.objenesis.ObjenesisStd;

import com.googlecode.transloader.except.Assert;

/**
 * Uses {@link Objenesis} to create new instances of <code>Class</code>es without invoking their
 * constructors.
 *
 * @author Jeremy Wales
 */
public final class ObjenesisInstantiationStrategy implements InstantiationStrategy
{
	private final Objenesis standard = new ObjenesisStd();
	private final Objenesis serializer = new ObjenesisSerializer();

	/**
	 * {@inheritDoc}
	 */
	public Object newInstanceOf(Class type) throws Exception
	{
		Assert.isNotNull(type);
		Objenesis objenesis = Serializable.class.isAssignableFrom(type) ? serializer : standard;
		return objenesis.newInstance(type);
	}
}

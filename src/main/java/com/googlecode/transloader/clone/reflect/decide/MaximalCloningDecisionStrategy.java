package com.googlecode.transloader.clone.reflect.decide;

import com.googlecode.transloader.except.Assert;

/**
 * When injected into a {@link com.googlecode.transloader.clone.reflect.ReflectionCloningStrategy},
 * decides that all given objects and those that they reference should be cloned.
 *
 * @author Jeremy Wales
 */
public final class MaximalCloningDecisionStrategy implements CloningDecisionStrategy
{
	/**
	 * Decides that all objects should be shallow copied.
	 *
	 * @param original
	 *            ignored; returns <code>true</code> regardless
	 * @param targetClassLoader
	 *            ignored; returns <code>true</code> regardless
	 * @return <code>true</code> always
	 */
	public boolean shouldCloneObjectItself(Object original, ClassLoader targetClassLoader)
	{
		Assert.areNotNull(original, targetClassLoader);
		return true;
	}

	/**
	 * Decides that all objects have their references copied.
	 *
	 * @param original
	 *            ignored; returns <code>true</code> regardless
	 * @param targetClassLoader
	 *            ignored; returns <code>true</code> regardless
	 * @return <code>true</code> always
	 */
	public boolean shouldCloneObjectReferences(Object original, ClassLoader targetClassLoader)
	{
		return shouldCloneObjectItself(original, targetClassLoader);
	}
}

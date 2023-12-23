package com.googlecode.transloader.clone.reflect.decide;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import com.googlecode.transloader.ClassWrapper;
import com.googlecode.transloader.except.Assert;
import com.googlecode.transloader.primitive.Wrapper;

/**
 * When injected into a {@link com.googlecode.transloader.clone.reflect.ReflectionCloningStrategy},
 * decides that only those objects whose <code>Class</code>es would be different if loaded through
 * the other <code>ClassLoader</code> should be cloned.
 *
 * @author Jeremy Wales
 */
public final class MinimalCloningDecisionStrategy implements CloningDecisionStrategy
{
	/**
	 * The list of primitive wrapper <code>Class</code>es.
	 */
	public static final List PRIMITIVE_WRAPPERS = Arrays.asList(Wrapper.LIST);

	// TODO test BigInteger and BigDecimal
	// TODO make the list of non-cloned Classes injectable
	private static final List KNOWN_SHARED_IMMUTABLES = Arrays
		.asList(new Class[] { String.class, BigInteger.class, BigDecimal.class });

	/**
	 * Decides that the given object should be shallow copied if its <code>Class</code> would be
	 * different when loaded through the given <code>ClassLoader</code>.
	 *
	 * @param original
	 *            the candidate for cloning
	 * @param targetClassLoader
	 *            the <code>ClassLoader</code> it may be cloned with
	 * @return <code>true</code> if <code>original</code>'s <code>Class</code> would be different
	 *         when loaded through <code>targetClassLoader</code>
	 */
	public boolean shouldCloneObjectItself(Object original, ClassLoader targetClassLoader)
	{
		Assert.areNotNull(original, targetClassLoader);
		return !isSameInClassLoader(original.getClass(), targetClassLoader);
	}

	private boolean isSameInClassLoader(Class originalClass, ClassLoader targetClassLoader)
	{
		return originalClass
			.equals(ClassWrapper.getClassFrom(targetClassLoader, originalClass.getName()));
	}

	/**
	 * Decides to not even consider cloning the objects referenced by the given object if it is
	 * known to an immutable object shared of a type shared among all <code>ClassLoader</code>s e.g.
	 * primitive wrappers and <code>String</code>s.
	 *
	 * @param original
	 *            the candidate for cloning
	 * @param targetClassLoader
	 *            the <code>ClassLoader</code> it may be cloned with; ignored in this implementation
	 * @return <code>true</code> unless <code>original</code>'s <code>Class</code> makes it a known
	 *         immutable of type shared among all <code>ClassLoader</code>s
	 */
	public boolean shouldCloneObjectReferences(Object original, ClassLoader targetClassLoader)
	{
		Assert.areNotNull(original, targetClassLoader);
		return !isEffectivelyPrimitive(original.getClass());
	}

	private boolean isEffectivelyPrimitive(Class originalClass)
	{
		return PRIMITIVE_WRAPPERS.contains(originalClass)
			|| KNOWN_SHARED_IMMUTABLES.contains(originalClass);
	}
}

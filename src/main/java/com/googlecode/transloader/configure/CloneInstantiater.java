package com.googlecode.transloader.configure;

import com.googlecode.transloader.clone.reflect.instantiate.DefaultInstantiater;
import com.googlecode.transloader.clone.reflect.instantiate.ObjenesisInstantiationStrategy;

/**
 * @author jeremywales
 */
public final class CloneInstantiater
{
	public static final com.googlecode.transloader.clone.reflect.instantiate.CloneInstantiater DEFAULT = new DefaultInstantiater(
		new ObjenesisInstantiationStrategy());

	private CloneInstantiater()
	{
	}
}

/*
 * Copyright 2012 Daniel Bechler
 *
 * This file is part of java-object-diff.
 *
 * java-object-diff is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * java-object-diff is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with java-object-diff.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.danielbechler.diff;

import de.danielbechler.diff.accessor.*;
import de.danielbechler.diff.node.*;
import de.danielbechler.diff.path.*;
import de.danielbechler.util.*;
import de.danielbechler.util.Collections;

import java.util.*;

/** @author Daniel Bechler */
class Instances
{
	private final Accessor sourceAccessor;
	private final Object working;
	private final Object base;
	private final Object fresh;

	public static <T> Instances of(final Accessor sourceAccessor, final T working, final T base, final T fresh)
	{
		return new Instances(sourceAccessor, working, base, fresh);
	}

	public static <T> Instances of(final Accessor sourceAccessor, final T working, final T base)
	{
		final Object fresh = (working != null) ? Classes.freshInstanceOf(working.getClass()) : null;
		return new Instances(sourceAccessor, working, base, fresh);
	}

	public static <T> Instances of(final T working, final T base)
	{
		final Object fresh = (working != null) ? Classes.freshInstanceOf(working.getClass()) : null;
		return new Instances(new RootAccessor(), working, base, fresh);
	}

	private Instances(final Accessor sourceAccessor,
					  final Object working,
					  final Object base,
					  final Object fresh)
	{
		Assert.notNull(sourceAccessor, "sourceAccessor");
		this.sourceAccessor = sourceAccessor;
		this.working = working;
		this.base = base;
		this.fresh = fresh;
	}

	/** @return The {@link Accessor} that has been used to get to these instances. */
	public Accessor getSourceAccessor()
	{
		return sourceAccessor;
	}

	public Instances access(final Accessor accessor)
	{
		Assert.notNull(accessor, "accessor");
		return new Instances(accessor, accessor.get(working), accessor.get(base), accessor.get(fresh));
	}

	public Object getWorking()
	{
		return working;
	}

	public <T> T getWorking(final Class<T> type)
	{
		return working != null ? type.cast(working) : null;
	}

	public Object getBase()
	{
		return base;
	}

	public <T> T getBase(final Class<T> type)
	{
		return base != null ? type.cast(base) : null;
	}

	public Object getFresh()
	{
		return fresh;
	}

	public <T> T getFresh(final Class<T> type)
	{
		return fresh != null ? type.cast(fresh) : null;
	}

	public boolean hasBeenAdded()
	{
		if (working != null && base == null)
		{
			return true;
		}
		if (Objects.isEqual(fresh, base) && !Objects.isEqual(base, working))
		{
			return true;
		}
		return false;
	}

	public boolean hasBeenRemoved()
	{
		if (base != null && working == null)
		{
			return true;
		}
		if (Objects.isEqual(fresh, working) && !Objects.isEqual(base, working))
		{
			return true;
		}
		return false;
	}

	public boolean areEqual()
	{
		return Objects.isEqual(base, working);
	}

	public boolean areSame()
	{
		return working == base;
	}

	public Class<?> getType()
	{
		if (sourceAccessor instanceof TypeAwareAccessor)
		{
			return ((TypeAwareAccessor) sourceAccessor).getPropertyType();
		}
		final Set<Class<?>> types = Classes.typesOf(working, base, fresh);
		if (types.isEmpty())
		{
			return null;
		}
		if (types.size() == 1)
		{
			return Collections.firstElementOf(types);
		}
		throw new IllegalStateException("Detected instances of different types " + types + ". " +
												"Instances must either be null or have the exact same type.");
		// NOTE It would be nice to be able to define a least common denominator like Map or Collection to allow mixed types
	}

	public PropertyPath getPropertyPath(final Node parentNode)
	{
		return new PropertyPathBuilder()
				.withPropertyPath(parentNode != null ? parentNode.getPropertyPath() : null)
				.withElement(sourceAccessor.getPathElement())
				.build();
	}
}
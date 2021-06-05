package dev.nokee.runtime.core;

import com.google.common.collect.ImmutableSet;
import dev.nokee.internal.testing.Assumptions;

import static com.google.common.collect.ImmutableSet.copyOf;

class CoordinateSetFactory_OfCoordinateSetFactoryMethodTest implements CoordinateSetFactoryTester {
	@Override
	public <T extends Enum<T>> CoordinateSet<T> createSubject(Class<T> type) {
		return Assumptions.skipCurrentTestExecution("Testing CoordinateSet.of(Set<Coordinate<T>>)");
	}

	@Override
	public <T> CoordinateSet<T> createSubject(Coordinate<T>... coordinates) {
		return CoordinateSet.of(copyOf(coordinates));
	}

	@Override
	public <T> CoordinateSet<T> createSubject(CoordinateAxis<T> axis, T... values) {
		return Assumptions.skipCurrentTestExecution("Testing CoordinateSet.of(Set<Coordinate<T>>)");
	}
}

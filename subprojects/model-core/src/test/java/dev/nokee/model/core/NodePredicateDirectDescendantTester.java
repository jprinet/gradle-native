package dev.nokee.model.core;

import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.core.ModelNodeTestUtils.*;
import static dev.nokee.utils.FunctionalInterfaceMatchers.calledOnceWith;
import static dev.nokee.utils.FunctionalInterfaceMatchers.singleArgumentOf;
import static dev.nokee.utils.SpecTestUtils.mockSpec;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertAll;

public interface NodePredicateDirectDescendantTester extends NodePredicateTester {
	@Test
	default void doesNotMatchProjectionOfSelfNode() {
		val root = rootNode();
		val child = childNodeOf(root);
		assertThat(createSubject().scope(child).isSatisfiedBy(projectionOf(child)), is(false));
	}

	@Test
	default void matchesProjectionOfChildNode() {
		val root = rootNode();
		val child = childNodeOf(root);
		val directChild = childNodeOf(child);
		assertThat(createSubject().scope(child).isSatisfiedBy(projectionOf(directChild)), is(true));
	}

	@Test
	default void doesNotMatchProjectionOfGrandchildNode() {
		val root = rootNode();
		val child = childNodeOf(root);
		val directChild = childNodeOf(child);
		val grandChild = childNodeOf(directChild);
		assertThat(createSubject().scope(child).isSatisfiedBy(projectionOf(grandChild)), is(false));
	}


	@Test
	default void queriesSpecWhenProjectionIsInScope() {
		val spec = mockSpec();
		val ancestor = rootNode();
		val inScope = childNodeOf(ancestor);
		val child = childNodeOf(inScope);
		createSubject(specOf(String.class, spec)).scope(inScope).isSatisfiedBy(projectionOf(child));
		assertThat(spec, calledOnceWith(singleArgumentOf(isA(ModelProjection.class))));
	}

	@Test
	default void returnsSpecResultWhenProjectionIsInScope() {
		val inScope = rootNode();
		val child = childNodeOf(inScope);
		val spec = createSubject(specOf(String.class, mockSpec().thenReturn(true, false))).scope(inScope);
		assertAll(
			() -> assertThat(spec.isSatisfiedBy(projectionOf(child)), is(true)),
			() -> assertThat(spec.isSatisfiedBy(projectionOf(child)), is(false))
		);
	}
}
package dev.nokee.model.dsl;

import dev.nokee.model.core.ModelProjection;
import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.dsl.ModelNodeTestUtils.*;
import static dev.nokee.utils.FunctionalInterfaceMatchers.calledOnceWith;
import static dev.nokee.utils.FunctionalInterfaceMatchers.singleArgumentOf;
import static dev.nokee.utils.SpecTestUtils.mockSpec;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertAll;

public interface NodePredicateAllTester extends NodePredicateTester {
	@Test
	default void matchesProjectionOfSelfNode() {
		val root = rootNode();
		val inScope = childNodeOf(root);
		assertThat(createSubject().scope(inScope).test(projectionOf(inScope)), is(true));
	}

	@Test
	default void matchesProjectionOfDirectChildNode() {
		val root = rootNode();
		val inScope = childNodeOf(root);
		val directChild = childNodeOf(inScope);
		assertThat(createSubject().scope(inScope).test(projectionOf(directChild)), is(true));
	}

	@Test
	default void matchesProjectionOfGrandChildNode() {
		val root = rootNode();
		val inScope = childNodeOf(root);
		val directChild = childNodeOf(inScope);
		val grandchild = childNodeOf(directChild);
		assertThat(createSubject().scope(inScope).test(projectionOf(grandchild)), is(true));
	}

	@Test
	default void queriesSpecWhenProjectionIsInScope() {
		val spec = mockSpec();
		val ancestor = rootNode();
		val inScope = childNodeOf(ancestor);
		createSubject(specOf(String.class, spec)).scope(inScope).test(projectionOf(inScope));
		assertThat(spec, calledOnceWith(singleArgumentOf(isA(ModelProjection.class))));
	}

	@Test
	default void returnsSpecResultWhenProjectionIsInScope() {
		val inScope = rootNode();
		val spec = createSubject(specOf(String.class, mockSpec().thenReturn(true, false))).scope(inScope);
		assertAll(
			() -> assertThat(spec.test(projectionOf(inScope)), is(true)),
			() -> assertThat(spec.test(projectionOf(inScope)), is(false))
		);
	}
}
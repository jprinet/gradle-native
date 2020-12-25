package dev.nokee.model.internal.core;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.nokee.model.internal.core.ModelNodes.descendantOf;
import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.core.ModelPath.root;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static dev.nokee.model.internal.core.ModelTestUtils.rootNode;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Subject(ModelNodes.class)
class ModelNodes_DescendantOfTest {
	@Test
	void canDetectDirectDescendantNode() {
		assertTrue(descendantOf(path("a")).test(node("a.b")), "direct descendant should be a descendant of the current path");
		assertTrue(descendantOf(root()).test(node("b")), "direct descendant should be a descendant of the current path");
	}

	@Test
	void canDetectIndirectDescendantNode() {
		assertTrue(descendantOf(path("a")).test(node("a.b.c")), "should be a descendant");
		assertTrue(descendantOf(root()).test(node("x.y")), "should be a descendant");
		assertTrue(descendantOf(path("f.i")).test(node("f.i.j.i")), "should be a descendant");
	}

	@Test
	void ancestorPathAreNotDescendant() {
		assertFalse(descendantOf(path("a.b.c")).test(node("a")), "should not be a descendant");
		assertFalse(descendantOf(path("x.y.z")).test(node("x.y")), "should not be a descendant");
		assertFalse(descendantOf(path("b.c")).test(rootNode()), "should not be a descendant");
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(descendantOf(path("a")), descendantOf(path("a")))
			.addEqualityGroup(descendantOf(path("b")))
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(descendantOf(path("a.b.c")), hasToString("ModelNodes.descendantOf(a.b.c)"));
	}
}

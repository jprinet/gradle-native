package dev.nokee.model.streams;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.NullPointerTester;
import dev.nokee.utils.ConsumerTestUtils;
import lombok.val;
import org.gradle.api.provider.Provider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.collect.ImmutableList.builder;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.utils.FunctionalInterfaceMatchers.*;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.junit.jupiter.api.Assertions.assertAll;

public interface ModelStreamTester<T> extends BranchedModelStreamTester<T> {
	ModelStream<T> createSubject();

	T createElement();

	@ParameterizedTest(name = "can apply filter [{argumentsWithNames}]")
	@EnumSource(TerminalOperator.class)
	default void emptyTopicHasNoElementToIterate(TerminalOperator operator) {
		val result = operator.apply(createSubject());
		assertThat(result, emptyIterable());
	}

	@ParameterizedTest(name = "can apply filter [{argumentsWithNames}]")
	@EnumSource(TerminalOperator.class)
	default void canApplyFilterOnStream(TerminalOperator operator) {
		val subject = createSubject();
		val e0 = createElement();
		val e1 = createElement();
		val result = operator.apply(subject.filter(it -> !it.equals(e0)));
		val e2 = createElement();
		assertThat(result, contains(e1, e2));
		val e3 = createElement();
		assertThat(result, contains(e1, e2, e3));
	}

	@ParameterizedTest(name = "can apply map [{argumentsWithNames}]")
	@EnumSource(TerminalOperator.class)
	default void canApplyMapOnStream(TerminalOperator operator) {
		val subject = createSubject();
		val e0 = createElement();
		val result = operator.apply(subject.map(Object::toString));
		val e1 = createElement();
		assertThat(result, contains(e0.toString(), e1.toString()));
		val e2 = createElement();
		assertThat(result, contains(e0.toString(), e1.toString(), e2.toString()));
	}

	@ParameterizedTest(name = "can apply flatMap [{argumentsWithNames}]")
	@EnumSource(TerminalOperator.class)
	default void canApplyFlatMapOnStream(TerminalOperator operator) {
		val subject = createSubject();
		val e0 = createElement();
		val result = operator.apply(subject.flatMap(it -> flatMapElement(it)));
		val e1 = createElement();
		assertThat(result, contains(builder().addAll(flatMapElement(e0)).addAll(flatMapElement(e1)).build().toArray()));
		val e2 = createElement();
		assertThat(result, contains(builder().addAll(flatMapElement(e0)).addAll(flatMapElement(e1)).addAll(flatMapElement(e2)).build().toArray()));
	}

	@ParameterizedTest(name = "can terminate stream multiple times [{argumentsWithNames}]")
	@EnumSource(TerminalOperator.class)
	default void canTerminateStreamMultipleTimes(TerminalOperator operator) {
		val subject = createSubject();
		val e0 = createElement();
		val result1 = operator.apply(subject);
		val e1 = createElement();
		val result2 = operator.apply(subject);
		val e2 = createElement();
		assertAll(
			() -> assertThat(result1, contains(e0, e1, e2)),
			() -> assertThat(result2, contains(e0, e1, e2))
		);
	}

	@Test
	default void canFilterOutAllElements() {
		val subject = createSubject();
		val e0 = createElement();
		val result = subject.filter(it -> false).collect(toList());
		val e1 = createElement();
		assertThat(result, providerOf(emptyIterable()));
	}

	@Test
	default void canFlatMapAllElementsToEmptyIterables() {
		val subject = createSubject();
		val e0 = createElement();
		val result = subject.flatMap(it -> emptyList()).collect(toList());
		val e1 = createElement();
		assertThat(result, providerOf(emptyIterable()));
	}

	@ParameterizedTest(name = "can create element during process action [{argumentsWithNames}]")
	@EnumSource(TerminalOperator.class)
	default void canCreateElementDuringProcessAction(TerminalOperator operator) {
		val subject = createSubject();
		val expectedElements = new ArrayList<>();
		expectedElements.add(createElement());
		val result = operator.apply(subject.map(new Function<T, T>() {
			private boolean added = false;
			@Override
			public T apply(T it) {
				if (!added) {
					expectedElements.add(ModelStreamTester.this.createElement());
					added = true;
				}
				return it;
			}
		}));
		expectedElements.add(createElement());
		assertThat(ImmutableList.copyOf(result), contains(expectedElements.toArray()));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	default void checkNulls() {
		new NullPointerTester().testAllPublicInstanceMethods(createSubject());
	}

	static <T> Iterable<String> flatMapElement(T t) {
		return Arrays.asList(t.toString() + "[0]", t.toString() + "[1]");
	}

	interface TerminalResult<T> extends Iterable<T> {

	}

	static final class ForEachOperatorResult<T> implements TerminalResult<T>, Consumer<T> {
		private final List<T> elements = new ArrayList<>();

		@Override
		public void accept(T t) {
			elements.add(t);
		}

		@Override
		public Iterator<T> iterator() {
			return elements.iterator();
		}
	}

	static final class CollectOperatorResult<T> implements TerminalResult<T> {
		private final Provider<List<T>> provider;

		public CollectOperatorResult(Provider<List<T>> provider) {
			this.provider = provider;
		}

		@Override
		public Iterator<T> iterator() {
			return provider.get().iterator();
		}
	}

	enum TerminalOperator {
		FOR_EACH {
			@Override
			<T> TerminalResult<T> apply(ModelStream<T> subject) {
				val result = new ForEachOperatorResult<T>();
				subject.forEach(result);
				return result;
			}
		},
		PEEK {
			@Override
			<T> TerminalResult<T> apply(ModelStream<T> subject) {
				val result = new ForEachOperatorResult<T>();
				subject.peek(result);
				return result;
			}
		},
		COLLECT {
			@Override
			<T> TerminalResult<T> apply(ModelStream<T> subject) {
				return new CollectOperatorResult<>(subject.collect(toList()));
			}
		};

		abstract <T> TerminalResult<T> apply(ModelStream<T> subject);
	}
}
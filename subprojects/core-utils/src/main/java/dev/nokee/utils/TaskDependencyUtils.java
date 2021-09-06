package dev.nokee.utils;

import com.google.common.collect.ImmutableSet;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.gradle.api.Task;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskDependency;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

public final class TaskDependencyUtils {
	/**
	 * Creates a {@link TaskDependency} containing all of the individual task dependencies.
	 *
	 * @param taskDependencies  the task dependencies contained within the resulting task dependency, must not be null
	 * @return a task dependency composed of all the specified task dependencies, never null
	 */
	public static TaskDependency composite(TaskDependency... taskDependencies) {
		val allTaskDependencies = ImmutableSet.copyOf(taskDependencies);
		if (allTaskDependencies.isEmpty()) {
			return EmptyTaskDependency.INSTANCE;
		} else if (allTaskDependencies.size() == 1) {
			return taskDependencies[0];
		} else {
			return new CompositeTaskDependency(allTaskDependencies);
		}
	}

	/** @see #composite(TaskDependency...) */
	@EqualsAndHashCode
	private static final class CompositeTaskDependency implements TaskDependency {
		private final ImmutableSet<TaskDependency> taskDependencies;

		private CompositeTaskDependency(ImmutableSet<TaskDependency> taskDependencies) {
			this.taskDependencies = taskDependencies;
		}

		@Override
		public Set<? extends Task> getDependencies(@Nullable Task task) {
			return taskDependencies.stream().flatMap(it -> it.getDependencies(task).stream()).collect(ImmutableSet.toImmutableSet());
		}

		@Override
		public String toString() {
			return "TaskDependencyUtils.composite(" + taskDependencies.stream().map(Object::toString).collect(joining(", ")) + ")";
		}
	}

	/**
	 * Creates task dependency for specified task provider.
	 * The provider is only realize when querying task dependency via {@link TaskDependency#getDependencies(Task)}.
	 *
	 * @param taskProvider  the task provider to use as task dependency, must not be null
	 * @return a task dependency for specified task provider, never null
	 */
	public static TaskDependency of(Provider<? extends Task> taskProvider) {
		return new OfTaskDependency(taskProvider);
	}

	@EqualsAndHashCode
	private static final class OfTaskDependency implements TaskDependency {
		private final Provider<? extends Task> taskProvider;

		private OfTaskDependency(Provider<? extends Task> taskProvider) {
			this.taskProvider = requireNonNull(taskProvider);
		}

		@Override
		public Set<? extends Task> getDependencies(@Nullable Task task) {
			return Collections.singleton(taskProvider.get());
		}

		@Override
		public String toString() {
			return "TaskDependencyUtils.of(" + taskProvider + ")";
		}
	}

	/**
	 * Creates an empty {@link TaskDependency}.
	 * It will always return an empty set.
	 *
	 * @return an empty task dependency, never null
	 */
	public static TaskDependency empty() {
		return EmptyTaskDependency.INSTANCE;
	}

	/** @see #empty() */
	private enum EmptyTaskDependency implements TaskDependency {
		INSTANCE;

		@Override
		public Set<? extends Task> getDependencies(@Nullable Task task) {
			return Collections.emptySet();
		}

		@Override
		public String toString() {
			return "TaskDependencyUtils.empty()";
		}
	}
}
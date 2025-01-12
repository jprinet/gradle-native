/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.platform.base.internal;

import dev.nokee.model.PolymorphicDomainObjectRegistry;
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelElementProviderSourceComponent;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.names.FullyQualifiedName;
import dev.nokee.model.internal.names.FullyQualifiedNameComponent;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import lombok.val;
import org.gradle.api.Namer;
import org.gradle.api.Task;
import org.gradle.api.internal.MutationGuards;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.core.ModelProjections.createdUsingNoInject;

public final class TaskRegistrationFactory {
	private final TaskContainer tasks;
	private final PolymorphicDomainObjectRegistry<Task> taskRegistry;
	private final Namer<TaskIdentifier<?>> taskNamer;

	// TODO: Specialize registry into TaskRegistry and return TaskProvider instead of NamedDomainObjectProvider.
	public TaskRegistrationFactory(TaskContainer tasks, PolymorphicDomainObjectRegistry<Task> taskRegistry, Namer<TaskIdentifier<?>> taskNamer) {
		this.tasks = tasks;
		this.taskRegistry = taskRegistry;
		this.taskNamer = taskNamer;
	}

	public <T extends Task> ModelRegistration.Builder create(TaskIdentifier<?> identifier, Class<T> type) {
		val name = FullyQualifiedName.of(taskNamer.determineName(identifier));
		val taskProvider = (TaskProvider<T>) taskRegistry.registerIfAbsent(name.toString(), type);
		return ModelRegistration.builder()
			.withComponent(new IdentifierComponent(identifier))
			.withComponent(IsTask.tag())
			.withComponent(ConfigurableTag.tag())
			.withComponent(new ModelElementProviderSourceComponent(taskProvider))
			.withComponent(createdUsingNoInject(ModelType.of(type), taskProvider::get))
			.withComponent(createdUsing(ModelType.of(TaskProvider.class), () -> taskProvider))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(IdentifierComponent.class), ModelComponentReference.of(IsTask.class), ModelComponentReference.of(ModelState.IsAtLeastCreated.class), (entity, id, tag, ignored) -> {
				if (id.get().equals(identifier)) {
					taskProvider.configure(task -> {
						MutationGuards.of(tasks).withMutationEnabled(__ -> ModelStates.realize(entity)).execute(null);
					});
				}
			}))
			;
	}
}

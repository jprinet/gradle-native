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
package dev.nokee.platform.nativebase.internal;

import dev.nokee.language.base.tasks.SourceCompile;
import dev.nokee.model.internal.ModelPropertyIdentifier;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.platform.base.TaskView;
import dev.nokee.platform.base.internal.BinaryIdentifier;
import dev.nokee.platform.base.internal.ComponentTasksPropertyRegistrationFactory;
import dev.nokee.platform.base.internal.IsBinary;
import dev.nokee.platform.base.internal.TaskRegistrationFactory;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.nativebase.SharedLibraryBinary;
import dev.nokee.platform.nativebase.tasks.LinkSharedLibrary;
import dev.nokee.platform.nativebase.tasks.internal.LinkSharedLibraryTask;
import dev.nokee.utils.TaskDependencyUtils;
import lombok.val;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.toPath;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;

public final class SharedLibraryBinaryRegistrationFactory {
	private final TaskRegistrationFactory taskRegistrationFactory;
	private final ModelPropertyRegistrationFactory propertyRegistrationFactory;
	private final ModelRegistry registry;
	private final ComponentTasksPropertyRegistrationFactory tasksPropertyRegistrationFactory;

	public SharedLibraryBinaryRegistrationFactory(TaskRegistrationFactory taskRegistrationFactory, ModelPropertyRegistrationFactory propertyRegistrationFactory, ModelRegistry registry, ComponentTasksPropertyRegistrationFactory tasksPropertyRegistrationFactory) {
		this.taskRegistrationFactory = taskRegistrationFactory;
		this.propertyRegistrationFactory = propertyRegistrationFactory;
		this.registry = registry;
		this.tasksPropertyRegistrationFactory = tasksPropertyRegistrationFactory;
	}

	public ModelRegistration create(BinaryIdentifier<?> identifier) {
		return ModelRegistration.builder()
			.withComponent(identifier)
			.withComponent(toPath(identifier))
			.withComponent(IsBinary.tag())
			.withComponent(createdUsing(ModelType.of(SharedLibraryBinary.class), ModelBackedSharedLibraryBinary::new))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(BinaryIdentifier.class), ModelComponentReference.of(ModelState.IsAtLeastRegistered.class), (entity, id, ignored) -> {
				if (id.equals(identifier)) {
					val linkTask = registry.register(taskRegistrationFactory.create(TaskIdentifier.of(identifier, "link"), LinkSharedLibraryTask.class).build());
					registry.register(propertyRegistrationFactory.create(ModelPropertyIdentifier.of(identifier, "linkTask"), ModelNodes.of(linkTask)));
					registry.register(tasksPropertyRegistrationFactory.create(ModelPropertyIdentifier.of(identifier, "compileTasks"), SourceCompile.class));
				}
			}))
			.build();
	}

	private static final class ModelBackedSharedLibraryBinary implements SharedLibraryBinary, HasPublicType, ModelNodeAware {
		private final ModelNode node = ModelNodeContext.getCurrentModelNode();

		@Override
		public TaskView<SourceCompile> getCompileTasks() {
			return ModelProperties.getProperty(this, "compileTasks").as(TaskView.class).get();
		}

		@Override
		public TaskProvider<LinkSharedLibrary> getLinkTask() {
			return ModelProperties.getProperty(this, "linkTask").as(TaskProvider.class).get();
		}

		@Override
		public boolean isBuildable() {
			return false; // FIXME: Should correctly check buildable
		}

		@Override
		public TaskDependency getBuildDependencies() {
			return TaskDependencyUtils.of(getLinkTask());
		}

		@Override
		public TypeOf<?> getPublicType() {
			return TypeOf.typeOf(SharedLibraryBinary.class);
		}

		@Override
		public ModelNode getNode() {
			return node;
		}
	}
}
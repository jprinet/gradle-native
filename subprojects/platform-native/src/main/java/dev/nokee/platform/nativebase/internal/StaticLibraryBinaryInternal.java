/*
 * Copyright 2020-2021 the original author or authors.
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

import com.google.common.collect.ImmutableSet;
import dev.nokee.language.nativebase.internal.ObjectSourceSet;
import dev.nokee.model.internal.names.FullyQualifiedName;
import dev.nokee.platform.base.TaskView;
import dev.nokee.platform.base.internal.BinaryIdentifier;
import dev.nokee.platform.nativebase.StaticLibraryBinary;
import dev.nokee.platform.nativebase.internal.dependencies.NativeIncomingDependencies;
import dev.nokee.platform.nativebase.tasks.CreateStaticLibrary;
import dev.nokee.platform.nativebase.tasks.internal.CreateStaticLibraryTask;
import dev.nokee.platform.nativebase.tasks.internal.ObjectFilesToBinaryTask;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import dev.nokee.runtime.nativebase.TargetMachine;
import lombok.AccessLevel;
import lombok.Getter;
import org.gradle.api.Buildable;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Task;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFile;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Set;

public class StaticLibraryBinaryInternal extends BaseNativeBinary implements StaticLibraryBinary, Buildable {
	private final TaskProvider<CreateStaticLibraryTask> createTask;
	@Getter(AccessLevel.PROTECTED) private final TaskContainer tasks;

	@Inject
	public StaticLibraryBinaryInternal(FullyQualifiedName name, BinaryIdentifier<?> identifier, DomainObjectSet<ObjectSourceSet> objectSourceSets, TargetMachine targetMachine, TaskProvider<CreateStaticLibraryTask> createTask, NativeIncomingDependencies dependencies, ObjectFactory objects, ProjectLayout layout, ProviderFactory providers, ConfigurationContainer configurations, TaskContainer tasks, TaskView<Task> compileTasks) {
		super(name, identifier, objectSourceSets, targetMachine, dependencies, objects, layout, providers, configurations, compileTasks);
		this.createTask = createTask;
		this.tasks = tasks;

		createTask.configure(this::configureStaticLibraryTask);
	}

	private void configureStaticLibraryTask(CreateStaticLibraryTask task) {
		task.setDescription("Creates the static library.");
		task.source(getObjectFiles());

		task.getTargetPlatform().set(getTargetPlatform());
		task.getTargetPlatform().finalizeValueOnRead();
		task.getTargetPlatform().disallowChanges();

		task.getOutputFile().set(getStaticLibraryCreatedFile());

		task.getToolChain().set(selectNativeToolChain(getTargetMachine()));
		task.getToolChain().finalizeValueOnRead();
		task.getToolChain().disallowChanges();
	}

	private Provider<RegularFile> getStaticLibraryCreatedFile() {
		return getLayout().getBuildDirectory().file(getBaseName().map(it -> {
			OperatingSystemFamily osFamily = getTargetMachine().getOperatingSystemFamily();
			OperatingSystemOperations osOperations = OperatingSystemOperations.of(osFamily);
			return osOperations.getStaticLibraryName(identifier.getOutputDirectoryBase("libs") + "/" + it);
		}));
	}

	@Override
	public TaskProvider<CreateStaticLibrary> getCreateTask() {
		return getTasks().named(createTask.getName(), CreateStaticLibrary.class);
	}

	@Override
	public TaskProvider<ObjectFilesToBinaryTask> getCreateOrLinkTask() {
		return getTasks().named(createTask.getName(), ObjectFilesToBinaryTask.class);
	}

	@Override
	public TaskDependency getBuildDependencies() {
		return new TaskDependency() {
			@Override
			public Set<? extends Task> getDependencies(@Nullable Task task) {
				return ImmutableSet.of(createTask.get());
			}
		};
	}
}

package dev.nokee.platform.nativebase.internal;

import com.google.common.collect.ImmutableSet;
import dev.nokee.language.base.internal.GeneratedSourceSet;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.StaticLibraryBinary;
import dev.nokee.platform.nativebase.internal.dependencies.NativeIncomingDependencies;
import dev.nokee.platform.nativebase.tasks.CreateStaticLibrary;
import dev.nokee.platform.nativebase.tasks.internal.CreateStaticLibraryTask;
import dev.nokee.platform.nativebase.tasks.internal.ObjectFilesToBinaryTask;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import dev.nokee.runtime.nativebase.internal.DefaultTargetMachine;
import org.gradle.api.Buildable;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Task;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Set;

public abstract class StaticLibraryBinaryInternal extends BaseNativeBinary implements StaticLibraryBinary, Buildable {
	private final TaskProvider<CreateStaticLibraryTask> createTask;

	@Inject
	public StaticLibraryBinaryInternal(NamingScheme names, DomainObjectSet<GeneratedSourceSet> objectSourceSets, DefaultTargetMachine targetMachine, TaskProvider<CreateStaticLibraryTask> createTask, NativeIncomingDependencies dependencies) {
		super(names, objectSourceSets, targetMachine, dependencies);
		this.createTask = createTask;

		createTask.configure(this::configureStaticLibraryTask);
	}

	@Inject
	protected abstract TaskContainer getTasks();

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
			return osOperations.getStaticLibraryName(getNames().getOutputDirectoryBase("libs") + "/" + it);
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

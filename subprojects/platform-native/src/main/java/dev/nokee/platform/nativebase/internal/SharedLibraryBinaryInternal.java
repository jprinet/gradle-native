package dev.nokee.platform.nativebase.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dev.nokee.language.base.internal.GeneratedSourceSet;
import dev.nokee.language.base.internal.LanguageSourceSetInternal;
import dev.nokee.language.nativebase.HeaderSearchPath;
import dev.nokee.language.nativebase.internal.DefaultHeaderSearchPath;
import dev.nokee.language.nativebase.internal.HeaderExportingSourceSetInternal;
import dev.nokee.language.nativebase.internal.UTTypeObjectCode;
import dev.nokee.language.nativebase.tasks.internal.NativeSourceCompileTask;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.SharedLibraryBinary;
import dev.nokee.platform.nativebase.tasks.LinkSharedLibrary;
import dev.nokee.platform.nativebase.tasks.internal.LinkSharedLibraryTask;
import dev.nokee.runtime.nativebase.internal.DefaultTargetMachine;
import lombok.Value;
import org.gradle.api.Buildable;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.internal.jvm.Jvm;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.language.nativeplatform.tasks.AbstractNativeCompileTask;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static dev.nokee.runtime.nativebase.internal.DependencyUtils.isFrameworkDependency;

public abstract class SharedLibraryBinaryInternal extends BaseNativeBinary implements SharedLibraryBinary, Buildable {
	private static final Logger LOGGER = Logger.getLogger(SharedLibraryBinaryInternal.class.getName());
	private final Configuration linkConfiguration;
	private final TaskProvider<LinkSharedLibraryTask> linkTask;
	private final DomainObjectSet<? super LanguageSourceSetInternal> sources;
	private final DefaultTargetMachine targetMachine;

	@Inject
	public SharedLibraryBinaryInternal(NamingScheme names, DomainObjectSet<LanguageSourceSetInternal> parentSources, Configuration implementation, DefaultTargetMachine targetMachine, DomainObjectSet<GeneratedSourceSet<UTTypeObjectCode>> objectSourceSets, TaskProvider<LinkSharedLibraryTask> linkTask, Configuration linkOnly) {
		super(objectSourceSets);
		this.linkTask = linkTask;
		sources = getObjects().domainObjectSet(LanguageSourceSetInternal.class);
		this.targetMachine = targetMachine;
		parentSources.all(it -> sources.add(it));

		getLinkerInputs().value(fromLinkConfiguration()).finalizeValueOnRead();
		getLinkerInputs().disallowChanges();

		ConfigurationUtils configurationUtils = getObjects().newInstance(ConfigurationUtils.class);
		this.linkConfiguration = getConfigurations().create(names.getConfigurationName("nativeLinkLibraries"),
			configurationUtils.asIncomingLinkLibrariesFrom(implementation, linkOnly)
				.forTargetMachine(targetMachine)
				.asDebug()
				.withDescription("Link libraries for JNI shared library."));

		// configure includes using the native incoming compile configuration
		getCompileTasks().configureEach(task -> {
			if (task instanceof AbstractNativeCompileTask) {
				AbstractNativeCompileTask softwareModelTaskInternal = (AbstractNativeCompileTask) task;
				NativeSourceCompileTask taskInternal = (NativeSourceCompileTask) task;
				taskInternal.getHeaderSearchPaths().addAll(softwareModelTaskInternal.getIncludes().getElements().map(SharedLibraryBinaryInternal::toHeaderSearchPaths));

				softwareModelTaskInternal.includes("src/main/headers");

				softwareModelTaskInternal.setPositionIndependentCode(true);

				sources.withType(HeaderExportingSourceSetInternal.class, sourceSet -> softwareModelTaskInternal.getIncludes().from(sourceSet.getSource()));

				// TODO: Move this to JNI Library configuration
				softwareModelTaskInternal.getIncludes().from(getJvmIncludes());
			}
		});

		linkTask.configure(task -> {
			task.dependsOn(linkConfiguration);
			task.getLibs().from(getLinkerInputs().map(this::toLinkLibraries));
			task.getLinkerArgs().addAll(getLinkerInputs().map(this::toFrameworkFlags));

			Provider<String> installName = task.getLinkedFile().getLocationOnly().map(linkedFile -> linkedFile.getAsFile().getName());
			task.getInstallName().set(installName);
		});

		getLinkedFile().set(linkTask.flatMap(AbstractLinkTask::getLinkedFile));
		getLinkedFile().disallowChanges();
	}

	@Inject
	protected abstract ConfigurationContainer getConfigurations();

	@Inject
	protected abstract ObjectFactory getObjects();

	public TaskProvider<? extends LinkSharedLibrary> getLinkTask() {
		return linkTask;
	}

	@Inject
	protected abstract ProviderFactory getProviderFactory();

	public abstract RegularFileProperty getLinkedFile();

	@Override
	public boolean isBuildable() {
		return super.isBuildable() && isBuildable(linkTask.get());
	}

	private static boolean isBuildable(LinkSharedLibrary linkTask) {
		AbstractLinkTask linkTaskInternal = (AbstractLinkTask)linkTask;
		return isBuildable(linkTaskInternal.getToolChain().get(), linkTaskInternal.getTargetPlatform().get());
	}

	private static List<HeaderSearchPath> toHeaderSearchPaths(Set<FileSystemLocation> paths) {
		return paths.stream().map(FileSystemLocation::getAsFile).map(DefaultHeaderSearchPath::new).collect(Collectors.toList());
	}

	private Provider<List<File>> getJvmIncludes() {
		return getProviderFactory().provider(() -> {
			List<File> result = new ArrayList<>();
			result.add(new File(Jvm.current().getJavaHome().getAbsolutePath() + "/include"));

			if (OperatingSystem.current().isMacOsX()) {
				result.add(new File(Jvm.current().getJavaHome().getAbsolutePath() + "/include/darwin"));
			} else if (OperatingSystem.current().isLinux()) {
				result.add(new File(Jvm.current().getJavaHome().getAbsolutePath() + "/include/linux"));
			} else if (OperatingSystem.current().isWindows()) {
				result.add(new File(Jvm.current().getJavaHome().getAbsolutePath() + "/include/win32"));
			}
			return result;
		});
	}

	//region Linker inputs
	private Provider<List<LinkerInput>> fromLinkConfiguration() {
		return getProviderFactory().provider(() -> linkConfiguration.getIncoming().getArtifacts().getArtifacts().stream().map(LinkerInput::of).collect(Collectors.toList()));
	}

	public abstract ListProperty<LinkerInput> getLinkerInputs();

	private List<String> toFrameworkFlags(List<LinkerInput> inputs) {
		return inputs.stream().filter(LinkerInput::isFramework).flatMap(it -> ImmutableList.of("-F", it.getFile().getParent(), "-framework", it.getFile().getName().substring(0, it.getFile().getName().lastIndexOf("."))).stream()).collect(Collectors.toList());
	}

	private List<File> toLinkLibraries(List<LinkerInput> inputs) {
		return inputs.stream().filter(it -> !it.isFramework()).map(LinkerInput::getFile).collect(Collectors.toList());
	}

	@Value
	static class LinkerInput {
		boolean framework;
		File file;

		public static LinkerInput of(ResolvedArtifactResult result) {
			return new LinkerInput(isFrameworkDependency(result), result.getFile());
		}
	}
	//endregion


	@Override
	public TaskDependency getBuildDependencies() {
		return task -> ImmutableSet.of(getLinkTask().get());
	}
}

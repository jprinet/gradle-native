package dev.nokee.platform.objectivec.internal.plugins;

import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dev.nokee.gradle.internal.GradleModule;
import dev.nokee.platform.base.DomainObjectElement;
import dev.nokee.platform.base.internal.DomainObjectIdentity;
import dev.nokee.platform.base.internal.DomainObjectStore;
import dev.nokee.platform.base.internal.plugins.ProjectStorePlugin;
import dev.nokee.platform.c.internal.DefaultCApplicationExtension;
import dev.nokee.platform.c.internal.DefaultCApplicationExtensionFactory;
import dev.nokee.platform.cpp.internal.DefaultCppApplicationExtension;
import dev.nokee.platform.cpp.internal.DefaultCppApplicationExtensionFactory;
import dev.nokee.platform.nativebase.internal.DefaultNativeApplicationComponent;
import dev.nokee.platform.nativebase.internal.NativeComponentModule;
import dev.nokee.platform.nativebase.internal.TargetBuildTypeRule;
import dev.nokee.platform.nativebase.internal.TargetMachineRule;
import dev.nokee.platform.objectivec.ObjectiveCApplicationExtension;
import dev.nokee.platform.objectivec.internal.DefaultObjectiveCApplicationExtension;
import dev.nokee.platform.objectivec.internal.DefaultObjectiveCApplicationExtensionFactory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.nativeplatform.toolchain.internal.plugins.StandardToolChainsPlugin;

import javax.inject.Inject;

public class ObjectiveCApplicationPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "application";
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;

	@Inject
	public ObjectiveCApplicationPlugin(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(StandardToolChainsPlugin.class);
		project.getPluginManager().apply(ProjectStorePlugin.class);

		val store = project.getExtensions().getByType(DomainObjectStore.class);
		val extension = DaggerObjectiveCApplicationPlugin_ObjectiveCApplicationComponent.factory().create(project).objectiveCApplicationComponent();
		val component = store.add(new DomainObjectElement<DefaultNativeApplicationComponent>() {
			@Override
			public DefaultNativeApplicationComponent get() {
				return extension.getComponent();
			}

			@Override
			public Class<DefaultNativeApplicationComponent> getType() {
				return DefaultNativeApplicationComponent.class;
			}

			@Override
			public DomainObjectIdentity getIdentity() {
				return DomainObjectIdentity.named("main");
			}
		});
		component.configure(it -> it.getBaseName().convention(project.getName()));
		component.get(); // force realize... for now.

		project.afterEvaluate(getObjects().newInstance(TargetMachineRule.class, extension.getTargetMachines(), EXTENSION_NAME));
		project.afterEvaluate(getObjects().newInstance(TargetBuildTypeRule.class, extension.getTargetBuildTypes(), EXTENSION_NAME));
		project.afterEvaluate(extension::finalizeExtension);

		project.getExtensions().add(ObjectiveCApplicationExtension.class, EXTENSION_NAME, extension);
	}

	@Module
	interface ObjectiveCModule {
		@Provides
		static DefaultObjectiveCApplicationExtension theExtension(DefaultObjectiveCApplicationExtensionFactory factory) {
			return factory.create();
		}
	}

	@Component(modules = {GradleModule.class, NativeComponentModule.class, ObjectiveCModule.class})
	interface ObjectiveCApplicationComponent {
		DefaultObjectiveCApplicationExtension objectiveCApplicationComponent();

		@Component.Factory
		interface Factory {
			ObjectiveCApplicationComponent create(@BindsInstance Project project);
		}
	}
}

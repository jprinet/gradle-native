package dev.nokee.model.internal;

import dev.nokee.model.NokeeExtension;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.PluginAware;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;
import java.util.function.Consumer;
import java.util.function.Function;

import static dev.nokee.utils.NamedDomainObjectCollectionUtils.whenElementKnown;

public /*final*/ abstract class ModelBasePlugin<T extends PluginAware & ExtensionAware> implements Plugin<T> {
	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	protected abstract ProviderFactory getProviders();

	@Override
	public void apply(T target) {
		val type = computeWhen(target, it -> Settings.class, it -> Project.class);

		val registry = new DefaultNamedDomainObjectRegistry();
		val extension = getObjects().newInstance(DefaultNokeeExtension.class, registry);
		target.getExtensions().add(NokeeExtension.class, "nokee", extension);

		extension.getModelRegistry().getRoot().newProjection(builder -> builder.type(type).forInstance(target));

		executeWhen(target,
			it -> {

			},
			project -> {
				registry.registerContainer(new NamedDomainObjectContainerRegistry.TaskContainerRegistry(project.getTasks()));
				registry.registerContainer(new NamedDomainObjectContainerRegistry.NamedContainerRegistry<>(project.getConfigurations()));
				whenElementKnown(project.getTasks(), new RegisterModelProjection<>(extension.getModelRegistry()));
				whenElementKnown(project.getConfigurations(), new RegisterModelProjection<>(extension.getModelRegistry()));
			}
		);
	}

	public static NokeeExtension nokee(ExtensionAware target) {
		return target.getExtensions().getByType(NokeeExtension.class);
	}

	private static <T> T computeWhen(Object target, Function<? super Settings, T> settingsAction, Function<? super Project, T> projectAction) {
		if (target instanceof Settings) {
			return settingsAction.apply((Settings) target);
		} else if (target instanceof Project) {
			return projectAction.apply((Project) target);
		} else {
			throw new UnsupportedOperationException();
		}
	}

	private static void executeWhen(Object target, Consumer<? super Settings> settingsAction, Consumer<? super Project> projectAction) {
		if (target instanceof Settings) {
			settingsAction.accept((Settings) target);
		} else if (target instanceof Project) {
			projectAction.accept((Project) target);
		} else {
			throw new UnsupportedOperationException();
		}
	}
}

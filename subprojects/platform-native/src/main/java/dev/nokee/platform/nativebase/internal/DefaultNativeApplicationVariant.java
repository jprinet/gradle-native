package dev.nokee.platform.nativebase.internal;

import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.VariantInternal;
import dev.nokee.platform.base.internal.dependencies.ResolvableComponentDependencies;
import dev.nokee.platform.nativebase.NativeApplication;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.VariantComponentDependencies;
import lombok.Getter;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;

import javax.inject.Inject;

public class DefaultNativeApplicationVariant extends BaseNativeVariant implements NativeApplication, VariantInternal {
	@Getter private final DefaultNativeApplicationComponentDependencies dependencies;
	@Getter private final ResolvableComponentDependencies resolvableDependencies;
	private final BinaryView<Binary> binaryView;

	@Inject
	public DefaultNativeApplicationVariant(VariantIdentifier<DefaultNativeApplicationVariant> identifier, NamingScheme names, VariantComponentDependencies<DefaultNativeApplicationComponentDependencies> dependencies, ObjectFactory objects, TaskContainer tasks, ProviderFactory providers, BinaryView<Binary> binaryView) {
		super(identifier, names, objects, tasks, providers);
		this.dependencies = dependencies.getDependencies();
		this.resolvableDependencies = dependencies.getIncoming();
		this.binaryView = binaryView;
	}

	@Override
	public BinaryView<Binary> getBinaries() {
		return binaryView;
	}

	@Override
	public DomainObjectSet<Binary> getBinaryCollection() {
		throw new UnsupportedOperationException();
	}
}

package dev.nokee.testing.xctest.internal;

import dev.nokee.language.swift.internal.SwiftSourceSet;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.dependencies.ConfigurationBucketRegistryImpl;
import dev.nokee.platform.base.internal.dependencies.DefaultComponentDependencies;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketFactoryImpl;
import dev.nokee.platform.ios.internal.IosApplicationOutgoingDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.*;
import lombok.Getter;
import lombok.val;
import lombok.var;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.SetProperty;

public final class XCTestTestSuiteComponentVariants implements ComponentVariants {
	@Getter private final VariantCollection<DefaultXCTestTestSuiteVariant> variantCollection;
	@Getter private final SetProperty<BuildVariantInternal> buildVariants;
	private final ObjectFactory objectFactory;
	private final BaseXCTestTestSuiteComponent component;
	private final DependencyHandler dependencyHandler;
	private final ConfigurationContainer configurationContainer;

	public XCTestTestSuiteComponentVariants(ObjectFactory objectFactory, BaseXCTestTestSuiteComponent component, DependencyHandler dependencyHandler, ConfigurationContainer configurationContainer) {
		this.variantCollection = new VariantCollection<>(DefaultXCTestTestSuiteVariant.class, objectFactory);
		this.buildVariants = objectFactory.setProperty(BuildVariantInternal.class);
		this.objectFactory = objectFactory;
		this.component = component;
		this.dependencyHandler = dependencyHandler;
		this.configurationContainer = configurationContainer;
	}

	public void calculateVariants() {
		getBuildVariants().get().forEach(buildVariant -> {
			val names = component.getNames().forBuildVariant(buildVariant, getBuildVariants().get());
			val variantIdentifier = VariantIdentifier.builder().withUnambiguousNameFromBuildVariants(buildVariant, getBuildVariants().get()).withComponentIdentifier(component.getIdentifier()).withType(DefaultXCTestTestSuiteVariant.class).build();

			val dependencies = newDependencies(names.withComponentDisplayName(component.getIdentifier().getDisplayName()), buildVariant, variantIdentifier);
			val variant = getVariantCollection().registerVariant(variantIdentifier, (name, bv) -> createVariant(variantIdentifier, dependencies));

			onEachVariantDependencies(variant, dependencies);
		});
	}

	private DefaultXCTestTestSuiteVariant createVariant(VariantIdentifier<?> identifier, VariantComponentDependencies<?> variantDependencies) {
		val buildVariant = (BuildVariantInternal) identifier.getBuildVariant();
		val names = component.getNames().forBuildVariant(buildVariant, getBuildVariants().get());

		DefaultXCTestTestSuiteVariant result = objectFactory.newInstance(DefaultXCTestTestSuiteVariant.class, identifier, names, variantDependencies);
		return result;
	}

	private VariantComponentDependencies<DefaultNativeComponentDependencies> newDependencies(NamingScheme names, BuildVariantInternal buildVariant, VariantIdentifier<DefaultXCTestTestSuiteVariant> variantIdentifier) {
		var variantDependencies = component.getDependencies();
		if (getBuildVariants().get().size() > 1) {
			val dependencyContainer = objectFactory.newInstance(DefaultComponentDependencies.class, variantIdentifier, new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(configurationContainer), dependencyHandler));
			variantDependencies = objectFactory.newInstance(DefaultNativeComponentDependencies.class, dependencyContainer);
			variantDependencies.configureEach(variantBucket -> {
				component.getDependencies().findByName(variantBucket.getName()).ifPresent(componentBucket -> {
					variantBucket.getAsConfiguration().extendsFrom(componentBucket.getAsConfiguration());
				});
			});
		}

		val incomingDependenciesBuilder = DefaultNativeIncomingDependencies.builder(variantDependencies).withVariant(buildVariant);
		boolean hasSwift = !component.getSourceCollection().withType(SwiftSourceSet.class).isEmpty();
		if (hasSwift) {
			incomingDependenciesBuilder.withIncomingSwiftModules();
		} else {
			incomingDependenciesBuilder.withIncomingHeaders();
		}

		NativeIncomingDependencies incoming = incomingDependenciesBuilder.buildUsing(objectFactory);
		NativeOutgoingDependencies outgoing = objectFactory.newInstance(IosApplicationOutgoingDependencies.class, names, buildVariant, variantDependencies);

		return new VariantComponentDependencies<>(variantDependencies, incoming, outgoing);
	}

	private void onEachVariantDependencies(VariantProvider<DefaultXCTestTestSuiteVariant> variant, VariantComponentDependencies<?> dependencies) {
		dependencies.getOutgoing().getExportedBinary().convention(variant.flatMap(it -> it.getDevelopmentBinary()));
	}
}

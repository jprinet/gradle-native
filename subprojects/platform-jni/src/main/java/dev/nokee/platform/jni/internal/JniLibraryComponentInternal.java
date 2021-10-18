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
package dev.nokee.platform.jni.internal;

import com.google.common.collect.Iterables;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.registry.ModelNodeBackedKnownDomainObject;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.platform.base.*;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.binaries.BinaryViewFactory;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.jni.JavaNativeInterfaceLibraryComponentDependencies;
import dev.nokee.platform.jni.JavaNativeInterfaceLibrarySources;
import dev.nokee.platform.jni.JniLibrary;
import dev.nokee.platform.nativebase.internal.rules.CreateVariantAssembleLifecycleTaskRule;
import dev.nokee.runtime.core.CoordinateSet;
import dev.nokee.runtime.core.Coordinates;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.runtime.nativebase.internal.TargetLinkages;
import dev.nokee.utils.ConfigureUtils;
import groovy.lang.Closure;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.util.ConfigureUtil;

import javax.inject.Inject;
import java.util.function.Supplier;

import static dev.nokee.model.internal.core.ModelActions.once;
import static dev.nokee.model.internal.core.ModelComponentType.projectionOf;
import static dev.nokee.model.internal.core.ModelNodeUtils.applyTo;
import static dev.nokee.model.internal.core.ModelNodes.stateAtLeast;
import static dev.nokee.model.internal.core.NodePredicate.allDirectDescendants;
import static dev.nokee.runtime.core.Coordinates.coordinateTypeOf;
import static dev.nokee.runtime.core.Coordinates.toCoordinateSet;
import static dev.nokee.utils.TransformerUtils.collect;
import static dev.nokee.utils.TransformerUtils.toSetTransformer;

public class JniLibraryComponentInternal extends BaseComponent<JniLibraryInternal> implements DependencyAwareComponent<JavaNativeInterfaceLibraryComponentDependencies>, BinaryAwareComponent, Component, ModelBackedSourceAwareComponentMixIn<JavaNativeInterfaceLibrarySources>, ModelBackedBinaryAwareComponentMixIn {
	@Getter private final GroupId groupId;
	@Getter private final SetProperty<TargetMachine> targetMachines;
	private final BinaryView<Binary> binaries;
	private final Supplier<JavaNativeInterfaceComponentVariants> componentVariants;
	private final SetProperty<BuildVariantInternal> buildVariants;
	private final Property<JniLibraryInternal> developmentVariant;
	private final TaskRegistry taskRegistry;

	@Inject
	public JniLibraryComponentInternal(ComponentIdentifier<?> identifier, GroupId groupId, ObjectFactory objects, BinaryViewFactory binaryViewFactory, TaskRegistry taskRegistry) {
		super(identifier, objects);
		this.groupId = groupId;
		this.targetMachines = ConfigureUtils.configureDisplayName(objects.setProperty(TargetMachine.class), "targetMachines");
		this.buildVariants = objects.setProperty(BuildVariantInternal.class);
		this.developmentVariant = objects.property(JniLibraryInternal.class);
		this.taskRegistry = taskRegistry;
		this.componentVariants = () -> ModelNodeUtils.get(getNode(), JavaNativeInterfaceComponentVariants.class);
		this.binaries = binaryViewFactory.create(identifier);

		// Order here doesn't align with general native
		getDimensions().add(getTargetMachines()
			.map(assertNonEmpty("target machine", identifier.getName().toString()))
			.map(toSetTransformer(coordinateTypeOf(TargetMachine.class)).andThen(collect(toCoordinateSet()))));
		// TODO: Missing build type dimension
		getDimensions().add(CoordinateSet.of(Coordinates.of(TargetLinkages.SHARED)));

		getBuildVariants().convention(getFinalSpace().map(DefaultBuildVariant::fromSpace));
		getBuildVariants().finalizeValueOnRead();
		getBuildVariants().disallowChanges(); // Let's disallow changing them for now.
	}

	private static <I extends Iterable<T>, T> Transformer<I, I> assertNonEmpty(String propertyName, String componentName) {
		return values -> {
			if (Iterables.isEmpty(values)) {
				throw new IllegalArgumentException(String.format("A %s needs to be specified for component '%s'.", propertyName, componentName));
			}
			return values;
		};
	}

	@Override
	public DefaultJavaNativeInterfaceLibraryComponentDependencies getDependencies() {
		return ModelProperties.getProperty(this, "dependencies").as(DefaultJavaNativeInterfaceLibraryComponentDependencies.class).get();
	}

	@Override
	public void dependencies(Action<? super JavaNativeInterfaceLibraryComponentDependencies> action) {
		action.execute(getDependencies());
	}

	@Override
	public void dependencies(@SuppressWarnings("rawtypes") Closure closure) {
		dependencies(ConfigureUtil.configureUsing(closure));
	}

	//region Variant-awareness
	public VariantView<JniLibraryInternal> getVariants() {
		return ModelProperties.getProperty(this, "variants").as(VariantView.class).get();
	}
	//endregion

	public Configuration getJvmImplementationDependencies() {
		return getDependencies().getJvmImplementation().getAsConfiguration();
	}

	@Override
	public Property<JniLibraryInternal> getDevelopmentVariant() {
		return developmentVariant;
	}

	@Override
	public BinaryView<Binary> getBinaries() {
		return binaries;
	}

	@Override
	public VariantCollection<JniLibraryInternal> getVariantCollection() {
		throw new UnsupportedOperationException("Use 'variants' property instead.");
	}

	@Override
	public SetProperty<BuildVariantInternal> getBuildVariants() {
		return buildVariants;
	}

	public void finalizeValue() {
		whenElementKnown(this, ModelActionWithInputs.of(ModelComponentReference.of(VariantIdentifier.class), ModelComponentReference.ofAny(projectionOf(JniLibrary.class)), (entity, variantIdentifier, variantProjection) -> {
			new CreateVariantAssembleLifecycleTaskRule(taskRegistry).accept(new ModelNodeBackedKnownDomainObject<>(ModelType.of(JniLibrary.class), entity));
		}));
		componentVariants.get().calculateVariants();
	}

	private static void whenElementKnown(Object target, ModelAction action) {
		applyTo(ModelNodes.of(target), allDirectDescendants(stateAtLeast(ModelState.Created)).apply(once(action)));
	}
}

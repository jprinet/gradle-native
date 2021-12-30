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
package dev.nokee.model.internal.core;

import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.ConfigurableStrategy;
import dev.nokee.model.internal.NamedStrategy;
import dev.nokee.model.internal.type.ModelType;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.provider.Property;

import java.util.Objects;
import java.util.function.Supplier;

// TODO: implementing ModelNodeAware is simply for legacy reason, it needs to be removed.
public final class DefaultModelElement implements ModelElement, ModelNodeAware {
	private final NamedStrategy namedStrategy;
	private final ConfigurableStrategy configurableStrategy;
	private final ModelCastableStrategy castableStrategy;
	private final ModelPropertyLookupStrategy propertyLookup;
	private final Supplier<ModelNode> entitySupplier;

	public DefaultModelElement(NamedStrategy namedStrategy, ConfigurableStrategy configurableStrategy, ModelCastableStrategy castableStrategy, ModelPropertyLookupStrategy propertyLookup) {
		this(namedStrategy, configurableStrategy, castableStrategy, propertyLookup, () -> { throw new UnsupportedOperationException(); });
	}

	private DefaultModelElement(NamedStrategy namedStrategy, ConfigurableStrategy configurableStrategy, ModelCastableStrategy castableStrategy, ModelPropertyLookupStrategy propertyLookup, Supplier<ModelNode> entitySupplier) {
		this.namedStrategy = Objects.requireNonNull(namedStrategy);
		this.configurableStrategy = Objects.requireNonNull(configurableStrategy);
		this.castableStrategy = Objects.requireNonNull(castableStrategy);
		this.propertyLookup = Objects.requireNonNull(propertyLookup);
		this.entitySupplier = Objects.requireNonNull(entitySupplier);
	}

	public static DefaultModelElement of(ModelNode entity) {
		Objects.requireNonNull(entity);
		val namedStrategy = new NamedStrategy() {
			@Override
			public String getAsString() {
				return entity.getComponent(ElementNameComponent.class).get();
			}
		};
		val castableStrategy = new ModelBackedModelCastableStrategy(entity);
		val configurableStrategy = new ConfigurableStrategy() {
			@Override
			public <S> void configure(ModelType<S> type, Action<? super S> action) {
				assert type != null;
				assert action != null;
				if (!ModelNodeUtils.canBeViewedAs(entity, type)) {
					throw new RuntimeException("...");
				}
				if (type.isSubtypeOf(Property.class)) {
					action.execute(castableStrategy.castTo(type).get());
				} else {
					castableStrategy.castTo(type).configure(action);
				}
			}
		};
		val propertyLookup = new ModelBackedModelPropertyLookupStrategy(entity);
		return new DefaultModelElement(
			namedStrategy,
			configurableStrategy,
			castableStrategy,
			propertyLookup,
			() -> entity
		);
	}

	@Override
	public <S> DomainObjectProvider<S> as(ModelType<S> type) {
		Objects.requireNonNull(type);
		return castableStrategy.castTo(type);
	}

	public <S> S asType(Class<S> ignored) {
		throw new UnsupportedOperationException("Use ModelElement#as(ModelType) instead.");
	}

	@Override
	public boolean instanceOf(ModelType<?> type) {
		Objects.requireNonNull(type);
		return castableStrategy.instanceOf(type);
	}

	@Override
	public ModelElement property(String name) {
		Objects.requireNonNull(name);
		return propertyLookup.get(name);
	}

	@Override
	public <S> ModelElement configure(ModelType<S> type, Action<? super S> action) {
		Objects.requireNonNull(type);
		Objects.requireNonNull(action);
		configurableStrategy.configure(type, action);
		return this;
	}

	@Override
	public String getName() {
		return namedStrategy.getAsString();
	}

	@Override
	public ModelNode getNode() {
		return entitySupplier.get();
	}
}
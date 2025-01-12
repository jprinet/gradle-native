/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.model;

import dev.nokee.model.internal.core.ModelElement;
import dev.nokee.model.internal.type.ModelType;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.util.ConfigureUtil;

import static java.util.Objects.requireNonNull;

public interface DomainObjectProvider<T> extends KnownDomainObject<T>, ModelElement {
	/**
	 * {@inheritDoc}
	 */
	@Override
	DomainObjectProvider<T> configure(Action<? super T> action);

	/**
	 * {@inheritDoc}
	 */
	@Override
	default DomainObjectProvider<T> configure(@DelegatesTo(type = "T", strategy = Closure.DELEGATE_FIRST) @SuppressWarnings("rawtypes") Closure closure) {
		return configure(ConfigureUtil.configureUsing(requireNonNull(closure)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	<S> DomainObjectProvider<T> configure(ModelType<S> type, Action<? super S> action);

	T get();
}

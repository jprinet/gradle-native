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
package dev.nokee.language.c.internal.plugins;

import dev.nokee.language.base.ConfigurableSourceSet;
import dev.nokee.language.base.internal.ModelBackedLanguageSourceSetLegacyMixIn;
import dev.nokee.language.base.internal.SourceProperty;
import dev.nokee.language.c.CSourceSet;
import dev.nokee.language.c.tasks.CCompile;
import dev.nokee.language.nativebase.internal.HeadersProperty;
import dev.nokee.language.nativebase.internal.NativeCompileTask;
import dev.nokee.language.nativebase.internal.NativeSourceSetLegacyTag;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.model.internal.type.ModelType;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.TaskProvider;

import static dev.nokee.model.internal.core.ModelProjections.createdUsing;

public final class RegisterCSourceSetProjectionRule {
	public static final class LegacyCSourceSetRule extends ModelActionWithInputs.ModelAction3<CSourceSetTag, NativeSourceSetLegacyTag, SourceProperty> {
		private final ObjectFactory objectFactory;

		public LegacyCSourceSetRule(ObjectFactory objectFactory) {
			this.objectFactory = objectFactory;
		}

		@Override
		protected void execute(ModelNode entity, CSourceSetTag cSourceSetSpec, NativeSourceSetLegacyTag nativeSourceSetLegacyTag, SourceProperty sourceProperty) {
			entity.addComponent(createdUsing(ModelType.of(CSourceSet.class), () -> objectFactory.newInstance(DefaultCSourceSet.class)));
		}
	}

	public static final class DefaultCSourceSetRule extends ModelActionWithInputs.ModelAction4<CSourceSetTag, SourceProperty, HeadersProperty, NativeCompileTask> {
		private final ObjectFactory objectFactory;

		public DefaultCSourceSetRule(ObjectFactory objectFactory) {
			this.objectFactory = objectFactory;
		}

		@Override
		protected void execute(ModelNode entity, CSourceSetTag cSourceSetSpec, SourceProperty sourceProperty, HeadersProperty headersProperty, NativeCompileTask compileTask) {
			entity.addComponent(createdUsing(ModelType.of(CSourceSet.class), () -> objectFactory.newInstance(DefaultCSourceSet.class)));
		}
	}

	public static class DefaultCSourceSet implements CSourceSet, ModelBackedLanguageSourceSetLegacyMixIn<CSourceSet> {
		public ConfigurableSourceSet getSource() {
			return ModelProperties.getProperty(this, "source").as(ConfigurableSourceSet.class).get();
		}

		public ConfigurableSourceSet getHeaders() {
			return ModelProperties.getProperty(this, "headers").as(ConfigurableSourceSet.class).get();
		}

		public TaskProvider<CCompile> getCompileTask() {
			return ModelProperties.getProperty(this, "compileTask").as(TaskProvider.class).get();
		}

		@Override
		public String toString() {
			return "C sources '" + getName() + "'";
		}
	}
}
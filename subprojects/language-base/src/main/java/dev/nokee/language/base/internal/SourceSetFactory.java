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
package dev.nokee.language.base.internal;

import dev.nokee.language.base.ConfigurableSourceSet;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.model.ObjectFactory;

public final class SourceSetFactory {
	private final ObjectFactory objectFactory;

	public SourceSetFactory(ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
	}

	public ConfigurableSourceSet sourceSet() {
		return new DefaultConfigurableSourceSet(new DefaultLanguageSourceSetStrategy(objectFactory));
	}

	public ConfigurableSourceSet bridgedSourceSet(SourceDirectorySet delegate) {
		return new DefaultConfigurableSourceSet(new BridgedLanguageSourceSetStrategy(delegate, objectFactory));
	}
}

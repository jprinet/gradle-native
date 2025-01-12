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
package dev.nokee.platform.ios.internal;

import dev.nokee.language.base.internal.HasConfigurableSourceMixIn;
import dev.nokee.language.base.internal.ModelBackedLanguageSourceSetLegacyMixIn;
import dev.nokee.platform.ios.IosResourceSet;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.TaskDependency;

public class IosResourceSetSpec implements IosResourceSet, HasPublicType, ModelBackedLanguageSourceSetLegacyMixIn<IosResourceSet>, HasConfigurableSourceMixIn {
	@Override
	public TaskDependency getBuildDependencies() {
		return getSource().getBuildDependencies();
	}

	@Override
	public TypeOf<?> getPublicType() {
		return TypeOf.typeOf(IosResourceSet.class);
	}
}

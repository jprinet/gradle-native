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
package dev.nokee.language.objectivec.internal.plugins;

import dev.nokee.language.base.internal.HasConfigurableSourceMixIn;
import dev.nokee.language.base.internal.ModelBackedLanguageSourceSetLegacyMixIn;
import dev.nokee.language.nativebase.internal.HasConfigurableHeadersMixIn;
import dev.nokee.language.nativebase.internal.HasNativeCompileTaskMixIn;
import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import dev.nokee.language.objectivec.internal.tasks.ObjectiveCCompileTask;

public class ObjectiveCSourceSetSpec implements ObjectiveCSourceSet, ModelBackedLanguageSourceSetLegacyMixIn<ObjectiveCSourceSet>, HasConfigurableSourceMixIn, HasConfigurableHeadersMixIn, HasNativeCompileTaskMixIn<ObjectiveCCompileTask> {
	@Override
	public String toString() {
		return "Objective-C sources '" + getName() + "'";
	}
}
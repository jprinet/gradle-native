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
package dev.nokee.language.nativebase.internal;

import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.nativebase.tasks.NativeSourceCompile;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.platform.base.internal.util.PropertyUtils;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.language.nativeplatform.tasks.AbstractNativeCompileTask;

import java.util.function.BiConsumer;

import static dev.nokee.platform.base.internal.util.PropertyUtils.from;
import static dev.nokee.platform.base.internal.util.PropertyUtils.wrap;

public final class AttachHeaderSearchPathsToCompileTaskRule extends ModelActionWithInputs.ModelAction4<LanguageSourceSetIdentifier, DependentHeaderSearchPaths, ProjectHeaderSearchPaths, NativeCompileTask> {
	private final LanguageSourceSetIdentifier identifier;

	public AttachHeaderSearchPathsToCompileTaskRule(LanguageSourceSetIdentifier identifier) {
		this.identifier = identifier;
	}

	@Override
	protected void execute(ModelNode entity, LanguageSourceSetIdentifier identifier, DependentHeaderSearchPaths incomingHeaders, ProjectHeaderSearchPaths userHeaders, NativeCompileTask compileTask) {
		if (identifier.equals(this.identifier)) {
			compileTask.configure(NativeSourceCompile.class, configureIncludeRoots(from(userHeaders).andThen(from(incomingHeaders))));
		}
	}

	//region Includes
	private static <SELF extends Task> Action<SELF> configureIncludeRoots(BiConsumer<? super SELF, ? super PropertyUtils.FileCollectionProperty> action) {
		return task -> action.accept(task, wrap(includesProperty(task)));
	}

	private static ConfigurableFileCollection includesProperty(Task task) {
		if (task instanceof AbstractNativeCompileTask) {
			return ((AbstractNativeCompileTask) task).getIncludes();
		} else {
			throw new IllegalArgumentException();
		}
	}
	//endregion
}
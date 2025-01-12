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
package dev.nokee.platform.nativebase;

import dev.nokee.platform.base.HasBaseName;
import dev.nokee.platform.nativebase.tasks.CreateStaticLibrary;
import org.gradle.api.Buildable;
import org.gradle.api.tasks.TaskProvider;

/**
 * A static library built from 1 or more native language.
 *
 * @since 0.4
 */
public interface StaticLibraryBinary extends NativeBinary, Buildable, HasBaseName {
	/**
	 * Returns a provider for the task that creates the static archive from the object files.
	 *
	 * @return a provider of {@link CreateStaticLibrary} task, never null.
	 */
	TaskProvider<CreateStaticLibrary> getCreateTask();
}

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
package dev.nokee.utils;

import com.google.common.testing.EqualsTester;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.specs.Spec;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.util.ConfigurationTestUtils.testConfiguration;
import static dev.nokee.utils.ConfigurationUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;

class ConfigurationUtils_DeclarableTest {
	@Test
	void onlyMatchDeclarableBucket() {
		assertAll(
			() -> assertThat(declarable().isSatisfiedBy(testConfiguration(configureAsConsumable())), is(false)),
			() -> assertThat(declarable().isSatisfiedBy(testConfiguration(configureAsResolvable())), is(false)),
			() -> assertThat(declarable().isSatisfiedBy(testConfiguration(configureAsDeclarable())), is(true))
		);
	}

	@Test
	void returnsEnhancedSpec() {
		assertThat(declarable(), isA(SpecUtils.Spec.class));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(declarable(), declarable())
			.addEqualityGroup(resolvable())
			.addEqualityGroup(consumable())
			.addEqualityGroup((Spec<Configuration>) it -> true)
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(declarable(), hasToString("ConfigurationUtils.declarable()"));
	}
}

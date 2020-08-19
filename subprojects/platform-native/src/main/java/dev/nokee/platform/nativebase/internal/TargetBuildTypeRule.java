package dev.nokee.platform.nativebase.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.runtime.nativebase.TargetBuildType;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.attributes.AttributeCompatibilityRule;
import org.gradle.api.attributes.AttributeDisambiguationRule;
import org.gradle.api.attributes.CompatibilityCheckDetails;
import org.gradle.api.attributes.MultipleCandidatesDetails;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.SetProperty;
import org.gradle.language.cpp.CppBinary;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Set;

public abstract class TargetBuildTypeRule implements Action<Project> {
	private final SetProperty<TargetBuildType> targetBuildTypes;
	private final String componentName;

	@Inject
	public TargetBuildTypeRule(SetProperty<TargetBuildType> targetBuildTypes, String componentName) {
		this.targetBuildTypes = targetBuildTypes;
		this.componentName = componentName;
		targetBuildTypes.convention(ImmutableList.of(DefaultTargetBuildTypeFactory.DEFAULT));

		getDependencies().getAttributesSchema().attribute(BaseTargetBuildType.BUILD_TYPE_ATTRIBUTE).getDisambiguationRules().add(BuildTypeSelectionRule.class);
	}

	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	protected abstract DependencyHandler getDependencies();

	static class BuildTypeSelectionRule implements AttributeDisambiguationRule<String> {
		@Override
		public void execute(MultipleCandidatesDetails<String> details) {
			// TODO: Should also include something that contains debug
			val firstMatchingDebugBuildType = details.getCandidateValues().stream().filter(it -> it.toLowerCase().equals("debug")).findFirst();
			firstMatchingDebugBuildType.ifPresent(details::closestMatch);
		}
	}

	@Override
	public void execute(Project project) {
		this.targetBuildTypes.disallowChanges();
		this.targetBuildTypes.finalizeValue();
		Set<TargetBuildType> targetBuildTypes = this.targetBuildTypes.get();
		assertNonEmpty(targetBuildTypes, "target build types", componentName);
	}

	private static void assertNonEmpty(Collection<?> values, String propertyName, String componentName) {
		if (values.isEmpty()) {
			throw new IllegalArgumentException(String.format("A %s needs to be specified for the %s.", propertyName, componentName));
		}
	}
}

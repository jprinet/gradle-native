package dev.nokee.platform.nativebase

import dev.nokee.fixtures.AbstractComponentDependenciesIntegrationTest
import dev.nokee.fixtures.AbstractLocalDarwinFrameworkDependenciesIntegrationTest
import dev.nokee.platform.base.internal.NamingScheme
import dev.nokee.platform.base.internal.dependencies.ConfigurationFactories
import dev.nokee.platform.base.internal.dependencies.DefaultComponentDependencies
import dev.nokee.platform.base.internal.dependencies.DefaultDependencyBucketFactory
import dev.nokee.platform.base.internal.dependencies.DefaultDependencyFactory
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeComponentDependencies
import dev.nokee.platform.nativebase.internal.dependencies.FrameworkAwareDependencyBucketFactory
import spock.lang.Subject

@Subject(DefaultNativeComponentDependencies)
class DefaultNativeComponentDependenciesIntegrationTest extends AbstractComponentDependenciesIntegrationTest {
	@Override
	protected newDependencies(NamingScheme names) {
		def dependencyContainer = project.objects.newInstance(DefaultComponentDependencies, "test component", new FrameworkAwareDependencyBucketFactory(new DefaultDependencyBucketFactory(new ConfigurationFactories.Prefixing(new ConfigurationFactories.Creating(project.configurations), { target -> names.getConfigurationName(target) }), new DefaultDependencyFactory(project.dependencies))))
		return project.objects.newInstance(DefaultNativeComponentDependencies, dependencyContainer)
	}

	@Override
	protected List<String> getBucketsUnderTest() {
		return ['implementation', 'linkOnly', 'runtimeOnly', 'compileOnly']
	}
}

@Subject(DefaultNativeComponentDependencies)
class DefaultNativeComponentDependenciesLocalDarwinFrameworkIntegrationTest extends AbstractLocalDarwinFrameworkDependenciesIntegrationTest {
	@Override
	protected newDependencies(NamingScheme names) {
		def dependencyContainer = project.objects.newInstance(DefaultComponentDependencies, "test component", new FrameworkAwareDependencyBucketFactory(new DefaultDependencyBucketFactory(new ConfigurationFactories.Prefixing(new ConfigurationFactories.Creating(project.configurations), { target -> names.getConfigurationName(target) }), new DefaultDependencyFactory(project.dependencies))))
		return project.objects.newInstance(DefaultNativeComponentDependencies, dependencyContainer)
	}

	@Override
	protected List<String> getBucketsUnderTest() {
		return ['implementation', 'linkOnly', 'runtimeOnly', 'compileOnly']
	}
}

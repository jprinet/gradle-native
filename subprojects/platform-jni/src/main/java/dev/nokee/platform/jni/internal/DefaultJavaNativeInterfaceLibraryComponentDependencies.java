package dev.nokee.platform.jni.internal;

import dev.nokee.platform.base.ComponentDependencies;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.dependencies.ComponentDependenciesInternal;
import dev.nokee.platform.jni.JavaNativeInterfaceLibraryComponentDependencies;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleDependency;

import javax.inject.Inject;

import static dev.nokee.platform.nativebase.internal.dependencies.ConfigurationUtilsEx.configureAsBucket;

public class DefaultJavaNativeInterfaceLibraryComponentDependencies extends DefaultJavaNativeInterfaceNativeComponentDependencies implements JavaNativeInterfaceLibraryComponentDependencies, ComponentDependencies {
	@Getter private final DependencyBucket api;
	@Getter private final DependencyBucket jvmImplementation;
	@Getter private final DependencyBucket jvmRuntimeOnly;

	@Inject
	public DefaultJavaNativeInterfaceLibraryComponentDependencies(ComponentDependenciesInternal delegate) {
		super(delegate);
		this.api = delegate.create("api", this::configureApiConfiguration);
		this.jvmImplementation = delegate.create("jvmImplementation", this::configureImplementationConfiguration);
		this.jvmRuntimeOnly = delegate.create("jvmRuntimeOnly", this::configureRuntimeOnlyConfiguration);
	}

	private void configureApiConfiguration(Configuration configuration) {
		configureAsBucket(configuration);
		configuration.setDescription(String.format("API dependencies for %s.", getComponentDisplayName()));
	}

	private void configureImplementationConfiguration(Configuration configuration) {
		configureAsBucket(configuration);
		configuration.extendsFrom(api.getAsConfiguration());
		configuration.setDescription(String.format("Implementation only dependencies for %s.", getComponentDisplayName()));
	}

	private void configureRuntimeOnlyConfiguration(Configuration configuration) {
		configureAsBucket(configuration);
		configuration.extendsFrom(jvmImplementation.getAsConfiguration());
		configuration.setDescription(String.format("Runtime only dependencies for %s.", getComponentDisplayName()));
	}

	@Override
	public void api(Object notation) {
		api.addDependency(notation);
	}

	@Override
	public void api(Object notation, Action<? super ModuleDependency> action) {
		api.addDependency(notation, action);
	}

	@Override
	public void jvmImplementation(Object notation) {
		jvmImplementation.addDependency(notation);
	}

	@Override
	public void jvmImplementation(Object notation, Action<? super ModuleDependency> action) {
		jvmImplementation.addDependency(notation, action);
	}

	@Override
	public void jvmRuntimeOnly(Object notation) {
		jvmRuntimeOnly.addDependency(notation);
	}

	@Override
	public void jvmRuntimeOnly(Object notation, Action<? super ModuleDependency> action) {
		jvmRuntimeOnly.addDependency(notation, action);
	}
}

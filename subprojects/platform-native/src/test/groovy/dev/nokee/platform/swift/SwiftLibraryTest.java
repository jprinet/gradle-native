package dev.nokee.platform.swift;

import dev.nokee.internal.testing.utils.TestUtils;
import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.platform.base.testers.SourceAwareComponentTester;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.io.TempDir;
import spock.lang.Subject;

import java.io.File;
import java.util.stream.Stream;

import static dev.nokee.model.fixtures.ModelRegistryTestUtils.create;
import static dev.nokee.model.fixtures.ModelRegistryTestUtils.registry;
import static dev.nokee.platform.swift.internal.plugins.SwiftLibraryPlugin.swiftLibrary;

@Subject(SwiftLibrary.class)
public class SwiftLibraryTest implements SourceAwareComponentTester<SwiftLibrary> {
	@Getter @TempDir File testDirectory;

	@Override
	public SwiftLibrary createSubject(String componentName) {
		val project = TestUtils.createRootProject(testDirectory);
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		val component = create(registry(project.getObjects()), swiftLibrary(componentName, project));
		((FunctionalSourceSet) component.getSources()).get(); // force realize
		return component;
	}

	@Override
	public Stream<SourcesUnderTest> provideSourceSetUnderTest() {
		return Stream.of(new SourcesUnderTest("swift", SwiftSourceSet.class, "swiftSources"));
	}
}

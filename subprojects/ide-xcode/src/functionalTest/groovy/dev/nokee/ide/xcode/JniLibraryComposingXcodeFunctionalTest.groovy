package dev.nokee.ide.xcode

import dev.nokee.platform.jni.JvmJarBinary
import dev.nokee.platform.jni.fixtures.JavaJniObjectiveCGreeterLib
import dev.nokee.platform.nativebase.SharedLibraryBinary
import dev.nokee.platform.nativebase.internal.SharedLibraryBinaryInternal
import org.gradle.internal.os.OperatingSystem

class JniLibraryComposingXcodeFunctionalTest extends AbstractXcodeIdeFunctionalSpec implements JavaObjectiveCJniLibraryXcodeIdeFixture {
	protected void makeSingleProject() {
		settingsFile << "rootProject.name = 'jni-greeter'"
		buildFile << """
			plugins {
				id 'dev.nokee.xcode-ide'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.objective-c-language'
				id 'java'
			}

			import ${JvmJarBinary.canonicalName}
			import ${SharedLibraryBinary.canonicalName}
			import ${SharedLibraryBinaryInternal.canonicalName} // TODO: Remove this requirement

			library {
				binaries.configureEach(${SharedLibraryBinary.simpleName}) {
					linkTask.configure {
						linkerArgs.add('-lobjc')
					}
				}
			}
		"""
	}

	protected JavaJniObjectiveCGreeterLib getComponentUnderTest() {
		return component
	}

	def "can compose a Xcode workspace for a JNI library"() {
		given:
		settingsFile << configurePluginClasspathAsBuildScriptDependencies()
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)
		buildFile << """
			xcode {
				projects.register('jni-greeter') {
					${registerJniGreeterTarget()}
					${registerJniSharedLibraryTarget()}
				}
			}
		"""

		when:
		succeeds('xcode')

		then:
		result.assertTasksExecutedAndNotSkipped(':jni-greeterXcodeProject', ':xcodeWorkspace', ':xcode')
		xcodeWorkspace('jni-greeter').assertHasProjects('jni-greeter.xcodeproj')
		xcodeProject('jni-greeter').assertHasTarget('JniGreeter')
		xcodeProject('jni-greeter').assertHasTarget('JniSharedLibrary')
		xcodeProject('jni-greeter').assertHasSchemes('JniGreeter', 'JniSharedLibrary')
		xcodeProject('jni-greeter').assertHasSourceLayout('JniGreeter/Greeter.java', 'JniGreeter/NativeLoader.java', 'JniSharedLibrary/greeter.h', 'JniSharedLibrary/greeter.m', 'JniSharedLibrary/greeter_impl.m', 'Products/jni-greeter.jar', sharedLibraryName('Products/jni-greeter'), 'build.gradle', 'settings.gradle')
	}

	def "creates an indexer target for known product type"() {
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)
		buildFile << """
			xcode {
				projects.register('jni-greeter') {
					${registerJniGreeterTarget()}
					${registerJniSharedLibraryTarget()}
				}
			}
		"""

		when:
		succeeds('xcode')

		then:
		result.assertTasksExecutedAndNotSkipped(':jni-greeterXcodeProject', ':xcodeWorkspace', ':xcode')
		xcodeProject('jni-greeter').assertHasTargets('JniGreeter', 'JniSharedLibrary', '__indexer_JniSharedLibrary')
	}

	def "can create multiple Xcode projects per Gradle project"() {
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)
		buildFile << """
			xcode {
				projects.register('jni-greeter') {
					${registerJniGreeterTarget()}
				}
				projects.register('jni-shared-library') {
					${registerJniSharedLibraryTarget()}
				}
			}
		"""

		when:
		succeeds('xcode')

		then:
		result.assertTasksExecutedAndNotSkipped(':jni-greeterXcodeProject', ':jni-shared-libraryXcodeProject', ':xcodeWorkspace', ':xcode')
		xcodeWorkspace('jni-greeter').assertHasProjects('jni-greeter.xcodeproj', 'jni-shared-library.xcodeproj')

		and:
		xcodeProject('jni-greeter').assertHasTarget('JniGreeter')
		xcodeProject('jni-greeter').assertHasSchemes('JniGreeter')
		xcodeProject('jni-greeter').assertHasSourceLayout('JniGreeter/Greeter.java', 'JniGreeter/NativeLoader.java', 'Products/jni-greeter.jar', 'build.gradle', 'settings.gradle')

		and:
		xcodeProject('jni-shared-library').assertHasTarget('JniSharedLibrary')
		xcodeProject('jni-shared-library').assertHasSchemes('JniSharedLibrary')
		xcodeProject('jni-shared-library').assertHasSourceLayout('JniSharedLibrary/greeter.h', 'JniSharedLibrary/greeter.m', 'JniSharedLibrary/greeter_impl.m', sharedLibraryName('Products/jni-greeter'), 'build.gradle', 'settings.gradle')
	}

	def "creates indexer target inside the correct Xcode project when multiple projects"() {
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)
		buildFile << """
			xcode {
				projects.register('jni-greeter') {
					${registerJniGreeterTarget()}
				}
				projects.register('jni-shared-library') {
					${registerJniSharedLibraryTarget()}
				}
			}
		"""

		when:
		succeeds('xcode')

		then:
		result.assertTasksExecutedAndNotSkipped(':jni-greeterXcodeProject', ':jni-shared-libraryXcodeProject', ':xcodeWorkspace', ':xcode')
		xcodeProject('jni-greeter').assertHasTargets('JniGreeter')
		xcodeProject('jni-shared-library').assertHasTargets('JniSharedLibrary', '__indexer_JniSharedLibrary')
	}

	// TODO: Create a GradleNativeFixture trait to includes all those utility methods.
	String sharedLibraryName(Object path) {
		return OperatingSystem.current().getSharedLibraryName(path.toString())
	}

	// TODO: Can create build configuration inside project
	// TODO: Can have 2 target with unequal build configuration
	// TODO: Source of target are included inside it's own PBXGroup

	// TODO: Can apply/inject xcode-ide plugin on source dependencies (basically applying the plugin to settings)
	// TODO: Can apply plugin to settings which apply to all projects... CAN we inject inside composite build...

	// TODO: __xcode_* bridge methods copied the files to the BUILT_PRODUCT_DIR
	// TODO: Can clean Xcode project by delegating to Gradle
}

package dev.nokee.platform.swift

import dev.gradleplugins.integtests.fixtures.nativeplatform.RequiresInstalledToolChain
import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement
import dev.gradleplugins.test.fixtures.file.TestFile
import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.nokee.fixtures.AbstractNativeComponentBuildTypeFunctionalTest
import dev.nokee.language.swift.SwiftTaskNames
import dev.nokee.platform.jni.fixtures.elements.GreeterImplementationAwareSourceElement
import dev.nokee.platform.jni.fixtures.elements.SwiftGreeter
import dev.nokee.platform.nativebase.fixtures.CCompileGreeter
import dev.nokee.platform.nativebase.fixtures.CGreeterApp
import dev.nokee.platform.nativebase.fixtures.SwiftCompileGreeter
import dev.nokee.platform.nativebase.fixtures.SwiftGreeterApp
import dev.nokee.platform.nativebase.fixtures.SwiftGreeterLib

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftApplicationBuildTypeFunctionalTest extends AbstractNativeComponentBuildTypeFunctionalTest implements SwiftTaskNames {
	@Override
	protected void makeSingleProject() {
		settingsFile << 'rootProject.name = "app"'
		buildFile << '''
			plugins {
				id 'dev.nokee.swift-application'
			}
		'''
	}

	@Override
	protected void makeMultiProject() {
		makeMultiProjectWithoutDependency()
		buildFile << '''
			application {
				dependencies {
					implementation project(':library')
				}
			}
		'''
	}

	@Override
	protected void makeMultiProjectWithoutDependency() {
		settingsFile << '''
			rootProject.name = "app"
			include 'library'
		'''
		buildFile << '''
			plugins {
				id 'dev.nokee.swift-application'
			}
		'''
		file('library', buildFileName) << '''
			plugins {
				id 'dev.nokee.swift-library'
			}
		'''
	}

	@Override
	protected ComponentUnderTest getComponentUnderTest() {
		return new AbstractNativeComponentBuildTypeFunctionalTest.ComponentUnderTest() {
			@Override
			void writeToProject(TestFile projectDirectory) {
				new SwiftGreeterApp().writeToProject(projectDirectory)
			}

			@Override
			SourceElement withImplementationAsSubproject(String subprojectPath) {
				return new SwiftGreeterApp().withImplementationAsSubproject(subprojectPath)
			}

			@Override
			SourceElement withPreprocessorImplementation() {
				return new SwiftCompileGreeter()
			}
		}
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftLibraryBuildTypeFunctionalTest extends AbstractNativeComponentBuildTypeFunctionalTest implements SwiftTaskNames {
	@Override
	protected void makeSingleProject() {
		settingsFile << 'rootProject.name = "lib"'
		buildFile << '''
			plugins {
				id 'dev.nokee.swift-library'
			}
		'''
	}

	@Override
	protected void makeMultiProjectWithoutDependency() {
		settingsFile << '''
			rootProject.name = "lib"
			include 'library'
		'''
		buildFile << '''
			plugins {
				id 'dev.nokee.swift-library'
			}
		'''
		file('library', buildFileName) << '''
			plugins {
				id 'dev.nokee.swift-library'
			}
		'''
	}

	@Override
	protected ComponentUnderTest getComponentUnderTest() {
		return new AbstractNativeComponentBuildTypeFunctionalTest.ComponentUnderTest() {
			@Override
			void writeToProject(TestFile projectDirectory) {
				new SwiftGreeterLib().writeToProject(projectDirectory)
			}

			@Override
			SourceElement withImplementationAsSubproject(String subprojectPath) {
				return new SwiftGreeterLib().withImplementationAsSubproject(subprojectPath)
			}

			@Override
			SourceElement withPreprocessorImplementation() {
				return new SwiftCompileGreeter()
			}
		}
	}
}

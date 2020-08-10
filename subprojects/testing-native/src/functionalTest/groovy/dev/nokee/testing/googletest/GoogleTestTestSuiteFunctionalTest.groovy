package dev.nokee.testing.googletest

import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.nokee.language.cpp.CppTaskNames
import dev.nokee.platform.nativebase.ExecutableBinary
import dev.nokee.platform.nativebase.fixtures.CppGreeterLib
import dev.nokee.platform.nativebase.fixtures.GoogleTestGreeterTest
import dev.nokee.testing.nativebase.NativeTestSuite

class GoogleTestTestSuiteFunctionalTest extends AbstractInstalledToolChainIntegrationSpec implements CppTaskNames {
	def "can test using Google Test built from source"() {
		given:
		settingsFile << '''
			sourceControl {
				gitRepository("https://github.com/google/googletest") {
					producesModule("com.github.google.googletest:gtest")
					producesModule("com.github.google.googletest:gtest_main")
					plugins {
						id 'dev.nokee.cmake-build-adapter'
					}
				}
			}
		'''
		buildFile << """
			plugins {
				id 'dev.nokee.cpp-library'
				id 'dev.nokee.native-unit-testing'
			}

			import ${NativeTestSuite.canonicalName}
			import ${ExecutableBinary.canonicalName}

			testSuites {
				test(${NativeTestSuite.simpleName}) {
					testedComponent library
					dependencies {
						implementation 'com.github.google.googletest:gtest:latest.integration'
						implementation 'com.github.google.googletest:gtest_main:latest.integration'
					}
					binaries.configureEach(${ExecutableBinary.simpleName}) {
						compileTasks.configureEach {
							compilerArgs.addAll(toolChain.map {
								if (it in Gcc || it in Clang) {
									return ['-std=c++17']
								}
								return []
							})
						}
						linkTask.configure {
							linkerArgs.addAll(targetPlatform.map {
								if (it.operatingSystem.linux) {
									return ['-lpthread']
								} else if (it.operatingSystem.windows) {
									return ['/SUBSYSTEM:CONSOLE']
								}
								return []
							})
						}
					}
				}
			}
		"""

		new GoogleTestGreeterTest().writeToProject(testDirectory)
		new CppGreeterLib().writeToProject(testDirectory)

		when:
		// Because of https://github.com/gradle/gradle/issues/13793
		executer = executer.withoutDeprecationChecks().requireIsolatedDaemons()
		succeeds('test')

		then:
		result.assertTasksExecuted(tasks.compile, tasks.withComponentName('test').forSharedLibrary.allToTest, ':googletest:gtest:make', ':googletest:gtest_main:make')
	}

	// TODO: Test failing test suite
}

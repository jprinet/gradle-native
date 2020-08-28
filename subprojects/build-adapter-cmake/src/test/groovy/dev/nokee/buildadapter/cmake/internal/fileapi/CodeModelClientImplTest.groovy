package dev.nokee.buildadapter.cmake.internal.fileapi

import dev.nokee.core.exec.CommandLineTool
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class CodeModelClientImplTest extends Specification {
	@Rule
	TemporaryFolder temporaryFolder = new TemporaryFolder()

	def "can query codemodel"() {
		given:
		temporaryFolder.newFile('CMakeLists.txt') << '''
			cmake_minimum_required(VERSION 3.10)
		'''
		def subject = new CodeModelClientImpl({ CommandLineTool.of('cmake')})

		when:
		subject.query(temporaryFolder.root)

		then:
		noExceptionThrown()
	}

	def "can read files"() {
		given:
		def testSetDirectory = getReplyFilesOf('msvc2019-googletest')
		def subject = CodeModelReplyFiles.of(testSetDirectory)

		when:
		def visitedIndex = null
		def visitedCodeModel = null
		def visitedTargets = []
		subject.visit(new CodeModelReplyFiles.Visitor() {
			@Override
			void visit(CodeModelReplyFile codeModel) {
				visitedCodeModel = codeModel.get()
			}

			@Override
			void visit(CodeModelTargetReplyFile codeModelTarget) {
				visitedTargets << codeModelTarget.get()
			}

			@Override
			void visit(IndexReplyFile replyIndex) {
				visitedIndex = replyIndex.get()
			}
		})

		then:
		visitedTargets.size() == 32
		visitedTargets*.name as Set == ['ZERO_CHECK', 'gtest_main', 'gtest', 'gmock_main', 'gmock', 'ALL_BUILD'] as Set
		visitedIndex.objects.size() == 1
		visitedIndex.objects[0].version.major == 2
		visitedIndex.objects[0].version.minor == 1
	}

	private static File getReplyFilesOf(String testSetName) {
		def replyDirectory = System.getProperty('dev.nokee.cmake-replies')
		assert replyDirectory != null
		return new File(replyDirectory, testSetName)
	}
}

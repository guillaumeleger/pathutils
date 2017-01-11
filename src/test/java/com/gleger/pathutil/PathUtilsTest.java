package com.gleger.pathutil;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PathUtilsTest {
	
	private Path m_testFile;
	private Path m_testDir;
	
	@Before
	public void setUpTestDir() throws IOException, URISyntaxException {
		m_testDir = Files.createTempDirectory("PathUtilsTest" + Instant.now().toEpochMilli());
		m_testFile = Paths.get(PathUtilsTest.class.getResource("TestFile.txt").toURI());
	}
	
	@Test
	public void testEmptyFile() throws IOException {
		Assert.assertTrue(PathUtils.isEmptyDirectory(Files.createTempDirectory("")));
	}
	
	@Test 
	public void testCopyTestDirectory() throws Exception {
		Path testDir = Paths.get(PathUtilsTest.class.getResource("testDir").toURI());
		PathUtils.copyDirectoryToDirectory(testDir, m_testDir);
		Assert.assertFalse("Copy should not delete source dir", PathUtils.isEmptyDirectory(testDir));
		Assert.assertFalse("Copy should not delete source dir", PathUtils.isEmptyDirectory(m_testDir));
		Assert.assertEquals(3, PathUtils.listFilesInDirectory(m_testDir).size());
		Assert.assertEquals(3, PathUtils.listFilesInDirectory(testDir).size());
	}
	
	@Test
	public void testCopyToReadOnlyDirectoryThrowsExecption() throws IOException, URISyntaxException {
		Path testDir = Paths.get(PathUtilsTest.class.getResource("testDir").toURI());
		Files.setAttribute(m_testDir, "", ""); //TODO: how to make a file/directory not writable
		try {
			PathUtils.copyDirectoryToDirectory(testDir, m_testDir);
			Assert.fail("Cant copy to non-writable location");
		} catch (IOException e) {
			
		} finally {
			//TODO: undo read only
		}
		Assert.assertTrue(PathUtils.isEmptyDirectory(m_testDir));
	}
	
	@Test
	public void testCopyToFileThrowsException() throws URISyntaxException, IOException {
		Path testDir = Paths.get(PathUtilsTest.class.getResource("testDir").toURI());
		Files.createFile(m_testDir.resolve("testFileInDir1"));
		try {
			PathUtils.copyDirectoryToDirectory(testDir, m_testDir);
			Assert.fail("Cant overwrite to files while copying");
		} catch(IOException e) {
			
		}
	}
	
	@After
	public void tearDown() throws IOException {
		PathUtils.deleteDirectoryRecursively(m_testDir);
	}
	
}

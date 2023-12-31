package funaselint.linter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class IgnoreProcessorTest {

    @TempDir
    Path testDirectory;
    IgnoreProcessor processor;

    @BeforeEach
    public void setUp() {
        processor = new IgnoreProcessor(testDirectory);
    }

    private void createFile(String path) throws IOException {
        Files.createFile(testDirectory.resolve(path));
    }

    private void createDirectory(String dirName) throws IOException {
        Files.createDirectory(testDirectory.resolve(dirName));
    }

    private void createFunaseignoreFile(String dirName, String ignoreContent) throws IOException {
        Files.write(testDirectory.resolve(dirName).resolve(".funaseignore"), ignoreContent.getBytes());
    }

    @Test
    public void testSubdirectoryFunaseignore() throws IOException {
        createDirectory("subdir");
        createFunaseignoreFile("subdir", "subdirPptx1.pptx\n");
        createFile("subdir/subdirPptx1.pptx");
        createFile("subdir/subdirPptx2.pptx");

        Set<Path> lintableFiles = processor.findFilesToLint();
        assertFalse(lintableFiles.contains(testDirectory.resolve("subdir/subdirPptx1.pptx")),
                "subdirPptx1.pptx in subdir should not be lintable");
        assertTrue(lintableFiles.contains(testDirectory.resolve("subdir/subdirPptx2.pptx")),
                "subdirPptx2.pptx in subdir should be lintable");
    }

    @Test
    public void testMultipleFunaseignoreFiles() throws IOException {
        createDirectory("dir1");
        createFunaseignoreFile("dir1", "dir1Pptx.pptx\n");
        createFile("dir1/dir1Pptx.pptx");

        createDirectory("dir2");
        createFile("dir2/dir2Pptx.pptx");

        Set<Path> lintableFiles = processor.findFilesToLint();
        assertFalse(lintableFiles.contains(testDirectory.resolve("dir1/dir1Pptx.pptx")),
                "dir1Pptx.pptx in dir1 should not be lintable");
        assertTrue(lintableFiles.contains(testDirectory.resolve("dir2/dir2Pptx.pptx")),
                "dir2Pptx.pptx in dir2 should be lintable");
    }

    @Test
    public void testNestedDirectoryFunaseignore() throws IOException {
        createDirectory("nested");
        createDirectory("nested/inner");
        createFunaseignoreFile("nested", "inner/\n");
        createFile("nested/inner/innerPptx.pptx");

        Set<Path> lintableFiles = processor.findFilesToLint();
        assertFalse(lintableFiles.contains(testDirectory.resolve("nested/inner/innerPptx.pptx")),
                "innerPptx.pptx in nested/inner should not be lintable");
    }

    @Test
    public void testNoFunaseignoreFile() throws IOException {
        createDirectory("noIgnoreDir");
        createFile("noIgnoreDir/noIgnorePptx.pptx");

        Set<Path> lintableFiles = processor.findFilesToLint();
        assertTrue(lintableFiles.contains(testDirectory.resolve("noIgnoreDir/noIgnorePptx.pptx")),
                "noIgnorePptx.pptx in noIgnoreDir should be lintable");
    }

    @Test
    public void testOverridingFunaseignoreFile() throws IOException {
        createDirectory("nested");
        createDirectory("nested/inner");
        createFunaseignoreFile("nested/inner", "!innerpptx.pptx\n");
        createFile("nested/inner/innerPptx.pptx");

        Set<Path> lintableFiles = processor.findFilesToLint();
        assertTrue(lintableFiles.contains(testDirectory.resolve("nested/inner/innerPptx.pptx")),
                "innerPptx.pptx in nested/inner should be lintable despite parent ignore");
    }

    @Test
    public void testEmptyFunaseignoreFile() throws IOException {
        createDirectory("emptyIgnoreDir");
        createFunaseignoreFile("emptyIgnoreDir", "");
        createFile("emptyIgnoreDir/fileInEmptyIgnoreDir.pptx");

        Set<Path> lintableFiles = processor.findFilesToLint();
        assertTrue(lintableFiles.contains(testDirectory.resolve("emptyIgnoreDir/fileInEmptyIgnoreDir.pptx")),
                "fileInEmptyIgnoreDir.pptx in emptyIgnoreDir should be lintable");
    }
}

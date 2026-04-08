package com.amartha.loan.infrastructure.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceTest {

    private FileStorageService service;
    private Path uploadDir;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        uploadDir = tempDir;
        service = new FileStorageService(uploadDir.toString());
    }

    @Test
    void storeFile_withValidData_returns_storedPath() throws IOException {
        byte[] content = "test content".getBytes();
        String originalFilename = "test.txt";

        String storedPath = service.storeFile(content, originalFilename);

        assertNotNull(storedPath);
        assertTrue(storedPath.contains("test.txt"));
    }

    @Test
    void storeFile_createsUploadDirectory_ifNotExists() throws IOException {
        Path nonExistentDir = uploadDir.resolve("new_dir");
        FileStorageService svc = new FileStorageService(nonExistentDir.toString());

        byte[] content = "test".getBytes();
        String path = svc.storeFile(content, "file.txt");

        assertNotNull(path);
        assertTrue(Files.exists(nonExistentDir));
    }

    @Test
    void retrieveFile_withExistingPath_returns_bytes() throws IOException {
        byte[] originalContent = "test content".getBytes();
        String storedPath = service.storeFile(originalContent, "test.txt");

        byte[] retrievedContent = service.retrieveFile(storedPath);

        assertArrayEquals(originalContent, retrievedContent);
    }

    @Test
    void retrieveFile_withInvalidPath_throws_IOException() {
        assertThrows(IOException.class, () -> service.retrieveFile("/nonexistent/path/file.txt"));
    }
}

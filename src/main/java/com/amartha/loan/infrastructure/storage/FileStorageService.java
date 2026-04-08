package com.amartha.loan.infrastructure.storage;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@ApplicationScoped
public class FileStorageService {

    private final String uploadDir;

    public FileStorageService(@ConfigProperty(name = "loan.storage.upload-dir") String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public String storeFile(byte[] fileContent, String originalFilename) throws IOException {
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String storedFilename = UUID.randomUUID() + "_" + originalFilename;
        Path filePath = uploadPath.resolve(storedFilename);

        Files.write(filePath, fileContent);
        return filePath.toString();
    }

    public byte[] retrieveFile(String storedPath) throws IOException {
        Path filePath = Paths.get(storedPath);
        if (!Files.exists(filePath)) {
            throw new IOException("File not found: " + storedPath);
        }
        return Files.readAllBytes(filePath);
    }
}

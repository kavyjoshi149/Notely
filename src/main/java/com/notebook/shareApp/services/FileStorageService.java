package com.notebook.shareApp.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/** Stores uploaded PDFs on local disk under a UUID name (swap for S3/Cloudinary before production). */
@Service
public class FileStorageService {

    private final Path root;

    public FileStorageService(@Value("${app.upload-dir:uploads}") String uploadDir) throws IOException {
        this.root = Path.of(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(root);
    }

    /** Saves the PDF and returns the stored file name (what goes into Note.filePath). */
    public String storePdf(MultipartFile file) throws IOException {
        String stored = UUID.randomUUID() + ".pdf";
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, root.resolve(stored), StandardCopyOption.REPLACE_EXISTING);
        }
        return stored;
    }

    public Resource load(String storedName) throws IOException {
        Path file = root.resolve(storedName).normalize();
        if (!file.startsWith(root) || !Files.exists(file)) {
            throw new IOException("File not found");
        }
        return new UrlResource(file.toUri());
    }
}

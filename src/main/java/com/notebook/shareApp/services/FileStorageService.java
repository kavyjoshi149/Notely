package com.notebook.shareApp.services;


import com.notebook.shareApp.Configuration.StorageProperties;
import com.notebook.shareApp.util.FileUtil;
import com.notebook.shareApp.util.InvalidFileException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Saves and loads PDFs on local disk behind one interface. Everything above this class
 * (NoteService, controllers) only knows "store a file, get back a name" / "load this name" —
 * swapping local disk for S3 or Cloudflare R2 later means changing only this one class.
 */
@Service
@EnableConfigurationProperties(StorageProperties.class)
public class FileStorageService {

    private final Path root;

    public FileStorageService(StorageProperties properties) {
        this.root = Paths.get(properties.getDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create upload directory: " + root, e);
        }
    }

    /** Validates the file, then stores it under a random name. Returns the stored file name
     *  (not the original name) — that's what NoteService should save as Note.filePath. */
    public String store(MultipartFile file) {
        FileUtil.validatePdf(file);
        String storedName = FileUtil.generateStoredName();
        Path target = root.resolve(storedName);

        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new InvalidFileException("Could not save the file. Please try again.");
        }
        return storedName;
    }

    /** Loads a previously stored file as a Resource, ready to stream in a controller response. */
    public Resource load(String storedName) {
        try {
            Path file = root.resolve(storedName).normalize();
            if (!file.startsWith(root)) {
                // storedName tried to escape the upload folder (e.g. "../../etc/passwd")
                throw new InvalidFileException("Invalid file reference.");
            }
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new InvalidFileException("File not found.");
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new InvalidFileException("Invalid file reference.");
        }
    }

    /** Deletes a stored file. Called when a note is removed; ignores a file that's already gone. */
    public void delete(String storedName) {
        try {
            Path file = root.resolve(storedName).normalize();
            if (file.startsWith(root)) {
                Files.deleteIfExists(file);
            }
        } catch (IOException e) {
            // log and move on — a leftover orphaned file on disk is not worth failing the request for
        }
    }
}


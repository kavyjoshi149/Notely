package com.notebook.shareApp.util;


import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Stateless helpers for validating and naming uploaded PDFs.
 * Never trust a file's extension or browser-supplied content-type alone —
 * both are just text the client sent and can be changed to anything.
 * The real check is the file's first bytes, its "magic number".
 */
public final class FileUtil {

    private static final byte[] PDF_MAGIC = {'%', 'P', 'D', 'F'};
    private static final long MAX_SIZE_BYTES = 15L * 1024 * 1024; // keep in sync with application.properties

    private FileUtil() {
    }

    /** Throws InvalidFileException if the file is missing, empty, oversized, or not really a PDF. */
    public static void validatePdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Please attach a PDF file.");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new InvalidFileException("File is larger than 15 MB.");
        }
        if (!hasPdfMagicBytes(file)) {
            throw new InvalidFileException("The file is not a valid PDF.");
        }
    }

    private static boolean hasPdfMagicBytes(MultipartFile file) {
        byte[] header = new byte[4];
        try (InputStream in = file.getInputStream()) {
            int read = in.read(header);
            if (read < 4) {
                return false;
            }
        } catch (IOException e) {
            return false;
        }
        for (int i = 0; i < PDF_MAGIC.length; i++) {
            if (header[i] != PDF_MAGIC[i]) {
                return false;
            }
        }
        return true;
    }

    /** A random name to store the file under, so two students' "notes.pdf" never collide and the
     *  original filename (which could contain path characters) never reaches the filesystem. */
    public static String generateStoredName() {
        return UUID.randomUUID() + ".pdf";
    }

    /** Keep the extension check separate from the magic-byte check: this is only used for a
     *  friendly, early error message before the browser even uploads the bytes (client side). */
    public static boolean hasPdfExtension(String originalFilename) {
        return originalFilename != null && originalFilename.toLowerCase().endsWith(".pdf");
    }
}

package com.notebook.shareApp.Configuration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Binds the app.upload.dir property from application.properties. */
@ConfigurationProperties(prefix = "app.upload")
public class StorageProperties {

    private String dir = "uploads/notes";

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }
}
package org.example.config;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;


public class ImageStorage {

    private static final Path IMAGE_BASE = Paths.get(System.getProperty("user.dir"), "image");

    static {
        try {
            Files.createDirectories(IMAGE_BASE);
        } catch (IOException e) {
            throw new RuntimeException("canät open this image: " + IMAGE_BASE, e);
        }
    }


    public static Path saveImage(File sourceFile) throws IOException {
        Path target = IMAGE_BASE.resolve(sourceFile.getName());
        return Files.copy(sourceFile.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
    }


    public static Path saveImage(InputStream data, String fileName) throws IOException {
        Path target = IMAGE_BASE.resolve(fileName);
        Files.copy(data, target, StandardCopyOption.REPLACE_EXISTING);
        return target;
    }
}
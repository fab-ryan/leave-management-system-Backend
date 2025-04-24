package com.example.leave_management.service.impl;

import com.example.leave_management.exception.AppException;
import com.example.leave_management.service.FileStorageService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    private Path rootLocation;

    @Override
    public void init() {
        try {
            rootLocation = Paths.get("./uploads");
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new AppException("Could not initialize storage", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public String store(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new AppException("Failed to store empty file", HttpStatus.BAD_REQUEST);
            }
            if (rootLocation == null) {
                init();
            }

            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, rootLocation.resolve(filename),
                        StandardCopyOption.REPLACE_EXISTING);
            }

            return filename;
        } catch (IOException e) {
            throw new AppException("Failed to store file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(rootLocation, 1)
                    .filter(path -> !path.equals(rootLocation))
                    .map(rootLocation::relativize);
        } catch (IOException e) {
            throw new AppException("Failed to read stored files", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Path load(String filename) {
        if (rootLocation == null) {
            init();
        }
        return rootLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) {
        System.out.println("file not found: " + filename);

        try {
            Path file = load(filename);
            System.out.println("file: " + file);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                System.out.println("file not found: " + filename);
                throw new AppException("Could not read file: " + filename, HttpStatus.NOT_FOUND);
            }
        } catch (MalformedURLException e) {
            System.out.println("file not found: " + filename);
            throw new AppException("Could not read file: " + filename, HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public void deleteAll() {
        try {
            Files.walk(rootLocation)
                    .filter(path -> !path.equals(rootLocation))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            throw new AppException("Failed to delete file", HttpStatus.INTERNAL_SERVER_ERROR);
                        }
                    });
        } catch (IOException e) {
            throw new AppException("Failed to delete files", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void delete(String filename) {
        try {
            Path file = load(filename);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new AppException("Failed to delete file: " + filename, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
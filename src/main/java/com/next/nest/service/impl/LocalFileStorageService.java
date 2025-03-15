package com.next.nest.service.impl;

import com.next.nest.exception.BadRequestException;
import com.next.nest.exception.FileStorageException;
import com.next.nest.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class LocalFileStorageService implements FileStorageService {

    private final Path fileStorageLocation;
    
    @Value("${app.url:http://localhost:8080}")
    private String appUrl;

    public LocalFileStorageService(@Value("${app.file.upload-dir:uploads}") String uploadDir) {
        try {
            this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored", ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file, String directory) {
        // Validate file
        if (file.isEmpty()) {
            throw new BadRequestException("Cannot store empty file");
        }
        
        // Clean the filename
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        
        // Check if the filename contains invalid characters
        if (originalFilename.contains("..")) {
            throw new BadRequestException("Filename contains invalid path sequence: " + originalFilename);
        }
        
        // Generate a unique filename
        String fileExtension = getFileExtension(originalFilename);
        String newFilename = UUID.randomUUID().toString() + fileExtension;
        
        // Create the directory if it doesn't exist
        Path directoryPath = this.fileStorageLocation.resolve(directory).normalize();
        try {
            Files.createDirectories(directoryPath);
        } catch (IOException ex) {
            throw new FileStorageException("Could not create directory: " + directory, ex);
        }
        
        // Copy the file to the target location
        Path targetLocation = directoryPath.resolve(newFilename);
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + originalFilename, ex);
        }
        
        // Return the URL to access the file
        return ServletUriComponentsBuilder.fromUriString(appUrl)
                .path("/api/files/")
                .path(directory)
                .path("/")
                .path(newFilename)
                .toUriString();
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        try {
            // Parse the file path from the URL
            String[] parts = fileUrl.split("/api/files/");
            if (parts.length < 2) {
                return false;
            }
            String relativePath = parts[1];
            
            // Resolve the file path
            Path filePath = this.fileStorageLocation.resolve(relativePath).normalize();
            
            // Delete the file
            return Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            log.error("Error deleting file: {}", fileUrl, ex);
            return false;
        }
    }

    @Override
    public byte[] getFile(String fileUrl) {
        try {
            // Parse the file path from the URL
            String[] parts = fileUrl.split("/api/files/");
            if (parts.length < 2) {
                throw new BadRequestException("Invalid file URL: " + fileUrl);
            }
            String relativePath = parts[1];
            
            // Resolve the file path
            Path filePath = this.fileStorageLocation.resolve(relativePath).normalize();
            
            // Check if the file exists
            if (!Files.exists(filePath)) {
                throw new BadRequestException("File not found: " + fileUrl);
            }
            
            // Read the file content
            return Files.readAllBytes(filePath);
        } catch (IOException ex) {
            throw new FileStorageException("Could not read file: " + fileUrl, ex);
        }
    }
    
    private String getFileExtension(String filename) {
        if (filename.lastIndexOf(".") != -1 && filename.lastIndexOf(".") != 0) {
            return filename.substring(filename.lastIndexOf("."));
        } else {
            return "";
        }
    }
}
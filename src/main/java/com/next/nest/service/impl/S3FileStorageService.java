package com.next.nest.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import com.next.nest.exception.BadRequestException;
import com.next.nest.exception.FileStorageException;
import com.next.nest.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "app.file.s3.enabled", havingValue = "true")
@Primary
@RequiredArgsConstructor
@Slf4j
public class S3FileStorageService implements FileStorageService {

    private final AmazonS3 amazonS3;
    
    @Value("${app.file.s3.bucket-name}")
    private String bucketName;

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
        
        // Full S3 key (path in the bucket)
        String key = directory + "/" + newFilename;
        
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());
            
            // Upload the file to S3
            amazonS3.putObject(bucketName, key, file.getInputStream(), metadata);
            
            // Return the URL to access the file
            return amazonS3.getUrl(bucketName, key).toString();
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + originalFilename, ex);
        }
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        try {
            // Extract key from URL
            String key = extractKeyFromUrl(fileUrl);
            
            // Delete the object from S3
            amazonS3.deleteObject(new DeleteObjectRequest(bucketName, key));
            return true;
        } catch (Exception ex) {
            log.error("Error deleting file from S3: {}", fileUrl, ex);
            return false;
        }
    }

    @Override
    public byte[] getFile(String fileUrl) {
        try {
            // Extract key from URL
            String key = extractKeyFromUrl(fileUrl);
            
            // Get the object from S3
            S3Object s3Object = amazonS3.getObject(bucketName, key);
            
            // Read the content
            try (S3ObjectInputStream objectContent = s3Object.getObjectContent()) {
                return IOUtils.toByteArray(objectContent);
            }
        } catch (IOException ex) {
            throw new FileStorageException("Could not read file from S3: " + fileUrl, ex);
        }
    }
    
    private String getFileExtension(String filename) {
        if (filename.lastIndexOf(".") != -1 && filename.lastIndexOf(".") != 0) {
            return filename.substring(filename.lastIndexOf("."));
        } else {
            return "";
        }
    }
    
    private String extractKeyFromUrl(String fileUrl) {
        // This is a simplified implementation and might need adjustment based on the actual S3 URL format
        String[] parts = fileUrl.split(bucketName + ".s3.");
        if (parts.length < 2) {
            throw new BadRequestException("Invalid S3 URL: " + fileUrl);
        }
        
        // Extract the key part after the region and bucket identifier
        String part = parts[1];
        int startIndex = part.indexOf("/") + 1;
        return part.substring(startIndex);
    }
}
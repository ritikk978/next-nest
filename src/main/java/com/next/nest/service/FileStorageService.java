package com.next.nest.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    
    /**
     * Store a file in the specified directory
     *
     * @param file The file to store
     * @param directory The directory path to store the file in
     * @return The URL to access the stored file
     */
    String storeFile(MultipartFile file, String directory);
    
    /**
     * Delete a file by its URL
     *
     * @param fileUrl The URL of the file to delete
     * @return True if the file was deleted successfully, false otherwise
     */
    boolean deleteFile(String fileUrl);
    
    /**
     * Get the file as a byte array
     *
     * @param fileUrl The URL of the file to get
     * @return The file as a byte array
     */
    byte[] getFile(String fileUrl);
}
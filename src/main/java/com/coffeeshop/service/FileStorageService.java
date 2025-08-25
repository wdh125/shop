package com.coffeeshop.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;

@Service
public class FileStorageService {
    @Value("${app.upload.dir}")
    private String uploadDir;

    public String saveFile(MultipartFile file) throws IOException {
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        File savedFile = new File(dir, fileName);
        file.transferTo(savedFile);

        return "/uploads/" + fileName;
    }
}
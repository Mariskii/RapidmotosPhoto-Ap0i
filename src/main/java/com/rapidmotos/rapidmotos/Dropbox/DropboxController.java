package com.rapidmotos.rapidmotos.Dropbox;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@CrossOrigin
@RequestMapping("/api/dropbox")
public class DropboxController {
    @Autowired
    private DropboxService dropboxService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = dropboxService.uploadFile(file);
            return ResponseEntity.ok("File uploaded successfully: " + fileUrl);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error uploading file: " + e.getMessage());
        }
    }
}

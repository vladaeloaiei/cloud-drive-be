package com.cc.cloud.drive.impl.controller;

import com.cc.cloud.drive.aws.s3.S3Exception;
import com.cc.cloud.drive.impl.service.CloudDriveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequestMapping("/user/drive")
@RestController
public class CloudDriveController {
    private CloudDriveService cloudDriveService;

    @Autowired
    public CloudDriveController(CloudDriveService cloudDriveService) {
        this.cloudDriveService = cloudDriveService;
    }

    @PostMapping("/{bucketId}")
    public ResponseEntity<?> uploadFile(@PathVariable("bucketId") int bucketId,
                                        @RequestParam("filename") String fileName,
                                        @RequestParam("file") MultipartFile file) {

        cloudDriveService.uploadFile(bucketId, file);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{bucketId}/{fileName}")
    public void downloadFile(@PathVariable("bucketId") int bucketId,
                             @PathVariable("fileName") String fileName,
                             HttpServletResponse response) {
        response.addHeader("Content-Disposition", "attachment; fileName=" + fileName);
        try {
            response.getOutputStream().write(cloudDriveService.downloadFile(bucketId, fileName));
            response.getOutputStream().flush();

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @DeleteMapping("/{bucketId}/{fileName}")
    public ResponseEntity<?> deleteFile(@PathVariable("bucketId") int bucketId,
                                        @PathVariable("fileName") String fileName) {

        cloudDriveService.deleteFile(bucketId, fileName);

        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(S3Exception.class)
    public ResponseEntity<?> handleStorageFileNotFound(S3Exception e) {
        e.printStackTrace();

        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        e.printStackTrace();

        return ResponseEntity.badRequest().body(e.getMessage());
    }
}


package com.wangsb.springbootintergration.controller;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

@RestController
@RequestMapping("/file")
public class FileController {

    private static final String ENCRYPTION_KEY = "ThisIsASecretKey"; // 请确保密钥长度为16、24或32字节


    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // 创建 ByteArrayOutputStream 来捕获加密后的文件内容
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // 加密文件
            try (InputStream fis = file.getInputStream()) {
                FileEncryptor.encrypt(fis, baos, ENCRYPTION_KEY);
            }

            // 将加密后的文件内容转换为字节数组
            byte[] encryptedBytes = baos.toByteArray();

            // 这里可以选择将加密后的字节数组保存到数据库或其他持久化存储中
            // 例如：saveEncryptedFileToDatabase(encryptedBytes);

            return ResponseEntity.ok("文件已成功上传并加密。");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("文件上传和加密失败");
        }
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable String fileName) {
        try {
            // 假设加密文件存储在某个目录下
            File encryptedFile = new File("C:\\Users\\18133\\AppData\\Local\\Temp\\encrypted_4318371859034651835.enc");

            // 使用 ByteArrayOutputStream 捕获解密后的文件内容
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // 解密文件
            try (FileInputStream fis = new FileInputStream(encryptedFile)) {
                FileEncryptor.decrypt(fis, baos, ENCRYPTION_KEY);
            }

            // 将 ByteArrayOutputStream 转换为 ByteArrayInputStream
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+fileName); // 根据实际情况设置文件名

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(baos.size())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(bais));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
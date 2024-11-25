package com.wangsb.springbootintergration.controller;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;

@RestController
@RequestMapping("/api")
public class PdfController {

    private static final Logger logger = LoggerFactory.getLogger(PdfController.class);
    private static final String PASSWORD = "EMmbTSMoFW5GWXQxGCvfGw==";
    private static final String ENCRYPTED_FILES_DIR = "E:\\home\\springboot-intergration\\src\\main\\resources\\file\\";

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // 读取上传的 PDF 文件
            byte[] pdfBytes = file.getBytes();

            // 加密 PDF 文件
            byte[] encryptedPdfBytes = encryptPdf(pdfBytes, PASSWORD);

            // 存储加密后的 PDF 文件到文件系统
            File encryptedFile = new File(ENCRYPTED_FILES_DIR + file.getOriginalFilename());
            FileUtils.writeByteArrayToFile(encryptedFile, encryptedPdfBytes);

            return ResponseEntity.ok("File uploaded and encrypted successfully");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error uploading file");
        }
    }


    @GetMapping("/download/{filename}")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable String filename) {
        try {
            // 从文件系统中读取加密的 PDF 文件
            byte[] encryptedPdfBytes = getEncryptedPdfBytes(filename);

            // 解密 PDF 文件
            byte[] decryptedPdfBytes = decryptPdf(encryptedPdfBytes, PASSWORD);

            // 创建响应头
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);

            // 返回解密后的 PDF 文件
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new InputStreamResource(new ByteArrayInputStream(decryptedPdfBytes)));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    private byte[] encryptPdf(byte[] pdfBytes, String password) throws IOException {
        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfBytes))) {
            // 创建访问权限
            AccessPermission ap = new AccessPermission();
            ap.setCanPrint(true); // 允许打印
            ap.setCanModify(false); // 不允许修改

            // 创建保护策略
            StandardProtectionPolicy protectionPolicy = new StandardProtectionPolicy(password, password, ap);
            protectionPolicy.setEncryptionKeyLength(128); // 设置加密密钥长度

            // 应用保护策略
            document.protect(protectionPolicy);

            // 确认文档已加密
            if (document.isEncrypted()) {
                logger.info("PDF document is encrypted successfully.");
            } else {
                logger.error("Failed to encrypt PDF document.");
            }

            // 保存加密后的 PDF 文件到字节数组
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    private byte[] decryptPdf(byte[] encryptedPdfBytes, String password) throws IOException {
        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(encryptedPdfBytes), password)) {
            // 检查文档是否加密
            if (document.isEncrypted()) {
                // 移除保护
                document.setAllSecurityToBeRemoved(true);
            }

            // 保存解密后的 PDF 文件到字节数组
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    // 从文件系统中读取加密的 PDF 文件
    private byte[] getEncryptedPdfBytes(String filename) throws IOException {
        File encryptedFile = new File(ENCRYPTED_FILES_DIR + filename);
        return FileUtils.readFileToByteArray(encryptedFile);
    }
}

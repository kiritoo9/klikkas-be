package com.klikkas.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OCRService {

    public List<String> uploadOCR(MultipartFile file) throws Exception {

        File tempFile = File.createTempFile("ocr-", ".jpg");
        file.transferTo(tempFile);

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "tesseract",
                    tempFile.getAbsolutePath(),
                    "stdout",
                    "-l",
                    "ind+eng");

            pb.redirectErrorStream(true);

            Process process = pb.start();

            List<String> result;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {

                result = reader.lines().toList();
            }

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("Tesseract failed:\n" + result);
            }

            return result;
        } finally {
            tempFile.delete();
        }
    }

}

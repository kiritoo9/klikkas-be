package com.klikkas.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.klikkas.service.OCRService;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;

@RestController
@RequiredArgsConstructor
public class OCRController {

    private final OCRService ocrService;

    @PostMapping("/ocr")
    public List<String> uploadOCR(
            @RequestPart("file") MultipartFile file) throws Exception {
        return ocrService.uploadOCR(file);
    }

}

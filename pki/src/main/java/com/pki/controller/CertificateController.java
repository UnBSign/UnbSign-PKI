package com.pki.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pki.service.CertificateService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;

import com.pki.dto.SerialNumbersRequest;


@RestController
@RequestMapping("api/pki/certificates")
public class CertificateController {
    private final CertificateService certificateService;
    @Autowired
    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @PostMapping("/signature")
    public ResponseEntity<String> signCertificate(
            @RequestParam("csr") String csrFile,
            @RequestParam("commonName") String commonName) {
        
        try {
            String signedCertificate = certificateService.signCsr(csrFile, commonName);
            
            return ResponseEntity.ok(signedCertificate);
        } catch (IOException | InterruptedException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error to process request");
        }

    }

    @GetMapping("/teste")
    public String getMethodName() {
        return "Muy bueno";
    }
    

    @PostMapping("/validate")
    public ResponseEntity<?> validateCertificates(@RequestBody SerialNumbersRequest request) {
        List<String> serialNumbers = request.getSerialNumbers();

        try {
            Map<String, Boolean> validationResults = certificateService.checkCertificate(serialNumbers);
            return ResponseEntity.ok(validationResults);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
}
package com.pki.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pki.config.PkiConfig;

import java.io.*;
import java.nio.file.*;

@Service
public class CertificateService {

    private final PkiConfig pkiConfig;

    private final Path CERTS_DIR;
    private final Path OPENSSL_CONFIG_PATH;

    @Autowired
    public CertificateService(PkiConfig pkiConfig) {
        this.pkiConfig = pkiConfig;
        
        this.CERTS_DIR = pkiConfig.getCertsDir();
        this.OPENSSL_CONFIG_PATH = pkiConfig.getOpensslConfigPath();
    }

    public String signCsr(String certificateRequest, String commonName) throws IOException, InterruptedException {
        String processedName = commonName.replaceAll("\\s+", "_").toLowerCase();
        
        Path csrFilePath = CERTS_DIR.resolve(processedName + ".csr");
        Files.write(csrFilePath, certificateRequest.getBytes(), StandardOpenOption.CREATE);

        Path certFilePath = CERTS_DIR.resolve(processedName + ".crt");

        String command = String.join(" ",
            "openssl", "ca",
            "-config", OPENSSL_CONFIG_PATH.toString(),
            "-in", csrFilePath.toString(),
            "-out", certFilePath.toString(),
            "-extensions", "enduser_ext"
        );

        executeScript(command);

        String certContent = Files.readString(certFilePath);

        
        return extractCertificate(certContent);

    }

    private void executeScript(String command) throws IOException, InterruptedException {
        String scriptPath = getClass().getClassLoader().getResource("run_openssl.sh").getPath();

        ProcessBuilder processBuilder = new ProcessBuilder(scriptPath, command);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);  // Imprime a saída do comando
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Erro ao executar o script. Código de erro: " + exitCode);
            }
        }

    }

    private String extractCertificate(String certContent) {
        // Localiza a posição das tags BEGIN e END CERTIFICATE
        String beginTag = "-----BEGIN CERTIFICATE-----";
        String endTag = "-----END CERTIFICATE-----";
        
        int beginIndex = certContent.indexOf(beginTag);
        int endIndex = certContent.indexOf(endTag);

        if (beginIndex == -1 || endIndex == -1) {
            throw new IllegalArgumentException("Certificado não encontrado no formato esperado");
        }

        // Extraímos o conteúdo entre as tags
        return certContent.substring(beginIndex, endIndex + endTag.length());
    }
}

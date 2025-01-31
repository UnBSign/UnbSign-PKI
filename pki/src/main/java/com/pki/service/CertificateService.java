package com.pki.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pki.config.PkiConfig;
import java.util.Map;
import java.util.regex.Pattern;
import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CertificateService {

    private final PkiConfig pkiConfig;

    private final Path CERTS_DIR;
    private final Path OPENSSL_CONFIG_PATH;
    private final Path DB_PATH;

    @Autowired
    public CertificateService(PkiConfig pkiConfig) {
        this.pkiConfig = pkiConfig;
        
        this.CERTS_DIR = pkiConfig.getCertsDir();
        this.OPENSSL_CONFIG_PATH = pkiConfig.getOpensslConfigPath();
        this.DB_PATH = pkiConfig.getCertsDataBase();
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
        String scriptPath = "/app/run_openssl.sh";
    
        ProcessBuilder processBuilder = new ProcessBuilder(scriptPath, command);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
    
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
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

    public Map<String, Boolean> checkCertificate(List<String> serialNumbers) {

        if (serialNumbers == null || serialNumbers.isEmpty()) {
            throw new IllegalArgumentException("Error: serialNumbers list is empty or null");
        }

        Map<String, Boolean> validationResults = new HashMap<>();

        for (String serial : serialNumbers) {
            boolean exists = getCertificatesInDB(serial);
            validationResults.put(serial, exists);
        }

        return validationResults;
    }

    private boolean getCertificatesInDB(String serial) {
        
        String certPattern = ".*Z\\t\\t([0-9A-F]{40})\\tunknown";
        Pattern pattern = Pattern.compile(certPattern);
        
        try (BufferedReader br = Files.newBufferedReader(DB_PATH)) {
            String line;
            
            while ((line = br.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);

                if (matcher.find()) {
                    String matchedSerial = matcher.group(1);
                    if (matchedSerial.equalsIgnoreCase(serial)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}

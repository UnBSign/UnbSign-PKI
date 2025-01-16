package com.pki.service;

import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.file.*;

@Service
public class CertificateService {
    private static final Path CERTS_DIR = Paths.get("/home/sidney/Documentos/UnB/UNBSIGN/UnbSign-PKI/simplepki/certs");
    private static final Path OPENSSL_CONFIG_PATH = Paths.get("/home/sidney/Documentos/UnB/UNBSIGN/UnbSign-PKI/simplepki/etc/unbsub-ca.conf");

    public String signCsr(String certificateRequest, String commonName) throws IOException, InterruptedException {
        Path csrFilePath = CERTS_DIR.resolve(commonName + ".csr");
        Files.write(csrFilePath, certificateRequest.getBytes(), StandardOpenOption.CREATE);

        Path certFilePath = CERTS_DIR.resolve(commonName + ".crt");

        String command = String.join(" ",
            "openssl", "ca",
            "-config", OPENSSL_CONFIG_PATH.toString(),
            "-in", csrFilePath.toString(),
            "-out", certFilePath.toString(),
            "-extensions", "enduser_ext"
        );

        executeScript(command);

        return Files.readString(certFilePath);

    }

    private void executeScript(String command) throws IOException, InterruptedException {
        String scriptPath = "/home/sidney/Documentos/UnB/UNBSIGN/UnbSign-PKI/pki/src/main/resources/run_openssl.sh";

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
}

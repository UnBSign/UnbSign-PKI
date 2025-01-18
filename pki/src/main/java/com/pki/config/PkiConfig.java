package com.pki.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class PkiConfig {

    @Value("${pki.directory}")
    private String pkiDirectory;

    public Path getCertsDir() {
        return Paths.get(pkiDirectory, "certs");
    }

    public Path getOpensslConfigPath() {
        return Paths.get(pkiDirectory, "etc", "unbsub-ca.conf");
    }
}
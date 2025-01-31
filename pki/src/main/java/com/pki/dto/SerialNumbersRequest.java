package com.pki.dto;

import java.util.List;

public class SerialNumbersRequest {
    private List<String> serialNumbers;

    public List<String> getSerialNumbers() {
        return serialNumbers;
    }

    public void setSerialNumbers(List<String> serialNumbers) {
        this.serialNumbers = serialNumbers;
    }
}

package org.example.printer_agent.dto;

public record PrintRequest(
        String printerName,
        String filePath
) {
}
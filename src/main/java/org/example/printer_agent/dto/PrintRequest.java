package org.example.printer_agent.dto;

public record PrintRequest(
        String printerName,
        String filePath,
        String paperSize,
        String orientation,
        Boolean color,
        Boolean duplex,
        Integer copies
) {
}
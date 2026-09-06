package org.example.printer_agent.dto;

import java.util.List;

public record PrinterSettings(
        String printerName,
        List<String> paperSizes,
        List<String> orientations,
        boolean color,
        boolean duplex,
        int defaultCopies
) {
}
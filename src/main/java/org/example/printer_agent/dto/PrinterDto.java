package org.example.printer_agent.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PrinterDto {

    private Long printerId;

    private String printerName;

    private String description;

    private String useYn;
}

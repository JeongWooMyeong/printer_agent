package org.example.printer_agent.Controller;

import lombok.RequiredArgsConstructor;
import org.example.printer_agent.dto.PrintRequest;
import org.example.printer_agent.service.PdfPrintService;
import org.example.printer_agent.service.PrinterService;
import org.springframework.web.bind.annotation.*;

import javax.print.PrintService;
import java.util.List;

@RestController
@RequestMapping("/api/printers")
@RequiredArgsConstructor
public class PrinterController {

    private final PrinterService printerService;
    private final PdfPrintService pdfPrintService;

    @GetMapping
    public List<String> getPrinters() {

        return printerService.getPrinters();
    }

    @PostMapping("/test")
    public String testPrinter(
            @RequestBody PrintRequest request
    ) {

        PrintService printer =
                printerService.findPrinter(
                        request.printerName()
                );

        return "프린터 선택 성공: "
                + printer.getName();
    }

    @PostMapping("/print")
    public String print(
            @RequestBody PrintRequest request
    ) throws Exception {

        pdfPrintService.print(
                request.printerName(),
                request.filePath()
        );

        return "출력 완료";
    }
}
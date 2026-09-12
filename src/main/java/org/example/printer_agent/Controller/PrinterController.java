package org.example.printer_agent.Controller;

import lombok.RequiredArgsConstructor;
import org.example.printer_agent.dto.PrintRequest;
import org.example.printer_agent.dto.PrinterSettings;
import org.example.printer_agent.service.PdfPrintService;
import org.example.printer_agent.service.PrinterService;
import org.springframework.web.bind.annotation.*;

import javax.print.PrintService;
import java.util.List;

@RestController
@RequestMapping("/api/agent/printers")
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
                request.filePath(),
                request.paperSize(),
                request.orientation(),
                request.color(),
                request.duplex(),
                request.copies()
        );

        return "출력 완료";
    }

    @GetMapping("/{printerName}/settings")
    public PrinterSettings getPrinterSettings(
            @PathVariable String printerName
    ) {
        return printerService.getPrinterSettings(printerName);
    }

}
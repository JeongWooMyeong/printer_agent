package org.example.printer_agent.service;

import org.springframework.stereotype.Service;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.util.Arrays;
import java.util.List;

@Service
public class PrinterService {

    /**
     * 설치된 프린터 목록 조회
     */
    public List<String> getPrinters() {

        PrintService[] printServices =
                PrintServiceLookup.lookupPrintServices(
                        null,
                        null
                );

        return Arrays.stream(printServices)
                .map(PrintService::getName)
                .toList();
    }

    /**
     * 프린터 이름으로 프린터 조회
     */
    public PrintService findPrinter(String printerName) {

        PrintService[] printServices =
                PrintServiceLookup.lookupPrintServices(
                        null,
                        null
                );

        return Arrays.stream(printServices)
                .filter(service ->
                        service.getName()
                                .equals(printerName)
                )
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException(
                                "프린터를 찾을 수 없습니다: "
                                        + printerName
                        )
                );
    }
}
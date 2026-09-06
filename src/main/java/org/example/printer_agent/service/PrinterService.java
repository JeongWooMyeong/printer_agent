package org.example.printer_agent.service;

import lombok.RequiredArgsConstructor;
import org.example.printer_agent.client.PopPrinterClient;
import org.example.printer_agent.dto.PrinterDto;
import org.example.printer_agent.dto.PrinterSettings;
import org.springframework.stereotype.Service;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.Attribute;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PrinterService {

    private final PopPrinterClient popPrinterClient;

    /**
     * 설치된 프린터 목록 조회
     */
    /*
     * DB에 등록된 프린터 중
     * 현재 Windows에 설치되어 있는 프린터만 조회
     */
    public List<String> getPrinters() {
        //DB 등록 프린터
        List<PrinterDto> registeredPrinters =
                popPrinterClient.getRegisteredPrinters();
        //Windows 설치 프린터
        PrintService[] windowsPrinters = PrintServiceLookup.lookupPrintServices(
                null,
                null
        );

        Set<String> windowsPrinterNames = new HashSet<>();

        for(PrintService printer : windowsPrinters){
            windowsPrinterNames.add(printer.getName());
        }

        //DB 프린터와 Windows 프린터 비교
        List<String> result = new ArrayList<>();

        for(PrinterDto printer : registeredPrinters){
            String printerName = printer.getPrinterName();

            if(windowsPrinterNames.contains(printerName)){
                result.add(printerName);
            }
        }

        return result;
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

    /*
     * 프린터 설정 조회
     */
    public PrinterSettings getPrinterSettings(
            String printerName
    ) {

        PrintService printer =
                findPrinter(printerName);

        printPrinterAttributes(printer);

        return new PrinterSettings(
                printer.getName(),

                // 일단 기본 용지
                List.of(
                        "A4",
                        "A5",
                        "B4",
                        "B5"
                ),

                // 방향
                List.of(
                        "PORTRAIT",
                        "LANDSCAPE"
                ),

                // 컬러
                true,

                // 양면
                true,

                // 기본 매수
                1
        );
    }

    private void printPrinterAttributes(PrintService printer) {

        System.out.println("========================================");
        System.out.println("프린터 이름: " + printer.getName());
        System.out.println("========================================");

        Class<?>[] categories =
                printer.getSupportedAttributeCategories();

        System.out.println(
                "지원 속성 개수: " + categories.length
        );

        for (Class<?> category : categories) {

            System.out.println("----------------------------------------");
            System.out.println(
                    "속성: " + category.getName()
            );

            if (!Attribute.class.isAssignableFrom(category)) {
                continue;
            }

            @SuppressWarnings("unchecked")
            Class<? extends Attribute> attributeCategory =
                    (Class<? extends Attribute>) category;

            Object values =
                    printer.getSupportedAttributeValues(
                            attributeCategory,
                            null,
                            null
                    );

            if (values == null) {

                System.out.println(
                        "지원 값: 없음"
                );

                continue;
            }

            if (values.getClass().isArray()) {

                int length =
                        java.lang.reflect.Array.getLength(values);

                for (int i = 0; i < length; i++) {

                    Object value =
                            java.lang.reflect.Array.get(
                                    values,
                                    i
                            );

                    System.out.println(
                            "  - " + value
                    );
                }

            } else {

                System.out.println(
                        "  - " + values
                );
            }
        }

        System.out.println("========================================");
    }

}
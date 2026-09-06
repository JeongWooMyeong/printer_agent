package org.example.printer_agent.client;

import org.example.printer_agent.dto.PrinterDto;
import org.example.printer_agent.exception.PrinterAgentException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class PopPrinterClient {

    private final RestClient restClient;
    private final String popServerUrl;

    public PopPrinterClient(
            @Value("${pop.server.url}") String popServerUrl
    ) {

        this.popServerUrl = popServerUrl;

        this.restClient =
                RestClient.builder()
                        .baseUrl(popServerUrl)
                        .build();
    }

     /*
     * POP 서버에 등록된 프린터 조회
     */
    public List<PrinterDto> getRegisteredPrinters() {

        System.out.println(
                "POP 서버 요청: "
                        + popServerUrl
                        + "/api/printers"
        );

        try {

            PrinterDto[] printers =
                    restClient.get()
                            .uri("/api/printers")
                            .retrieve()
                            .body(PrinterDto[].class);

            if (printers == null) {
                return List.of();
            }

            return Arrays.asList(printers);

        } catch (Exception e) {

            throw new PrinterAgentException(
                    "POP 서버에서 프린터 목록을 조회하지 못했습니다.",
                    e
            );
        }
    }
}

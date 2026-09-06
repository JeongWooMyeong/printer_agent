package org.example.printer_agent.service;

import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.example.printer_agent.exception.PrinterAgentException;
import org.springframework.stereotype.Service;

import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Chromaticity;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.Sides;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
@RequiredArgsConstructor
public class PdfPrintService {

    private final PrinterService printerService;

    public void print(
            String printerName,
            String filePath,
            String paperSize,
            String orientation,
            Boolean color,
            Boolean duplex,
            Integer copies
    ) throws Exception {

        // ========================================
        // PDF 다운로드
        // ========================================

        HttpClient client =
                HttpClient.newHttpClient();

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(filePath))
                        .GET()
                        .build();

        HttpResponse<byte[]> response =
                client.send(
                        request,
                        HttpResponse.BodyHandlers.ofByteArray()
                );

        if (response.statusCode() != 200) {

            throw new PrinterAgentException(
                    "PDF 다운로드 실패. HTTP Status="
                            + response.statusCode()
            );
        }


        // ========================================
        // 프린터 조회
        // ========================================

        PrintService printer =
                printerService.findPrinter(
                        printerName
                );


        // ========================================
        // PDF 로드
        // ========================================

        try (PDDocument document =
                     Loader.loadPDF(response.body())) {

            PDFRenderer renderer =
                    new PDFRenderer(document);


            // ========================================
            // Printable 생성
            // ========================================

            Printable printable =
                    (graphics, pageFormat, pageIndex) -> {

                        if (
                                pageIndex >=
                                        document.getNumberOfPages()
                        ) {
                            return Printable.NO_SUCH_PAGE;
                        }

                        Graphics2D g2 =
                                (Graphics2D) graphics;

                        BufferedImage image;

                        try {

                            image =
                                    renderer.renderImageWithDPI(
                                            pageIndex,
                                            150
                                    );

                        } catch (IOException e) {

                            throw new PrinterAgentException(
                                    "PDF 페이지 렌더링에 실패했습니다. page="
                                            + pageIndex,
                                    e
                            );
                        }


                        // ========================================
                        // 출력 가능 영역
                        // ========================================

                        double pageWidth =
                                pageFormat.getImageableWidth();

                        double pageHeight =
                                pageFormat.getImageableHeight();


                        // ========================================
                        // PDF 이미지 비율 유지
                        // ========================================

                        double scaleX =
                                pageWidth
                                        / image.getWidth();

                        double scaleY =
                                pageHeight
                                        / image.getHeight();

                        double scale =
                                Math.min(
                                        scaleX,
                                        scaleY
                                );


                        // ========================================
                        // 중앙 정렬
                        // ========================================

                        double x =
                                pageFormat.getImageableX()
                                        + (
                                        pageWidth
                                                - image.getWidth()
                                                * scale
                                ) / 2;

                        double y =
                                pageFormat.getImageableY()
                                        + (
                                        pageHeight
                                                - image.getHeight()
                                                * scale
                                ) / 2;


                        // ========================================
                        // 이미지 출력
                        // ========================================

                        g2.drawImage(
                                image,
                                (int) x,
                                (int) y,
                                (int) (
                                        image.getWidth()
                                                * scale
                                ),
                                (int) (
                                        image.getHeight()
                                                * scale
                                ),
                                null
                        );

                        return Printable.PAGE_EXISTS;
                    };


            // ========================================
            // PrinterJob 생성
            // ========================================

            PrinterJob printerJob =
                    PrinterJob.getPrinterJob();

            printerJob.setPrintService(printer);

            printerJob.setPrintable(printable);


            // ========================================
            // 인쇄 설정
            // ========================================

            PrintRequestAttributeSet attributes =
                    new HashPrintRequestAttributeSet();


            // ----------------------------------------
            // 용지
            // ----------------------------------------

            if (paperSize != null) {

                switch (paperSize.toUpperCase()) {

                    case "A4":
                        attributes.add(
                                MediaSizeName.ISO_A4
                        );
                        break;

                    case "A5":
                        attributes.add(
                                MediaSizeName.ISO_A5
                        );
                        break;

                    case "B5":
                        attributes.add(
                                MediaSizeName.ISO_B5
                        );
                        break;

                    case "LETTER":
                        attributes.add(
                                MediaSizeName.NA_LETTER
                        );
                        break;

                    default:
                        attributes.add(
                                MediaSizeName.ISO_A4
                        );
                        break;
                }
            }


            // ----------------------------------------
            // 방향
            // ----------------------------------------

            if ("LANDSCAPE".equalsIgnoreCase(
                    orientation
            )) {

                attributes.add(
                        OrientationRequested.LANDSCAPE
                );

            } else {

                attributes.add(
                        OrientationRequested.PORTRAIT
                );
            }


            // ----------------------------------------
            // 컬러
            // ----------------------------------------

            if (Boolean.TRUE.equals(color)) {

                attributes.add(
                        Chromaticity.COLOR
                );

            } else {

                attributes.add(
                        Chromaticity.MONOCHROME
                );
            }


            // ----------------------------------------
            // 양면
            // ----------------------------------------

            if (Boolean.TRUE.equals(duplex)) {

                attributes.add(
                        Sides.DUPLEX
                );

            } else {

                attributes.add(
                        Sides.ONE_SIDED
                );
            }


            // ----------------------------------------
            // 매수
            // ----------------------------------------

            int printCopies =
                    copies == null || copies < 1
                            ? 1
                            : copies;

            attributes.add(
                    new Copies(printCopies)
            );


            // ========================================
            // 실제 출력
            // ========================================

            printerJob.print(
                    attributes
            );
        }
    }
}
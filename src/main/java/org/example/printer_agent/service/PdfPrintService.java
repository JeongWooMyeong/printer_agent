package org.example.printer_agent.service;

import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.example.printer_agent.exception.PrinterAgentException;
import org.springframework.stereotype.Service;

import javax.print.PrintService;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class PdfPrintService {

    private final PrinterService printerService;

    public void print(
            String printerName,
            String filePath
    ) throws Exception {

        // 프린터 조회
        PrintService printer =
                printerService.findPrinter(printerName);

        try (PDDocument document =
                     Loader.loadPDF(new File(filePath))) {

            PDFRenderer renderer =
                    new PDFRenderer(document);

            // Printable 생성
            Printable printable =
                    (graphics, pageFormat, pageIndex) -> {

                        if (pageIndex >= document.getNumberOfPages()) {
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

                        double pageWidth =
                                pageFormat.getImageableWidth();

                        double pageHeight =
                                pageFormat.getImageableHeight();

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

            // PrinterJob 생성
            PrinterJob printerJob =
                    PrinterJob.getPrinterJob();

            printerJob.setPrintService(printer);

            printerJob.setPrintable(printable);

            // 실제 출력
            printerJob.print();
        }
    }
}
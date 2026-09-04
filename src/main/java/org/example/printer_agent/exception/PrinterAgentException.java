package org.example.printer_agent.exception;

public class PrinterAgentException extends RuntimeException {

    public PrinterAgentException(String message) {
        super(message);
    }

    public PrinterAgentException(String message, Throwable cause) {
        super(message, cause);
    }
}
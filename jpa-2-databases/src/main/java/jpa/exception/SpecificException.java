package jpa.exception;

public class SpecificException extends RuntimeException {
    public SpecificException() {
        super("fail processing for id 1000");
    }
}

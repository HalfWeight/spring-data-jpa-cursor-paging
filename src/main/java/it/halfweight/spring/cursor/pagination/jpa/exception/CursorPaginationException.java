package it.halfweight.spring.cursor.pagination.jpa.exception;

public class CursorPaginationException extends RuntimeException {

    public CursorPaginationException() {
    }

    public CursorPaginationException(String s) {
        super(s);
    }

    public CursorPaginationException(String s, Throwable throwable) {
        super(s, throwable);
    }

}

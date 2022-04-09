package it.halfweight.spring.cursor.pagination.jpa.domain;

import org.springframework.data.domain.Sort;

public class CursorPageRequest implements CursorPageable {

    private String continuationToken;

    private int size;

    private Sort sort;

    protected CursorPageRequest() {
    }

    protected CursorPageRequest(String continuationToken, int size, Sort sort) {
        this.continuationToken = continuationToken;
        this.size = size;
        this.sort = sort;
    }

    public static CursorPageRequest of(String continuationToken, int size) {
        return of(continuationToken, size, null);
    }

    public static CursorPageRequest of(int size) {
        return of(null, size, null);
    }

    public static CursorPageRequest of(int size, Sort sort) {
        return of(null, size, sort);
    }

    public static CursorPageRequest of(String continuationToken, int size, Sort sort) {
        return new CursorPageRequest(continuationToken, size, sort);
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    @Override
    public String getContinuationToken() {
        return continuationToken;
    }

}

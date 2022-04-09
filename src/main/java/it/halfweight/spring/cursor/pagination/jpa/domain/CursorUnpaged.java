package it.halfweight.spring.cursor.pagination.jpa.domain;

import org.springframework.data.domain.Sort;

enum CursorUnpaged implements CursorPageable {

    INSTANCE;

    @Override
    public int getSize() {
        return 20;
    }

    @Override
    public Sort getSort() {
        return null;
    }

    @Override
    public String getContinuationToken() {
        return null;
    }

}

package it.halfweight.spring.cursor.pagination.jpa.domain;

import org.springframework.data.domain.Sort;

import java.util.Optional;

public interface CursorPageable {

    /**
     * Returns a {@link CursorPageable} instance representing no pagination setup.
     * @return
     */
    static CursorPageable unpaged() {
        return CursorUnpaged.INSTANCE;
    }

    /**
     * Returns the number of items to be returned.
     * @return the number of items of that page
     */
    int getSize();

    /**
     * Returns the sorting parameter.
     * @return
     */
    Sort getSort();

    /**
     * Returns the continuationToken parameter.
     * @return
     */
    String getContinuationToken();

    /**
     * Returns whether the current {@link CursorPageable} contains pagination information.
     * @return
     */
    default boolean isPaged() {
        return true;
    }

    /**
     * Returns whether the current {@link CursorPageable} does not contain pagination
     * information.
     * @return
     */
    default boolean isUnpaged() {
        return !isPaged();
    }

    /**
     * Returns an {@link Optional} so that it can easily be mapped on.
     * @return
     */
    default Optional<CursorPageable> toOptional() {
        return isUnpaged() ? Optional.empty() : Optional.of(this);
    }

}

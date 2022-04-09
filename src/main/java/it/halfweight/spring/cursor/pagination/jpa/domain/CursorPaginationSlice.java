package it.halfweight.spring.cursor.pagination.jpa.domain;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class CursorPaginationSlice<T> {

    private final List<T> content;

    private final boolean hasNext;

    private final String continuationToken;

    private final int size;

    /**
     * Creates a new {@link CursorPaginationSlice} with the given content and metadata
     *
     * @param content           must not be {@literal null}. from the current one.
     * @param size              the size of the {@link CursorPaginationSlice} to be returned.
     * @param continuationToken continuationToken to access the next
     *                          {@link CursorPaginationSlice}. Can be {@literal null}.
     */
    public CursorPaginationSlice(@NonNull List<T> content, int size, @Nullable String continuationToken) {

        Assert.notNull(content, "Content must not be null!");

        this.content = new ArrayList<>(content);
        this.continuationToken = continuationToken;
        this.hasNext = continuationToken != null && !content.isEmpty();
        this.size = size;
    }

    /**
     * Returns the continuationToken to access the next {@link CursorPaginationSlice}
     * whether there's one.
     *
     * @return Returns the continuationToken to access the next
     * {@link CursorPaginationSlice} whether there's one.
     */
    public String getContinuationToken() {
        return continuationToken;
    }

    /**
     * Returns the number of elements currently on this {@link CursorPaginationSlice}.
     *
     * @return the number of elements currently on this {@link CursorPaginationSlice}.
     */
    public int getNumberOfElements() {
        return content.size();
    }

    /**
     * Returns if there is a next {@link CursorPaginationSlice}.
     *
     * @return if there is a next {@link CursorPaginationSlice}.
     */
    public boolean hasNext() {
        return this.hasNext;
    }

    /**
     * Returns whether the {@link CursorPaginationSlice} has content at all.
     *
     * @return whether the {@link CursorPaginationSlice} has content at all.
     */
    public boolean hasContent() {
        return !content.isEmpty();
    }

    /**
     * Returns the page content as {@link List}.
     *
     * @return the page content as {@link List}.
     */
    public List<T> getContent() {
        return Collections.unmodifiableList(content);
    }

    /**
     * Returns the size of the {@link CursorPaginationSlice}.
     *
     * @return the size of the {@link CursorPaginationSlice}.
     */
    public int getSize() {
        return size;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Iterable#iterator()
     */
    public Iterator<T> iterator() {
        return content.iterator();
    }


}

package it.halfweight.spring.cursor.pagination.jpa.domain;


import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;

public interface Projection<X, Y> {

    Selection<Y> toSelection(Root<X> root, CriteriaBuilder cb);
}

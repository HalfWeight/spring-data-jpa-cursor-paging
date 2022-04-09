package it.halfweight.spring.cursor.pagination.jpa.domain;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

public interface Projection<X, Y> {

    Selection<Y> toSelection(Root<X> root, CriteriaBuilder cb);
}

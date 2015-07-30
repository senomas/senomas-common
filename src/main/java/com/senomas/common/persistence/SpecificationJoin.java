package com.senomas.common.persistence;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

public interface SpecificationJoin<T, T1, T2> {

	/**
	 * Creates a WHERE clause for a query of the referenced entity in form of a {@link Predicate} for the given
	 * {@link Root} and {@link CriteriaQuery}.
	 * 
	 * @param root
	 * @param query
	 * @return a {@link Predicate}, must not be {@literal null}.
	 */
	Predicate toPredicate(Root<T1> root1, Root<T2> root2, CriteriaQuery<?> query, CriteriaBuilder builder);
	
	Selection<? extends T> getSelection(Root<T1> root1, Root<T2> root2, CriteriaQuery<?> query, CriteriaBuilder builder);

}

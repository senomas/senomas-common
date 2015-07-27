package com.senomas.common.persistence;

import javax.persistence.EntityManager;

public interface EntityUnwrapper<T> {

    T unwrap(EntityManager entityManager);
    
}

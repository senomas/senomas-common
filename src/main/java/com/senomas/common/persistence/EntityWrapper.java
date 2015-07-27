package com.senomas.common.persistence;

import javax.persistence.EntityManager;

public interface EntityWrapper<T> {
    
    void wrap(EntityManager entityManager, T object);
    
}

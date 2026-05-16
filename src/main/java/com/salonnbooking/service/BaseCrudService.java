package com.salonnbooking.service;

import java.lang.reflect.Field;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.salonnbooking.exception.ResourceNotFoundException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ManyToOne;

public abstract class BaseCrudService<T> {

    private final JpaRepository<T, Long> repository;
    private final EntityManager entityManager;
    private final Class<T> entityClass;

    protected BaseCrudService(JpaRepository<T, Long> repository, EntityManager entityManager, Class<T> entityClass) {
        this.repository = repository;
        this.entityManager = entityManager;
        this.entityClass = entityClass;
    }

    @Transactional(readOnly = true)
    public List<T> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public T findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(entityClass.getSimpleName() + " not found with id: " + id));
    }

    @Transactional
    public T create(T entity) {
        normalizeReferences(entity);
        return repository.save(entity);
    }

    @Transactional
    public T update(Long id, T request) {
        T existing = findById(id);
        BeanUtils.copyProperties(request, existing, "id");
        normalizeReferences(existing);
        return repository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        T existing = findById(id);
        repository.delete(existing);
    }

    private void normalizeReferences(T entity) {
        for (Field field : entityClass.getDeclaredFields()) {
            if (!field.isAnnotationPresent(ManyToOne.class)) {
                continue;
            }

            field.setAccessible(true);

            try {
                Object reference = field.get(entity);
                if (reference == null) {
                    continue;
                }

                Object referenceId = extractId(reference);
                if (referenceId == null) {
                    continue;
                }

                Object managedReference = entityManager.getReference(field.getType(), referenceId);
                field.set(entity, managedReference);
            } catch (IllegalAccessException ex) {
                throw new IllegalStateException("Cannot normalize relation field: " + field.getName(), ex);
            }
        }
    }

    private Object extractId(Object reference) {
        Class<?> current = reference.getClass();
        while (current != null) {
            try {
                Field idField = current.getDeclaredField("id");
                idField.setAccessible(true);
                return idField.get(reference);
            } catch (NoSuchFieldException ex) {
                current = current.getSuperclass();
            } catch (IllegalAccessException ex) {
                throw new IllegalStateException("Cannot read relation id", ex);
            }
        }
        return null;
    }
}

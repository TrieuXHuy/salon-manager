package com.salonnbooking.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.salonnbooking.service.BaseCrudService;

public abstract class BaseCrudController<T> {

    private final BaseCrudService<T> service;

    protected BaseCrudController(BaseCrudService<T> service) {
        this.service = service;
    }

    @GetMapping
    public List<T> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public T findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public T create(@RequestBody T request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public T update(@PathVariable Long id, @RequestBody T request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

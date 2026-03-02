/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.infomatrices.vanigam.arvinsoft.repository;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author aravindhmuthuswamy
 */
public interface BaseRepository<T, ID extends Serializable> {
    T save(T entity);
    List<T> saveAll(List<T> entities);
    void update(T entity);
    void delete(T entity);
    void deleteById(ID id);
    Optional<T> findById(ID id);
    List<T> findAll();
    long count();
}

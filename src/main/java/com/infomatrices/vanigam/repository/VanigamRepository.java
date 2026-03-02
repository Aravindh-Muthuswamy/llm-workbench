/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.infomatrices.vanigam.repository;

import jakarta.persistence.EntityManager;
import java.util.List;

/**
 *
 * @author aravindhmuthuswamy
 */
public interface VanigamRepository<T> {
    void save(T t, EntityManager em);
    List<T> findAll(EntityManager em);
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.infomatrices.vanigam.config;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.EntityManager;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.SessionFactory;
import org.hibernate.Session;

/**
 *
 * @author aravindhmuthuswamy
 */
@Getter
@Setter
public class HibernateConfig {
     private EntityManagerFactory factory;
    private EntityManager entityManager;

    public HibernateConfig() {
        this.setFactory(Persistence.createEntityManagerFactory("vanigam"));
    }

    public EntityManager getEntityManager() {
        return entityManager = new HibernateConfig().getFactory().createEntityManager();
    }

    public SessionFactory getSessionFactoryFromConfig(){
        return getEntityManager().unwrap(Session.class).getFactory();
    }
    
    public void closeConnection() {
        entityManager.close();
        getFactory().close();
    }

    
}

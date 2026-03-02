/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.infomatrices.vanigam.arvinsoft.repository;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author aravindhmuthuswamy
 */
public abstract class ArvinSoftHibernateRepository<T, ID extends Serializable> 
    implements BaseRepository<T, ID> {
    
    private final Class<T> entityClass;
    private final SessionFactory sessionFactory;
    
    @SuppressWarnings("unchecked")
    public ArvinSoftHibernateRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.entityClass = (Class<T>) ((ParameterizedType) getClass()
            .getGenericSuperclass()).getActualTypeArguments()[0];
    }
    
    protected Session getSession() {
        return sessionFactory.getCurrentSession();
    }
    
    protected SessionFactory getSessionFactory() {
        return sessionFactory;
    }
    
    @Override
    public T save(T entity) {
        Transaction tx = null;
        try {
            Session session = sessionFactory.openSession();
            tx = session.beginTransaction();
            session.persist(entity);
            tx.commit();
            session.close();
            return entity;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Error saving entity", e);
        }
    }
    
    @Override
    public List<T> saveAll(List<T> entities) {
        Transaction tx = null;
        try {
            Session session = sessionFactory.openSession();
            tx = session.beginTransaction();
            for (T entity : entities) {
                session.persist(entity);
            }
            tx.commit();
            session.close();
            return entities;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Error saving entities", e);
        }
    }
    
    @Override
    public void update(T entity) {
        Transaction tx = null;
        try {
            Session session = sessionFactory.openSession();
            tx = session.beginTransaction();
            session.merge(entity);
            tx.commit();
            session.close();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Error updating entity", e);
        }
    }
    
    @Override
    public void delete(T entity) {
        Transaction tx = null;
        try {
            Session session = sessionFactory.openSession();
            tx = session.beginTransaction();
            session.remove(entity);
            tx.commit();
            session.close();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Error deleting entity", e);
        }
    }
    
    @Override
    public void deleteById(ID id) {
        findById(id).ifPresent(this::delete);
    }
    
    @Override
    public Optional<T> findById(ID id) {
        try (Session session = sessionFactory.openSession()) {
            T entity = session.find(entityClass, id);
            return Optional.ofNullable(entity);
        } catch (Exception e) {
            throw new RuntimeException("Error finding entity by id", e);
        }
    }
    
    @Override
    public List<T> findAll() {
        try (Session session = sessionFactory.openSession()) {
            Query<T> query = session.createQuery(
                "FROM " + entityClass.getSimpleName(), entityClass);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding all entities", e);
        }
    }
    
    @Override
    public long count() {
        try (Session session = sessionFactory.openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e", 
                Long.class);
            return query.getSingleResult();
        } catch (Exception e) {
            throw new RuntimeException("Error counting entities", e);
        }
    }
    
    // Protected helper for custom queries in extended repositories
    protected List<T> executeQuery(String hql, Object... params) {
        try (Session session = sessionFactory.openSession()) {
            Query<T> query = session.createQuery(hql, entityClass);
            for (int i = 0; i < params.length; i++) {
                query.setParameter(i + 1, params[i]); // Hibernate ordinal params start at 1
            }
            return query.getResultList();
        }
    }
    
    // Protected helper for custom queries with named parameters
    protected List<T> executeNamedQuery(String hql, java.util.Map<String, Object> params) {
        try (Session session = sessionFactory.openSession()) {
            Query<T> query = session.createQuery(hql, entityClass);
            params.forEach(query::setParameter);
            return query.getResultList();
        }
    }
}

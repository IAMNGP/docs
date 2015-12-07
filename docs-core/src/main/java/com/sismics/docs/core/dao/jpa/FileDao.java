package com.sismics.docs.core.dao.jpa;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.sismics.docs.core.constant.AuditLogType;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.docs.core.util.AuditLogUtil;
import com.sismics.util.context.ThreadLocalContext;

/**
 * File DAO.
 * 
 * @author bgamard
 */
public class FileDao {
    /**
     * Creates a new file.
     * 
     * @param file File
     * @return New ID
     * @throws Exception
     */
    public String create(File file) {
        // Create the UUID
        file.setId(UUID.randomUUID().toString());
        
        // Create the file
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        file.setCreateDate(new Date());
        em.persist(file);
        
        // Create audit log
        AuditLogUtil.create(file, AuditLogType.CREATE);
        
        return file.getId();
    }
    
    /**
     * Returns the list of all files.
     * 
     * @return List of files
     */
    @SuppressWarnings("unchecked")
    public List<File> findAll() {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select f from File f where f.deleteDate is null");
        return q.getResultList();
    }
    
    /**
     * Returns the list of all files from a user.
     * 
     * @param userId User ID
     * @return List of files
     */
    @SuppressWarnings("unchecked")
    public List<File> findByUserId(String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select f from File f where f.userId = :userId and f.deleteDate is null");
        q.setParameter("userId", userId);
        return q.getResultList();
    }
    
    /**
     * Returns an active file.
     * 
     * @param id File ID
     * @return Document
     */
    public File getFile(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select f from File f where f.id = :id and f.deleteDate is null");
        q.setParameter("id", id);
        try {
            return (File) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Returns an active file.
     * 
     * @param id File ID
     * @param userId User ID
     * @return Document
     */
    public File getFile(String id, String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select f from File f where f.id = :id and f.userId = :userId and f.deleteDate is null");
        q.setParameter("id", id);
        q.setParameter("userId", userId);
        try {
            return (File) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Deletes a file.
     * 
     * @param id File ID
     */
    public void delete(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
            
        // Get the file
        Query q = em.createQuery("select f from File f where f.id = :id and f.deleteDate is null");
        q.setParameter("id", id);
        File fileDb = (File) q.getSingleResult();
        
        // Delete the file
        Date dateNow = new Date();
        fileDb.setDeleteDate(dateNow);
        
        // Create audit log
        AuditLogUtil.create(fileDb, AuditLogType.DELETE);
    }
    
    /**
     * Update a file.
     * 
     * @param file File to update
     * @return Updated file
     */
    public File update(File file) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Get the file
        Query q = em.createQuery("select f from File f where f.id = :id and f.deleteDate is null");
        q.setParameter("id", file.getId());
        File fileFromDb = (File) q.getSingleResult();

        // Update the file
        fileFromDb.setDocumentId(file.getDocumentId());
        fileFromDb.setContent(file.getContent());
        fileFromDb.setOrder(file.getOrder());
        fileFromDb.setMimeType(file.getMimeType());
        
        return file;
    }
    
    /**
     * Gets a file by its ID.
     * 
     * @param id File ID
     * @return File
     */
    public File getById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            return em.find(File.class, id);
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Get files by document ID or all orphan files of an user.
     * 
     * @parma userId User ID
     * @param documentId Document ID
     * @return List of files
     */
    @SuppressWarnings("unchecked")
    public List<File> getByDocumentId(String userId, String documentId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        if (documentId == null) {
            Query q = em.createQuery("select f from File f where f.documentId is null and f.deleteDate is null and f.userId = :userId order by f.createDate asc");
            q.setParameter("userId", userId);
            return q.getResultList();
        }
        Query q = em.createQuery("select f from File f where f.documentId = :documentId and f.deleteDate is null order by f.order asc");
        q.setParameter("documentId", documentId);
        return q.getResultList();
    }
}

package iuh.fit.hotelmanagementemployeeclient.utils;

import iuh.fit.hotelmanagementemployeeclient.utils.GlobalConstants;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Admin 1/16/2025
 **/
public class EntityManagerUtil {

    private static EntityManagerFactory emf;

    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null)
            emf = Persistence.createEntityManagerFactory(GlobalConstants.PERSISTENCE_UNIT_NAME);
        return emf;
    }

    public static EntityManager getEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    public static void close() {
        if (emf.isOpen()) {
            emf.close();
        }
    }
}

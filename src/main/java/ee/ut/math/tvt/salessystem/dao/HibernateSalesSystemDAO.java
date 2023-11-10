package ee.ut.math.tvt.salessystem.dao;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class HibernateSalesSystemDAO implements SalesSystemDAO {
    private final EntityManagerFactory emf;
    private final EntityManager em;
    public HibernateSalesSystemDAO() {
        // if you get ConnectException / JDBCConnectionException then you
        // probably forgot to start the database before starting the application
        emf = Persistence.createEntityManagerFactory("pos");
        em = emf.createEntityManager();
    }
    // TODO implement missing methods
    public void close() {
        em.close();
        emf.close();
    }
    @Override
    public void beginTransaction() {
        em.getTransaction().begin();
    }
    @Override
    public void rollbackTransaction() {
        em.getTransaction().rollback();
    }
    @Override
    public void commitTransaction() {
        em.getTransaction().commit();
    }
}

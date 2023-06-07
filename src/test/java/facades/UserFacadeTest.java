package facades;

import entities.User;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.*;
import utils.EMF_Creator;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

@NoArgsConstructor
public class UserFacadeTest {

    private static EntityManagerFactory emf;
    private static UserFacade facade;

    User user1, user2;

    @BeforeAll
    public static void setUpClass() {
        emf = EMF_Creator.createEntityManagerFactoryForTest();
        facade = UserFacade.getUserFacade(emf);
    }

    @AfterAll
    public static void tearDownClass() {

    }

    @BeforeEach
    public void setUp() {
        EntityManager em = emf.createEntityManager();

        user1 = new User("user1", "test123", "test", "test");
        user2 = new User("user2", "test123", "test", "test");

        try {
            em.getTransaction().begin();
            em.createNamedQuery("User.deleteAllRows").executeUpdate();
            em.persist(user1);
            em.persist(user2);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    @AfterEach
    public void tearDown() {
    }

    @Test
    public void countNumberOfWorkoutsInTestDB() {
        assertEquals(2, facade.countUsers(), "Expects two rows in the database");
    }
}

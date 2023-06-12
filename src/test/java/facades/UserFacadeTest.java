package facades;

import entities.User;
import errorhandling.API_Exception;
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

    @Test
    public void testGetUser() throws API_Exception {
        assertEquals(user1.getUserName(), facade.getUser(user1.getUserName()).getUserName());
    }

    @Test
    public void testEditFirstName() throws API_Exception {
        EntityManager em = emf.createEntityManager();
        User user = em.find(User.class, user1.getUserName());
        user.setFirstName("newFirstName");
        em.getTransaction().begin();
        em.merge(user);
        em.getTransaction().commit();
        em.close();

        assertEquals("newFirstName", facade.getUser(user1.getUserName()).getFirstName());
    }
}

package facades;

import dtos.UserDTO;
import entities.User;
import errorhandling.API_Exception;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.*;
import security.errorhandling.AuthenticationException;
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

    // Test of getVerifiedUser method
    @Test
    public void testGetVerifiedUserSuccess() throws AuthenticationException {
        User user = facade.getVerifiedUser("user1", "test123");
        assertEquals(user1.getUserName(), user.getUserName());
    }

    @Test
    public void testGetVerifiedUserFailure() {
        Assertions.assertThrows(AuthenticationException.class, () -> {
            facade.getVerifiedUser("user1", "test1234");
        });
    }

    // Test of createUser method
    @Test
    public void testCreateUserSuccess() throws API_Exception {
        User user = new User("user3", "test123", "test", "test");
        UserDTO userDTO = new UserDTO(user);
        UserDTO createdUser = facade.createUser(userDTO);
        assertEquals(user.getUserName(), createdUser.getUser_name());
    }

    @Test
    public void testCreateUserFailure() {
        Assertions.assertThrows(API_Exception.class, () -> {
            facade.createUser(new UserDTO("user1", "test123", "test", "test"));
        });
    }

    // Test of getAllUsers method
    @Test
    public void testGetAllUsersSuccess() {
        assertEquals(2, facade.getAllUsers().size());
    }

    // Test of deleteUser method
    @Test
    public void testDeleteUserSuccess() throws API_Exception {
        facade.deleteUser(user1.getUserName());
        assertEquals(1, facade.getAllUsers().size());
    }

    @Test
    public void testDeleteUserFailure() {
        Assertions.assertThrows(API_Exception.class, () -> {
            facade.deleteUser("user3");
        });
    }

    // Test of editUser method
    @Test
    public void testEditUserSuccess() throws API_Exception {
        UserDTO userDTO = new UserDTO(user1);
        userDTO.setFirstName("test5");
        UserDTO editedUser = facade.editUser(userDTO);
        assertEquals("test5", facade.getUserByUsername("user1").getFirstName());
    }

    @Test
    public void testEditUserFailure() {
        Assertions.assertThrows(API_Exception.class, () -> {
            UserDTO userDTO = new UserDTO("test", "test", "test", "test");
            userDTO.setUser_name("user3");
            facade.editUser(userDTO);
        });
    }

}

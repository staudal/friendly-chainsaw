package facades;

import dtos.ShowDTO;
import dtos.UserDTO;
import entities.Festival;
import entities.Show;
import entities.User;
import errorhandling.API_Exception;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.*;
import utils.EMF_Creator;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.time.LocalDate;

import static junit.framework.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@NoArgsConstructor
public class ShowFacadeTest {

    private static EntityManagerFactory emf;
    private static ShowFacade facade;

    Show show1, show2;
    Festival festival1;
    User user1;

    @BeforeAll
    public static void setUpClass() {
        emf = EMF_Creator.createEntityManagerFactoryForTest();
        facade = ShowFacade.getShowFacade(emf);
    }

    @AfterAll
    public static void tearDownClass() {

    }

    @BeforeEach
    public void setUp() {
        EntityManager em = emf.createEntityManager();

        show1 = new Show("test1", 60, LocalDate.of(2021, 1, 1));
        show2 = new Show("test2", 90, LocalDate.of(2022, 2, 3));

        user1 = new User("tester", "testPass", "testFirstName", "testLastName");

        user1.getShows().add(show1);
        show1.getGuests().add(user1);

        festival1 = new Festival("testFestival", "Roskilde", LocalDate.of(2021, 6, 26), LocalDate.of(2021, 7, 3));

        user1.getFestivals().add(festival1);
        festival1.getGuests().add(user1);

        show1.setFestival(festival1);
        festival1.getShows().add(show1);

        try {
            em.getTransaction().begin();
            em.persist(user1);
            em.persist(show1);
            em.persist(show2);
            em.persist(festival1);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    @AfterEach
    public void tearDown() {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.createNamedQuery("Show.deleteAllRows").executeUpdate();
            em.createNamedQuery("User.deleteAllRows").executeUpdate();
            em.createNamedQuery("Festival.deleteAllRows").executeUpdate();
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    // Test of getAllShows method
    @Test
    public void testGetAllShowsSuccess() {
        assertEquals(2, facade.getAllShows().size());
    }

    // Test of createNewShow method
    @Test
    public void testCreateNewShowSuccess() throws API_Exception {
        ShowDTO show3 = new ShowDTO();
        show3.setName("test3");
        show3.setDuration(120);
        show3.setDate(LocalDate.of(2023, 3, 3).toString());
        show3.setFestival(festival1.getId());
        facade.createNewShow(show3);

        assertEquals(3, facade.getAllShows().size());
    }

    // Test of editShow method
    @Test
    public void testEditShowSuccess() throws API_Exception {
        ShowDTO show3 = new ShowDTO();
        show3.setName("test3");
        show3.setDuration(120);
        show3.setDate(LocalDate.of(2023, 3, 3).toString());
        show3.setFestival(festival1.getId());
        ShowDTO showDTO = facade.createNewShow(show3);

        showDTO.setName("test4");
        facade.editShow(showDTO.getId(), showDTO);

        assertEquals("test4", facade.getAllShows().get(2).getName());
    }

    @Test
    public void testEditShowFailure() throws API_Exception {
        ShowDTO show3 = new ShowDTO();
        show3.setName("test3");
        show3.setDuration(120);
        show3.setDate(LocalDate.of(2023, 3, 3).toString());
        show3.setFestival(festival1.getId());
        ShowDTO showDTO = facade.createNewShow(show3);

        showDTO.setName("test4");
        showDTO.setFestival(999L);

        assertThrows(API_Exception.class, () -> {
            facade.editShow(showDTO.getId(), showDTO);
        });
    }

    // Test of deleteShow method
    @Test
    public void testDeleteShowSuccess() throws API_Exception {
        facade.deleteShow(show1.getId());

        assertEquals(1, facade.getAllShows().size());
    }

    @Test
    public void testDeleteShowFailure() {
        assertThrows(API_Exception.class, () -> {
            facade.deleteShow(999L);
        });
    }

    // Test of removeUserFromShow method
    @Test
    public void testRemoveUserFromShowSuccess() throws API_Exception {
        UserDTO userDTO = new UserDTO(user1);
        facade.removeUserFromShow(show1.getId(), userDTO);

        assertEquals(0, facade.getShowsByUsername(user1.getUserName()).size());
    }

    @Test
    public void testRemoveUserFromShowFailure() {
        UserDTO userDTO = new UserDTO(user1);
        userDTO.setUser_name("testUser2");

        assertThrows(API_Exception.class, () -> {
            facade.removeUserFromShow(show1.getId(), userDTO);
        });
    }

    // Test of addUserToShow method
    @Test
    public void testAddUserToShowSuccess() throws API_Exception {
        UserDTO userDTO = new UserDTO(user1);
        facade.addUserToShow(show2.getId(), userDTO);

        assertEquals(2, facade.getShowsByUsername(user1.getUserName()).size());
    }

    @Test
    public void testAddUserToShowFailure() {
        UserDTO userDTO = new UserDTO(user1);
        userDTO.setUser_name("testUser2");

        assertThrows(API_Exception.class, () -> {
            facade.addUserToShow(show2.getId(), userDTO);
        });
    }

    // Test of getPossibleShowsByUsername method
    @Test
    public void testGetPossibleShowsByUsernameSuccess() throws API_Exception {
        assertEquals(1, facade.getPossibleShowsByUsername(user1.getUserName()).size());
    }

    @Test
    public void testGetPossibleShowsByUsernameFailure() {
        assertThrows(API_Exception.class, () -> {
            facade.getPossibleShowsByUsername("testUser2");
        });
    }

    // Test of getShowsByUsername method
    @Test
    public void testGetShowsByUsernameSuccess() throws API_Exception {
        assertEquals(1, facade.getShowsByUsername(user1.getUserName()).size());
    }

    @Test
    public void testGetShowsByUsernameFailure() {
        assertThrows(API_Exception.class, () -> {
            facade.getShowsByUsername("testUser2");
        });
    }

}

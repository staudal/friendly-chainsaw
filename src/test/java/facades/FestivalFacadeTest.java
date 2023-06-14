package facades;

import dtos.FestivalDTO;
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
public class FestivalFacadeTest {
    private static EntityManagerFactory emf;
    private static FestivalFacade facade;

    Show show1, show2;
    Festival festival1, festival2;
    User user1;

    @BeforeAll
    public static void setUpClass() {
        emf = EMF_Creator.createEntityManagerFactoryForTest();
        facade = FestivalFacade.getFestivalFacade(emf);
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
        festival2 = new Festival("testFestival2", "Roskilde", LocalDate.of(2021, 6, 26), LocalDate.of(2021, 7, 3));

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
            em.persist(festival2);
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

    // Test of getAllFestivals method
    @Test
    public void testGetAllFestivalsSuccess() {
        assertEquals(2, facade.getAllFestivals().size());
    }

    // Test of createNewFestival method
    @Test
    public void testCreateNewFestivalSuccess() throws API_Exception {
        FestivalDTO festivalDTO = new FestivalDTO("testFestival2", "Roskilde", LocalDate.of(2021, 6, 26).toString(), LocalDate.of(2021, 7, 3).toString());
        facade.createNewFestival(festivalDTO);
        assertEquals(3, facade.getAllFestivals().size());
    }

    // Test of editFestival method
    @Test
    public void testEditFestivalSuccess() throws API_Exception {
        FestivalDTO festivalDTO = new FestivalDTO(festival1);
        festivalDTO.setName("testFestival2");
        facade.editFestival(festivalDTO.getId(), festivalDTO);
        assertEquals("testFestival2", facade.getAllFestivals().get(0).getName());
    }

    @Test
    public void testEditFestivalFailure() {
        FestivalDTO festivalDTO = new FestivalDTO(festival1);
        festivalDTO.setName("testFestival2");
        assertThrows(API_Exception.class, () -> {
            facade.editFestival(1000L, festivalDTO);
        });
    }

    // Test of deleteFestival method
    @Test
    public void testDeleteFestivalSuccess() throws API_Exception {
        facade.deleteFestival(festival1.getId());
        assertEquals(1, facade.getAllFestivals().size());
    }

    @Test
    public void testDeleteFestivalFailure() {
        assertThrows(API_Exception.class, () -> {
            facade.deleteFestival(1000L);
        });
    }

    // Test of getFestivalsByUser method
    @Test
    public void testGetFestivalsByUserSuccess() throws API_Exception {
        assertEquals(1, facade.getFestivalsByUser(user1.getUserName()).size());
    }

    @Test
    public void testGetFestivalsByUserFailure() {
        assertThrows(API_Exception.class, () -> {
            facade.getFestivalsByUser("test");
        });
    }

    // Test of removeUserFromFestival method
    @Test
    public void testRemoveUserFromFestivalSuccess() throws API_Exception {
        UserDTO userDTO = new UserDTO(user1);
        facade.removeUserFromFestival(festival1.getId(), userDTO);
        assertEquals(0, facade.getFestivalsByUser(user1.getUserName()).size());
    }

    @Test
    public void testRemoveUserFromFestivalFailure() {
        UserDTO userDTO = new UserDTO(user1);
        assertThrows(API_Exception.class, () -> {
            facade.removeUserFromFestival(1000L, userDTO);
        });
    }

    // Test of addUserToFestival method
    @Test
    public void testAddUserToFestivalSuccess() throws API_Exception {
        UserDTO userDTO = new UserDTO(user1);
        facade.addUserToFestival(festival2.getId(), userDTO);
        assertEquals(2, facade.getFestivalsByUser(user1.getUserName()).size());
    }

    @Test
    public void testAddUserToFestivalFailure() {
        UserDTO userDTO = new UserDTO(user1);
        assertThrows(API_Exception.class, () -> {
            facade.addUserToFestival(1000L, userDTO);
        });
    }

}

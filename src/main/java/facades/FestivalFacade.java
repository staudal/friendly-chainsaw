package facades;

import dtos.FestivalDTO;
import dtos.UserDTO;
import entities.Festival;
import entities.Show;
import entities.User;
import errorhandling.API_Exception;
import lombok.NoArgsConstructor;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class FestivalFacade {
    private static EntityManagerFactory emf;
    private static FestivalFacade instance;

    public static FestivalFacade getFestivalFacade(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new FestivalFacade();
        }
        return instance;
    }

    public List<FestivalDTO> getAllFestivals() {
        EntityManager em = emf.createEntityManager();
        try {
            return FestivalDTO.getFestivalDTOs(em.createQuery("SELECT f FROM Festival f", Festival.class).getResultList());
        } finally {
            em.close();
        }
    }

    public FestivalDTO getFestivalById(Long id) throws API_Exception {
        EntityManager em = emf.createEntityManager();
        try {
            return new FestivalDTO(em.find(Festival.class, id));
        } catch (Exception e) {
            throw new API_Exception("No festival with provided id found", 404);
        } finally {
            em.close();
        }
    }

    public FestivalDTO addFestival(FestivalDTO festivalToAdd) throws API_Exception {
        EntityManager em = emf.createEntityManager();
        Festival festival = new Festival(festivalToAdd);
        try {
            em.getTransaction().begin();
            em.persist(festival);
            em.getTransaction().commit();
            return new FestivalDTO(festival);
        } catch (Exception e) {
            throw new API_Exception("Could not add festival", 400);
        } finally {
            em.close();
        }
    }

    public FestivalDTO editFestival(Long id, FestivalDTO festivalToEdit) throws API_Exception {
        EntityManager em = emf.createEntityManager();
        Festival festival = em.find(Festival.class, id);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            em.getTransaction().begin();

            if (!festivalToEdit.getName().equals("")) {
                festival.setName(festivalToEdit.getName());
            }

            if (!festivalToEdit.getCity().equals("")) {
                festival.setCity(festivalToEdit.getCity());
            }

            if (!festivalToEdit.getStartDate().equals("")) {
                festival.setStartDate(LocalDate.parse(festivalToEdit.getStartDate(), formatter));
            }

            if (!festivalToEdit.getEndDate().equals("")) {
                festival.setEndDate(LocalDate.parse(festivalToEdit.getEndDate(), formatter));
            }

            em.getTransaction().commit();
            return new FestivalDTO(festival);
        } catch (Exception e) {
            throw new API_Exception("Could not edit festival", 400);
        } finally {
            em.close();
        }
    }

    public FestivalDTO deleteFestival(Long id) throws API_Exception {
        EntityManager em = emf.createEntityManager();
        Festival festival = em.find(Festival.class, id);
        List<User> guests = festival.getGuests();
        List<Show> shows = festival.getShows();
        try {
            em.getTransaction().begin();

            // Remove guests from festival
            for (User guest : guests) {
                guest.getFestivals().remove(festival);
            }

            // Remove shows from festival
            for (Show show : shows) {
                em.remove(show);
            }

            em.remove(festival);

            em.getTransaction().commit();
            return new FestivalDTO(festival);
        } catch (Exception e) {
            throw new API_Exception("Could not delete festival", 400);
        } finally {
            em.close();
        }
    }

    public List<UserDTO> getFestivalGuests(Long id) {
        EntityManager em = emf.createEntityManager();
        List<UserDTO> guests = new ArrayList<>();
        try {
            Festival festival = em.find(Festival.class, id);
            for (User user : festival.getGuests()) {
                guests.add(new UserDTO(user));
            }
            return guests;
        } finally {
            em.close();
        }
    }

    public List<FestivalDTO> getFestivalsByUser(String username) {
        EntityManager em = emf.createEntityManager();
        List<FestivalDTO> festivals = new ArrayList<>();
        try {
            User user = em.find(User.class, username);
            for (Festival festival : user.getFestivals()) {
                festivals.add(new FestivalDTO(festival));
            }
            return festivals;
        } finally {
            em.close();
        }
    }

    public FestivalDTO removeUserFromFestival(Long id, UserDTO userToRemove) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            Festival festival = em.find(Festival.class, id);
            User user = em.find(User.class, userToRemove.getUser_name());

            festival.getGuests().remove(user);
            user.getFestivals().remove(festival);

            // remove festival shows from user
            for (Show show : festival.getShows()) {
                user.getShows().remove(show);
            }

            // remove user from festival shows
            for (Show show : festival.getShows()) {
                show.getGuests().remove(user);
            }

            em.getTransaction().commit();
            return new FestivalDTO(festival);
        } finally {
            em.close();
        }
    }

    public FestivalDTO addUserToFestival(Long id, UserDTO userToAdd) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            Festival festival = em.find(Festival.class, id);
            User user = em.find(User.class, userToAdd.getUser_name());

            festival.getGuests().add(user);
            user.getFestivals().add(festival);

            em.getTransaction().commit();
            return new FestivalDTO(festival);
        } finally {
            em.close();
        }
    }
}

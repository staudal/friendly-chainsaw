package facades;

import dtos.FestivalDTO;
import dtos.UserDTO;
import entities.Festival;
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
        try {
            em.getTransaction().begin();
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
}

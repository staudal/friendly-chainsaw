package facades;

import dtos.FestivalDTO;
import dtos.ShowDTO;
import dtos.UserDTO;
import entities.Festival;
import entities.Show;
import entities.User;
import errorhandling.API_Exception;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ShowFacade {

    private static EntityManagerFactory emf;
    private static ShowFacade instance;

    private ShowFacade() {
    }

    public static ShowFacade getShowFacade(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new ShowFacade();
        }
        return instance;
    }


    public List<ShowDTO> getAllShows() {
        EntityManager em = emf.createEntityManager();
        try {
            return ShowDTO.getShowDTOs(em.createQuery("SELECT s FROM Show s", Show.class).getResultList());
        } finally {
            em.close();
        }
    }

    public ShowDTO addShow(ShowDTO showDTO) {
        EntityManager em = emf.createEntityManager();
        Show show = new Show(showDTO);

        Festival festival = em.find(Festival.class, showDTO.getFestival());

        try {
            em.getTransaction().begin();
            festival.getShows().add(show);
            show.setFestival(festival);
            em.persist(show);
            em.getTransaction().commit();
            return new ShowDTO(show);
        } finally {
            em.close();
        }
    }

    public ShowDTO editShow(Long id, ShowDTO showToEdit) throws API_Exception {
        EntityManager em = emf.createEntityManager();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try {
            em.getTransaction().begin();
            Show show = em.find(Show.class, id);

            if (!showToEdit.getName().equals("")) {
                show.setName(showToEdit.getName());
            }

            if (showToEdit.getDuration() != 0) {
                show.setDuration(showToEdit.getDuration());
            }

            if (showToEdit.getDate() != null) {
                show.setDate(LocalDate.parse(showToEdit.getDate(), formatter));
            }

            if (showToEdit.getFestival() != null) {

                // removing show from old festival
                Festival oldFestival = em.find(Festival.class, show.getFestival().getId());
                oldFestival.getShows().remove(show);

                // removing show from users
                for (User user : show.getGuests()) {
                    user.getShows().remove(show);
                }

                // adding show to new festival
                Festival newFestival = em.find(Festival.class, showToEdit.getFestival());
                newFestival.getShows().add(show);
                show.setFestival(newFestival);
            }

            em.getTransaction().commit();
            return new ShowDTO(show);
        } finally {
            em.close();
        }
    }

    public Show deleteShow(Long id) {
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();
            Show show = em.find(Show.class, id);

            // removing show from users
            for (User user : show.getGuests()) {
                user.getShows().remove(show);
            }

            // removing show from festival
            if (show.getFestival() != null) {
                show.getFestival().getShows().remove(show);
            }

            em.remove(show);
            em.getTransaction().commit();
            return show;
        } finally {
            em.close();
        }
    }

    public List<ShowDTO> getAllShowsByUser(String username) {
        EntityManager em = emf.createEntityManager();
        User user = em.find(User.class, username);
        try {
            return ShowDTO.getShowDTOs(user.getShows());
        } finally {
            em.close();
        }
    }

    public ShowDTO removeShowFromUser(Long id, UserDTO user) {
        EntityManager em = emf.createEntityManager();
        User userToRemove = em.find(User.class, user.getUser_name());
        Show show = em.find(Show.class, id);
        try {
            em.getTransaction().begin();
            userToRemove.getShows().remove(show);
            show.getGuests().remove(userToRemove);
            em.getTransaction().commit();
            return new ShowDTO(show);
        } finally {
            em.close();
        }
    }

    public ShowDTO addShowToUser(Long id, UserDTO user) {
        EntityManager em = emf.createEntityManager();
        User userToAdd = em.find(User.class, user.getUser_name());
        Show show = em.find(Show.class, id);
        try {
            em.getTransaction().begin();
            userToAdd.getShows().add(show);
            show.getGuests().add(userToAdd);
            em.getTransaction().commit();
            return new ShowDTO(show);
        } finally {
            em.close();
        }
    }


    public List<ShowDTO> getShowsByFestival(Long id) {
        EntityManager em = emf.createEntityManager();
        Festival festival = em.find(Festival.class, id);
        try {
            return ShowDTO.getShowDTOs(festival.getShows());
        } finally {
            em.close();
        }
    }

    public List<ShowDTO> getPossibleShowsByUsername(String username) {
        EntityManager em = emf.createEntityManager();
        FestivalFacade festivalFacade = FestivalFacade.getFestivalFacade(emf);
        List<ShowDTO> shows = new ArrayList<>();

        try {
            List<Long> festivalIDs = new ArrayList<>();

            for (FestivalDTO festival : festivalFacade.getFestivalsByUser(username)) {
                festivalIDs.add(festival.getId());
            }

            for (Long id : festivalIDs) {
                shows.addAll(getShowsByFestival(id));
            }

            return shows;

        } finally {
            em.close();
        }

    }

    public List<ShowDTO> getShowsByUsername(String username) {
        EntityManager em = emf.createEntityManager();
        User user = em.find(User.class, username);
        try {
            return ShowDTO.getShowDTOs(user.getShows());
        } finally {
            em.close();
        }
    }
}

package facades;

import dtos.UserDTO;
import entities.Role;
import entities.User;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import security.errorhandling.AuthenticationException;

import java.util.List;

public class UserFacade {

    private static EntityManagerFactory emf;
    private static UserFacade instance;

    private UserFacade() {
    }

    public static UserFacade getUserFacade(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new UserFacade();
        }
        return instance;
    }

    public User getVerifiedUser(String username, String password) throws AuthenticationException {
        EntityManager em = emf.createEntityManager();
        User user;
        try {
            user = em.find(User.class, username);
            if (user == null || !user.verifyPassword(password)) {
                throw new AuthenticationException("Invalid user name or password");
            }
        } finally {
            em.close();
        }
        return user;
    }

    public UserDTO createUser(UserDTO userDTO) {
        EntityManager em = emf.createEntityManager();
        User user = new User(userDTO.getUser_name(), userDTO.getUser_pass(), userDTO.getFirstName(), userDTO.getLastName());
        user.addRole(new Role("user"));
        try {
            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
        return new UserDTO(user);
    }

    public Long countUsers() {
        EntityManager em = emf.createEntityManager();
        try {
            return (Long) em.createQuery("SELECT COUNT(u) FROM User u").getSingleResult();
        } finally {
            em.close();
        }
    }

    public List<UserDTO> getAllUsers() {
        EntityManager em = emf.createEntityManager();
        List<User> users;

        try {
            users = em.createQuery("SELECT u FROM User u", User.class).getResultList();
        } finally {
            em.close();
        }

        return UserDTO.getUsersDTO(users);

    }

    public UserDTO deleteUser(String user_name) {
        EntityManager em = emf.createEntityManager();
        User user = em.find(User.class, user_name);
        try {
            em.getTransaction().begin();
            em.remove(user);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
        return new UserDTO(user);
    }
}

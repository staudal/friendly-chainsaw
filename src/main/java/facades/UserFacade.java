package facades;

import dtos.UserDTO;
import entities.Role;
import entities.User;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import errorhandling.API_Exception;
import lombok.NoArgsConstructor;
import security.errorhandling.AuthenticationException;

import java.util.List;

@NoArgsConstructor
public class UserFacade {

    private static EntityManagerFactory emf;
    private static UserFacade instance;

    public static UserFacade getUserFacade(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new UserFacade();
        }
        return instance;
    }

    // Used for testing purposes only
    public User getUserByUsername(String username) {
        EntityManager em = emf.createEntityManager();
        User user;
        try {
            user = em.find(User.class, username);
        } finally {
            em.close();
        }
        return user;
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

    public UserDTO createUser(UserDTO userDTO) throws API_Exception {
        EntityManager em = emf.createEntityManager();
        User user = new User(userDTO.getUser_name(), userDTO.getUser_pass(), userDTO.getFirstName(), userDTO.getLastName());
        Role role = new Role("user");
        try {
            em.getTransaction().begin();

            // if role already exists, use that one instead
            Role existingRole = em.find(Role.class, role.getRoleName());

            if (existingRole != null) {
                role = existingRole;
            }

            user.addRole(role);
            role.getUserList().add(user);

            em.persist(user);

            if (existingRole == null) {
                em.persist(role);
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            throw new API_Exception("Username already exists", 400);
        } finally {
            em.close();
        }
        return new UserDTO(user);
    }

    public List<UserDTO> getAllUsers() {
        EntityManager em = emf.createEntityManager();
        List<User> users;

        try {
            users = em.createQuery("SELECT u FROM User u", User.class).getResultList();
        } finally {
            em.close();
        }

        return UserDTO.getUserDTOs(users);
    }

    public UserDTO deleteUser(String user_name) throws API_Exception {
        EntityManager em = emf.createEntityManager();
        User user = em.find(User.class, user_name);
        try {
            em.getTransaction().begin();
            em.remove(user);
            em.getTransaction().commit();
        } catch (Exception e) {
            throw new API_Exception("Could not delete user", 400);
        } finally {
            em.close();
        }
        return new UserDTO(user);
    }

    public UserDTO editUser(UserDTO userDTO) throws API_Exception {
        EntityManager em = emf.createEntityManager();

        User user = em.find(User.class, userDTO.getUser_name());

        if (user == null) {
            throw new API_Exception("User not found", 404);
        }

        if (!userDTO.getFirstName().equals("")) {
            user.setFirstName(userDTO.getFirstName());
        }

        if (!userDTO.getLastName().equals("")) {
            user.setLastName(userDTO.getLastName());
        }

        try {
            em.getTransaction().begin();
            em.merge(user);
            em.getTransaction().commit();
        } catch (Exception e) {
            throw new API_Exception("Could not edit user", 400);
        } finally {
            em.close();
        }
        return new UserDTO(user.getUserName(), user.getFirstName(), user.getLastName());
    }
}

package utils;


import entities.Festival;
import entities.Role;
import entities.User;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.time.LocalDate;

public class SetupTestUsers {

  public static void main(String[] args) {

    EntityManagerFactory emf = EMF_Creator.createEntityManagerFactory();
    EntityManager em = emf.createEntityManager();

    User user = new User("user", "test123", "test", "test");
    User admin = new User("admin", "test123", "test", "test");

    Role userRole = new Role("user");
    Role adminRole = new Role("admin");

    Festival festival = new Festival("Roskilde", "Roskilde", LocalDate.of(2021, 6, 26), LocalDate.of(2021, 7, 3));

    em.getTransaction().begin();

    user.addRole(userRole);
    admin.addRole(adminRole);

    festival.getGuests().add(user);
    user.setFestival(festival);

    em.persist(userRole);
    em.persist(adminRole);

    em.persist(user);
    em.persist(admin);

    em.persist(festival);

    em.getTransaction().commit();
    System.out.println("PW: " + user.getUserPass());
    System.out.println("Testing user with OK password: " + user.verifyPassword("test"));
    System.out.println("Testing user with wrong password: " + user.verifyPassword("test1"));
    System.out.println("Created TEST Users");
   
  }

}

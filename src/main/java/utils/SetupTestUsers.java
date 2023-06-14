package utils;


import entities.Festival;
import entities.Role;
import entities.Show;
import entities.User;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.time.LocalDate;

public class SetupTestUsers {

  public static void main(String[] args) {

    EntityManagerFactory emf = EMF_Creator.createEntityManagerFactory();
    EntityManager em = emf.createEntityManager();

    em.getTransaction().begin();

    User user = new User("user", "test123", "test", "test");
    User admin = new User("admin", "test123", "test", "test");

    Festival festival = new Festival("Roskilde Festival", "Roskilde", LocalDate.of(2023, 5, 6), LocalDate.of(2023, 5, 8));

    Show show = new Show("Metallica", 120, LocalDate.of(2023, 5, 6));

    Role userRole = new Role("user");
    Role adminRole = new Role("admin");

    user.addRole(userRole);
    admin.addRole(adminRole);

    festival.getShows().add(show);
    show.setFestival(festival);

    user.getFestivals().add(festival);
    festival.getGuests().add(user);

    user.getShows().add(show);
    show.getGuests().add(user);

    em.persist(userRole);
    em.persist(adminRole);
    em.persist(user);
    em.persist(admin);
    em.persist(festival);
    em.persist(show);

    em.getTransaction().commit();
    System.out.println("PW: " + user.getUserPass());
    System.out.println("Testing user with OK password: " + user.verifyPassword("test"));
    System.out.println("Testing user with wrong password: " + user.verifyPassword("test1"));
    System.out.println("Created TEST Users");
   
  }

}

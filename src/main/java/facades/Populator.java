package facades;

import entities.Festival;
import utils.EMF_Creator;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.time.LocalDate;

public class Populator {
    public static void populate(){
        EntityManagerFactory emf = EMF_Creator.createEntityManagerFactory();
        EntityManager em = emf.createEntityManager();

        Festival festival = new Festival();
        festival.setName("Roskilde Festival");
        festival.setCity("Roskilde");
        festival.setStartDate(LocalDate.of(2021, 6, 26));
        festival.setEndDate(LocalDate.of(2021, 7, 3));

        em.getTransaction().begin();
        em.persist(festival);
        em.getTransaction().commit();

        em.close();
    }

    public static void main(String[] args) {
        populate();
    }
}

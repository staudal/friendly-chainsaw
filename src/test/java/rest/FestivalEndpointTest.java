package rest;

import entities.Festival;
import entities.Role;
import entities.Show;
import entities.User;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.EMF_Creator;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class FestivalEndpointTest {
    private static final int SERVER_PORT = 7777;
    private static final String SERVER_URL = "http://localhost/api";

    static final URI BASE_URI = UriBuilder.fromUri(SERVER_URL).port(SERVER_PORT).build();
    private static HttpServer httpServer;
    private static EntityManagerFactory emf;

    //This is how we hold on to the token after login, similar to that a client must store the token somewhere
    private static String securityToken;

    User user1, user2;
    Show show1, show2;
    Festival festival1, festival2;

    static HttpServer startServer() {
        ResourceConfig rc = ResourceConfig.forApplication(new ApplicationConfig());
        return GrizzlyHttpServerFactory.createHttpServer(BASE_URI, rc);
    }

    @BeforeAll
    public static void setUpClass() {
        //This method must be called before you request the EntityManagerFactory
        EMF_Creator.startREST_TestWithDB();
        emf = EMF_Creator.createEntityManagerFactoryForTest();

        httpServer = startServer();
        //Setup RestAssured
        RestAssured.baseURI = SERVER_URL;
        RestAssured.port = SERVER_PORT;
        RestAssured.defaultParser = Parser.JSON;
    }

    @AfterAll
    public static void closeTestServer() {
        //Don't forget this, if you called its counterpart in @BeforeAll
        EMF_Creator.endREST_TestWithDB();

        httpServer.shutdownNow();
    }

    @BeforeEach
    public void setUp() {
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM User").executeUpdate();
            em.createQuery("DELETE FROM Role").executeUpdate();
            em.createQuery("DELETE FROM Show").executeUpdate();
            em.createQuery("DELETE FROM Festival").executeUpdate();

            user1 = new User("admin", "test123", "jakob", "staudal");
            user2 = new User("user", "test123", "bokaj", "laduats");

            Role userRole = new Role("admin");
            Role adminRole = new Role("user");

            user1.addRole(userRole);
            user2.addRole(adminRole);

            festival1 = new Festival("Roskilde Festival", "Roskilde", LocalDate.of(2023, 5, 2), LocalDate.of(2023, 5, 6));
            festival2 = new Festival("Smukfest", "Skanderborg", LocalDate.of(2023, 5, 2), LocalDate.of(2023, 5, 6));

            show1 = new Show("Avatar 3", 124, LocalDate.of(2023, 5, 2));
            show2 = new Show("Avatar 4", 124, LocalDate.of(2027, 5, 2));

            festival1.getGuests().add(user1);
            festival2.getGuests().add(user2);

            user1.getFestivals().add(festival1);
            user2.getFestivals().add(festival2);

            festival1.getShows().add(show1);
            festival2.getShows().add(show2);

            show1.setFestival(festival1);
            show2.setFestival(festival2);

            show1.getGuests().add(user1);
            user1.getShows().add(show1);

            em.persist(userRole);
            em.persist(adminRole);
            em.persist(user1);
            em.persist(user2);
            em.persist(festival1);
            em.persist(festival2);
            em.persist(show1);
            em.persist(show2);

            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    //Utility method to login and set the returned securityToken
    private static void login(String role, String password) {
        String json = String.format("{username: \"%s\", password: \"%s\"}", role, password);
        securityToken = given()
                .contentType("application/json")
                .body(json)
                //.when().post("/api/login")
                .when().post("/login")
                .then()
                .extract().path("token");
        //System.out.println("TOKEN ---> " + securityToken);
    }

    // Test of getAllFestivals endpoint
    @Test
    public void testGetAllFestivals() {
        login("admin", "test123");
        given()
                .contentType("application/json")
                .header("x-access-token", securityToken)
                .when()
                .get("/festivals")
                .then()
                .statusCode(200)
                .body("size()", equalTo(2));
    }

    // Test of getFestivalsByUser endpoint
    @Test
    public void testGetFestivalsByUser() {
        login("user", "test123");
        given()
                .contentType("application/json")
                .header("x-access-token", securityToken)
                .when()
                .get("/festivals/user/" + user1.getUserName())
                .then()
                .statusCode(200)
                .body("size()", equalTo(1));
    }

    // Test of createNewFestival endpoint
    @Test
    public void testCreateNewFestival() {
        login("admin", "test123");
        given()
                .contentType("application/json")
                .header("x-access-token", securityToken)
                .body("{\"name\": \"Test Festival\", \"city\": \"Test City\", \"startDate\": \"2023-05-02\", \"endDate\": \"2023-05-06\"}")
                .when()
                .post("/festivals")
                .then()
                .statusCode(200)
                .body("name", equalTo("Test Festival"));
    }

    // Test of editFestival endpoint
    @Test
    public void testEditFestival() {
        login("admin", "test123");
        given()
                .contentType("application/json")
                .header("x-access-token", securityToken)
                .body("{\"name\": \"Test Festival EDITED\", \"city\": \"\", \"startDate\": \"\", \"endDate\": \"\"}")
                .when()
                .put("/festivals/" + festival1.getId())
                .then()
                .statusCode(200)
                .body("name", equalTo("Test Festival EDITED"));
    }

    // Test of deleteFestival endpoint
    @Test
    public void testDeleteFestival() {
        login("admin", "test123");
        given()
                .contentType("application/json")
                .header("x-access-token", securityToken)
                .when()
                .delete("/festivals/" + festival1.getId())
                .then()
                .statusCode(200)
                .body("name", equalTo("Roskilde Festival"));
    }

    // Test of addUserToFestival endpoint
    @Test
    public void testAddUserToFestival() {
        login("user", "test123");
        given()
                .contentType("application/json")
                .header("x-access-token", securityToken)
                .body("{\"user_name\": \"user\"}")
                .when()
                .put("/festivals/user/add/" + festival1.getId())
                .then()
                .statusCode(200)
                .body("name", equalTo("Roskilde Festival"));
    }

    // Test of removeUserFromFestival endpoint
    @Test
    public void testRemoveUserFromFestival() {
        login("user", "test123");
        given()
                .contentType("application/json")
                .header("x-access-token", securityToken)
                .body("{\"user_name\": \"admin\"}")
                .when()
                .put("/festivals/user/remove/" + festival1.getId())
                .then()
                .statusCode(200)
                .body("name", equalTo("Roskilde Festival"));
    }
}

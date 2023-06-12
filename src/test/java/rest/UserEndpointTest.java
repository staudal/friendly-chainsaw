package rest;

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


import static io.restassured.RestAssured.given;

public class UserEndpointTest {
    private static final int SERVER_PORT = 7777;
    private static final String SERVER_URL = "http://localhost/api";

    static final URI BASE_URI = UriBuilder.fromUri(SERVER_URL).port(SERVER_PORT).build();
    private static HttpServer httpServer;
    private static EntityManagerFactory emf;

    User user1, user2;

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

    // Setup the DataBase (used by the test-server and this test) in a known state BEFORE EACH TEST
    //TODO -- Make sure to change the EntityClass used below to use YOUR OWN (renamed) Entity class
    @BeforeEach
    public void setUp() {
        EntityManager em = emf.createEntityManager();

        user1 = new User("user1", "test123", "test", "test");
        user2 = new User("user2", "test123", "test", "test");

        try {
            em.getTransaction().begin();
            em.createNamedQuery("User.deleteAllRows").executeUpdate();
            em.persist(user1);
            em.persist(user2);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    @Test
    public void testServerIsUp() {
        System.out.println("Testing is server UP");
        given()
                .when()
                .get("/users")
                .then()
                .statusCode(200);
    }

    @Test
    public void testCount() {
        given()
                .contentType("application/json")
                .get("/users").then()
                .assertThat()
                .statusCode(200).body("size()", org.hamcrest.Matchers.is(2));
    }

    @Test
    public void testGetAllUsers() {
        given()
                .contentType("application/json")
                .get("/users").then()
                .assertThat()
                .statusCode(200)
                .body("user_name", org.hamcrest.Matchers.hasItems("user1", "user2"));
    }

    @Test
    public void testEditFirstName() {
        given()
                .contentType("application/json")
                .body("{\"firstName\": \"test2\"}")
                .put("/users/edit/user1").then()
                .assertThat()
                .statusCode(200)
                .body("firstName", org.hamcrest.Matchers.is("test2"));
    }
}

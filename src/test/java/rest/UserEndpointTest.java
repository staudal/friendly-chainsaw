package rest;

import entities.Role;
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
import java.util.ArrayList;


import static io.restassured.RestAssured.given;

public class UserEndpointTest {
    private static final int SERVER_PORT = 7777;
    private static final String SERVER_URL = "http://localhost/api";

    static final URI BASE_URI = UriBuilder.fromUri(SERVER_URL).port(SERVER_PORT).build();
    private static HttpServer httpServer;
    private static EntityManagerFactory emf;

    //This is how we hold on to the token after login, similar to that a client must store the token somewhere
    private static String securityToken;

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

        try {
em.getTransaction().begin();
            em.createQuery("DELETE FROM User").executeUpdate();
            em.createQuery("DELETE FROM Role").executeUpdate();

            user1 = new User("user1", "test1", "jakob", "staudal");
            user2 = new User("user2", "test2", "bokaj", "laduats");

            Role userRole = new Role("user");
            Role adminRole = new Role("admin");

            user1.addRole(userRole);
            user2.addRole(adminRole);

            em.persist(userRole);
            em.persist(adminRole);

            em.persist(user1);
            em.persist(user2);

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

    private void logOut() {
        securityToken = null;
    }

    @Test
    public void testCount() {
        login("user2", "test2");
        given()
                .contentType("application/json")
                .header("x-access-token", securityToken)
                .get("/users").then()
                .assertThat()
                .statusCode(200).body("size()", org.hamcrest.Matchers.is(2));
    }

    // Secure endpoint test
    @Test
    public void testGetAllUsers() {
        login("user2", "test2");
        given()
                .contentType("application/json")
                .header("x-access-token", securityToken)
                .get("/users").then()
                .assertThat()
                .statusCode(200)
                .body("size()", org.hamcrest.Matchers.is(2));
    }

    @Test
    public void testEditFirstName() {
        login("user2", "test2");
        given()
                .contentType("application/json")
                .header("x-access-token", securityToken)
                .body("{\"firstName\": \"test2\"}")
                .put("/users/edit/user1").then()
                .assertThat()
                .statusCode(200)
                .body("firstName", org.hamcrest.Matchers.is("test2"));
    }
}

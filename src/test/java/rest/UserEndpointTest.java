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

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

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

    @BeforeEach
    public void setUp() {
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM User").executeUpdate();
            em.createQuery("DELETE FROM Role").executeUpdate();

            user1 = new User("admin", "test123", "jakob", "staudal");
            user2 = new User("user", "test123", "bokaj", "laduats");

            Role userRole = new Role("admin");
            Role adminRole = new Role("user");

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

    // Test of getAllUsers endpoint
    @Test
    public void testGetAllUsers() {
        login("admin", "test123");
        given()
                .contentType("application/json")
                .header("x-access-token", securityToken)
                .when()
                .get("/users")
                .then()
                .statusCode(200)
                .body("size()", equalTo(2));
    }

    // Test of createUser endpoint
    @Test
    public void testCreateUser() {
        login("admin", "test123");
        given()
                .contentType("application/json")
                .header("x-access-token", securityToken)
                .body("{\"user_name\":\"test\", \"user_pass\":\"test123\", \"firstName\":\"test\", \"lastName\":\"test\"}")
                .when()
                .post("/users")
                .then()
                .statusCode(200)
                .body("user_name", equalTo("test"));
    }

    // Test of deleteUser endpoint
    @Test
    public void testDeleteUser() {
        login("admin", "test123");
        given()
                .contentType("application/json")
                .header("x-access-token", securityToken)
                .when()
                .delete("/users/delete/" + user2.getUserName())
                .then()
                .statusCode(200)
                .body("user_name", equalTo("user"));
    }

    // Test of editUser endpoint
    @Test
    public void testEditUser() {
        login("admin", "test123");
        given()
                .contentType("application/json")
                .header("x-access-token", securityToken)
                .body("{\"user_name\":\"\", \"user_pass\":\"\", \"firstName\":\"test\", \"lastName\":\"\"}")
                .when()
                .put("/users/edit/" + user2.getUserName())
                .then()
                .statusCode(200)
                .body("firstName", equalTo("test"));
    }
}

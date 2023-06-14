package rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dtos.UserDTO;
import errorhandling.API_Exception;
import facades.UserFacade;
import utils.EMF_Creator;
import utils.LocalDateTypeAdapter;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.time.LocalDate;
import java.util.List;

@Path("users")
public class UserResource {

    @Context
    private UriInfo context;

    @Context
    SecurityContext securityContext;

    private static final EntityManagerFactory EMF = EMF_Creator.createEntityManagerFactory();
    private final UserFacade USER_FACADE = UserFacade.getUserFacade(EMF);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter()).create();

    // Used to get all users on the AdminUsers page
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin")
    public List<UserDTO> getAllUsers() {
        return USER_FACADE.getAllUsers();
    }

    // Used to create a new user in the AddUserModal component and the CreateAccount page
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    public Response createUser(String jsonUser) throws API_Exception {
        UserDTO userDTO = GSON.fromJson(jsonUser, UserDTO.class);

        try {
            UserDTO newUser = USER_FACADE.createUser(userDTO);
            return Response.ok(newUser).build();
        } catch (Exception e) {
            throw new API_Exception("Username already used", 400);
        }
    }

    // Used to delete a user on the AdminUsers page
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin")
    @Path("/delete/{user_name}")
    public Response deleteUser(@PathParam("user_name") String user_name) throws API_Exception {
        if (user_name.equals("admin")) {
            throw new API_Exception("Can't delete yourself", 400);
        }

        if (user_name.equals("")) {
            throw new API_Exception("No user name provided", 400);
        }

        try {
            UserDTO userDTO = USER_FACADE.deleteUser(user_name);
            return Response.ok(userDTO).build();
        } catch (Exception e) {
            throw new API_Exception("Could not delete user", 400);
        }
    }

    // Used to edit a user in the EditUserModal component
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin")
    @Path("/edit/{user_name}")
    public Response editUser(@PathParam("user_name") String user_name, String jsonUser) throws API_Exception {
        UserDTO userDTO = GSON.fromJson(jsonUser, UserDTO.class);

        // Attach username to userDTO
        userDTO.setUser_name(user_name);

        try {
            UserDTO editedUser = USER_FACADE.editUser(userDTO);
            return Response.ok(editedUser).build();
        } catch (Exception e) {
            throw new API_Exception("Could not edit user", 400);
        }
    }
}
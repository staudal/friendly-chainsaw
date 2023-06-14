package rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dtos.UserDTO;
import errorhandling.API_Exception;
import facades.UserFacade;
import utils.EMF_Creator;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;

@Path("users")
public class UserResource {

    @Context
    private UriInfo context;

    @Context
    SecurityContext securityContext;

    private static final EntityManagerFactory EMF = EMF_Creator.createEntityManagerFactory();
    private final UserFacade USER_FACADE = UserFacade.getUserFacade(EMF);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin")
    public List<UserDTO> getAllUsers() {
        return USER_FACADE.getAllUsers();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser(String jsonUser) throws API_Exception {
        UserDTO userDTO = GSON.fromJson(jsonUser, UserDTO.class);

        try {
            UserDTO newUser = USER_FACADE.createUser(userDTO);
            return Response.ok(newUser).build();
        } catch (Exception e) {
            throw new API_Exception("Username already used", 400);
        }
    }

    @DELETE
    @Path("/delete/{user_name}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin")
    public Response deleteUser(@PathParam("user_name") String user_name) throws API_Exception {
        if (user_name == null || user_name.equals("admin")) {
            throw new API_Exception("Can't delete yourself", 400);
        }

        try {
            UserDTO userDTO = USER_FACADE.deleteUser(user_name);
            return Response.ok(userDTO).build();
        } catch (Exception e) {
            throw new API_Exception("Could not delete user", 400);
        }
    }

    // Edit first name and last name
    @PUT
    @Path("/edit/{user_name}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin")
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
package rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dtos.ShowDTO;
import dtos.UserDTO;
import errorhandling.API_Exception;
import facades.ShowFacade;
import utils.EMF_Creator;
import utils.LocalDateTypeAdapter;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.time.LocalDate;

@Path("shows")
public class ShowResource {
    @Context
    private UriInfo context;

    @Context
    SecurityContext securityContext;

    private static final EntityManagerFactory EMF = EMF_Creator.createEntityManagerFactory();
    private final ShowFacade SHOW_FACADE = ShowFacade.getShowFacade(EMF);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter()).create();

    // Used to get all shows on the AdminShows page
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin")
    public Response getAllShows() {
        return Response.ok(SHOW_FACADE.getAllShows()).build();
    }

    // Used to get all shows that's included in the festivals that the user is attending on the UserShows page
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Path("/festivals/{username}")
    public Response getPossibleShowsByUsername(@PathParam("username") String username) throws API_Exception {
        return Response.ok(SHOW_FACADE.getPossibleShowsByUsername(username)).build();
    }

    // Used to get all shows that the user is attending on the UserShows page
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Path("/{username}")
    public Response getShowsByUsername(@PathParam("username") String username) throws API_Exception {
        return Response.ok(SHOW_FACADE.getShowsByUsername(username)).build();
    }

    // Used to create a new show in the AddShowModal component
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin")
    public Response createNewShow(String show) throws API_Exception {
        ShowDTO showDTO = GSON.fromJson(show, ShowDTO.class);
        return Response.ok(SHOW_FACADE.createNewShow(showDTO)).build();
    }

    // Used to edit a show in the EditShowModal component
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin")
    @Path("/{id}")
    public Response editShow(@PathParam("id") Long id, String show) throws API_Exception {
        ShowDTO showToEdit = GSON.fromJson(show, ShowDTO.class);
        ShowDTO editedShow = SHOW_FACADE.editShow(id, showToEdit);
        System.out.println(editedShow);
        return Response.ok(editedShow).build();
    }

    // Used to delete a show on the AdminShows page
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin")
    @Path("/{id}")
    public Response deleteShow(@PathParam("id") Long id) throws API_Exception {
        return Response.ok(SHOW_FACADE.deleteShow(id)).build();
    }

    // Used to add a user to a show on the UserShows page
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Path("/add/{id}")
    public Response addUserToShow(@PathParam("id") Long id, String user) throws API_Exception {
        UserDTO userToAdd = GSON.fromJson(user, UserDTO.class);
        ShowDTO show = SHOW_FACADE.addUserToShow(id, userToAdd);
        return Response.ok(show).build();
    }

    // Used to remove a user from a show on the UserShows page
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Path("/remove/{id}")
    public Response removeUserFromShow(@PathParam("id") Long id, String user) throws API_Exception {
        UserDTO userToRemove = GSON.fromJson(user, UserDTO.class);
        ShowDTO show = SHOW_FACADE.removeUserFromShow(id, userToRemove);
        return Response.ok(show).build();
    }
}

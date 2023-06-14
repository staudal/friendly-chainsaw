package rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dtos.ShowDTO;
import dtos.UserDTO;
import errorhandling.API_Exception;
import facades.FestivalFacade;
import facades.ShowFacade;
import utils.EMF_Creator;
import utils.LocalDateTypeAdapter;

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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllShows() {
        return Response.ok(SHOW_FACADE.getAllShows()).build();
    }

    @GET
    @Path("/festivals/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPossibleShowsByUsername(@PathParam("username") String username) throws API_Exception {
        return Response.ok(SHOW_FACADE.getPossibleShowsByUsername(username)).build();
    }

    @GET
    @Path("/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getShowsByUsername(@PathParam("username") String username) throws API_Exception {
        return Response.ok(SHOW_FACADE.getShowsByUsername(username)).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addShow(String show) {
        ShowDTO showDTO = GSON.fromJson(show, ShowDTO.class);
        System.out.println(showDTO);
        return Response.ok(SHOW_FACADE.addShow(showDTO)).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public Response editShow(@PathParam("id") Long id, String show) throws API_Exception {
        ShowDTO showToEdit = GSON.fromJson(show, ShowDTO.class);
        ShowDTO editedShow = SHOW_FACADE.editShow(id, showToEdit);
        System.out.println(editedShow);
        return Response.ok(editedShow).build();
    }

    // remove user from show
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/remove/{id}")
    public Response removeShowFromUser(@PathParam("id") Long id, String user) throws API_Exception {
        UserDTO userToRemove = GSON.fromJson(user, UserDTO.class);
        ShowDTO show = SHOW_FACADE.removeShowFromUser(id, userToRemove);
        return Response.ok(show).build();
    }

    // add user to show
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/add/{id}")
    public Response addShowToUser(@PathParam("id") Long id, String user) throws API_Exception {
        UserDTO userToAdd = GSON.fromJson(user, UserDTO.class);
        ShowDTO show = SHOW_FACADE.addShowToUser(id, userToAdd);
        return Response.ok(show).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteShow(@PathParam("id") Long id) {
        return Response.ok(SHOW_FACADE.deleteShow(id)).build();
    }
}

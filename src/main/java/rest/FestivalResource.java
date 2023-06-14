package rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dtos.FestivalDTO;
import dtos.UserDTO;
import errorhandling.API_Exception;
import facades.FestivalFacade;
import utils.EMF_Creator;
import utils.LocalDateTypeAdapter;

import javax.persistence.EntityManagerFactory;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.time.LocalDate;

@Path("festivals")
public class FestivalResource {
    @Context
    private UriInfo context;

    @Context
    SecurityContext securityContext;

    private static final EntityManagerFactory EMF = EMF_Creator.createEntityManagerFactory();
    private final FestivalFacade FESTIVAL_FACADE = FestivalFacade.getFestivalFacade(EMF);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter()).create();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllFestivals() {
        return Response.ok(FESTIVAL_FACADE.getAllFestivals()).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFestivalById(@PathParam("id") Long id) throws API_Exception {
        return Response.ok(FESTIVAL_FACADE.getFestivalById(id)).build();
    }

    @GET
    @Path("/user/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFestivalsByUser(@PathParam("username") String username) throws API_Exception {
        return Response.ok(FESTIVAL_FACADE.getFestivalsByUser(username)).build();
    }

    @GET
    @Path("/guests/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFestivalGuests(@PathParam("id") Long id) throws API_Exception {
        return Response.ok(FESTIVAL_FACADE.getFestivalGuests(id)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addFestival(String festival) throws API_Exception {
        FestivalDTO festivalToAdd = GSON.fromJson(festival, FestivalDTO.class);
        FestivalDTO addedFestival = FESTIVAL_FACADE.addFestival(festivalToAdd);
        return Response.ok(addedFestival).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response editFestival(@PathParam("id") Long id, String festival) throws API_Exception {
        FestivalDTO festivalToEdit = GSON.fromJson(festival, FestivalDTO.class);
        FestivalDTO editedFestival = FESTIVAL_FACADE.editFestival(id, festivalToEdit);
        return Response.ok(editedFestival).build();
    }

    // remove user from festival
    @PUT
    @Path("/user/remove/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeUserFromFestival(@PathParam("id") Long id, String username) throws API_Exception {
        UserDTO userToRemove = GSON.fromJson(username, UserDTO.class);
        FestivalDTO festival = FESTIVAL_FACADE.removeUserFromFestival(id, userToRemove);
        return Response.ok(festival).build();
    }

    // add user to festival
    @PUT
    @Path("/user/add/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addUserToFestival(@PathParam("id") Long id, String username) throws API_Exception {
        UserDTO userToAdd = GSON.fromJson(username, UserDTO.class);
        FestivalDTO festival = FESTIVAL_FACADE.addUserToFestival(id, userToAdd);
        return Response.ok(festival).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteFestival(@PathParam("id") Long id) throws API_Exception {
        FestivalDTO deletedFestival = FESTIVAL_FACADE.deleteFestival(id);
        return Response.ok(deletedFestival).build();
    }
}

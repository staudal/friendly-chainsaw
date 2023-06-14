package rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dtos.FestivalDTO;
import dtos.UserDTO;
import errorhandling.API_Exception;
import facades.FestivalFacade;
import utils.EMF_Creator;
import utils.LocalDateTypeAdapter;

import javax.annotation.security.RolesAllowed;
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

    // Used to get all festivals on the AdminFestivals page and the UserFestivals page
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    public Response getAllFestivals() {
        return Response.ok(FESTIVAL_FACADE.getAllFestivals()).build();
    }

    // Used to get all festivals that the user is attending on the UserFestivals page
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    @Path("/user/{username}")
    public Response getFestivalsByUser(@PathParam("username") String username) throws API_Exception {
        return Response.ok(FESTIVAL_FACADE.getFestivalsByUser(username)).build();
    }

    // Used to create a new festival in the AddFestivalModal component
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin")
    public Response createNewFestival(String festival) throws API_Exception {
        FestivalDTO festivalToAdd = GSON.fromJson(festival, FestivalDTO.class);
        FestivalDTO addedFestival = FESTIVAL_FACADE.createNewFestival(festivalToAdd);
        return Response.ok(addedFestival).build();
    }

    // Used to edit a festival in the EditFestivalModal component
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin")
    @Path("/{id}")
    public Response editFestival(@PathParam("id") Long id, String festival) throws API_Exception {
        FestivalDTO festivalToEdit = GSON.fromJson(festival, FestivalDTO.class);
        FestivalDTO editedFestival = FESTIVAL_FACADE.editFestival(id, festivalToEdit);
        return Response.ok(editedFestival).build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin")
    @Path("/{id}")
    public Response deleteFestival(@PathParam("id") Long id) throws API_Exception {
        FestivalDTO deletedFestival = FESTIVAL_FACADE.deleteFestival(id);
        return Response.ok(deletedFestival).build();
    }

    // Used to add a user to a festival on the UserFestivals page
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    @Path("/user/add/{id}")
    public Response addUserToFestival(@PathParam("id") Long id, String username) throws API_Exception {
        UserDTO userToAdd = GSON.fromJson(username, UserDTO.class);
        FestivalDTO festival = FESTIVAL_FACADE.addUserToFestival(id, userToAdd);
        return Response.ok(festival).build();
    }

    // Used to remove a user from a festival on the UserFestivals page
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    @Path("/user/remove/{id}")
    public Response removeUserFromFestival(@PathParam("id") Long id, String username) throws API_Exception {
        UserDTO userToRemove = GSON.fromJson(username, UserDTO.class);
        FestivalDTO festival = FESTIVAL_FACADE.removeUserFromFestival(id, userToRemove);
        return Response.ok(festival).build();
    }
}

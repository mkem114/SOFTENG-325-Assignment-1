package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.common.dto.ConcertDTO;
import nz.ac.auckland.concert.common.dto.PerformerDTO;
import nz.ac.auckland.concert.common.dto.UserDTO;
import nz.ac.auckland.concert.service.domain.Concert;
import nz.ac.auckland.concert.service.domain.Performer;
import nz.ac.auckland.concert.service.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Class to implement a simple REST Web service for managing Concerts.
 *
 */
@Path("/resource")
@Produces({MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_XML})
public class ConcertResource {

	private static Logger _logger = LoggerFactory.getLogger(ConcertResource.class);

	@GET
	@Path("/concerts")
	public Response getAllConcerts() {
		EntityManager em = PersistenceManager.instance().createEntityManager();
		em.getTransaction().begin();

		TypedQuery<Concert> concertQuery = em.createQuery("select c from Concert c", Concert.class);
		List<Concert> concerts = concertQuery.getResultList();

		if (concerts.isEmpty()) {
			return Response.noContent().build();
		}

		Set<ConcertDTO> concertDTOs = new HashSet<>();
		for (Concert c : concerts) {
			concertDTOs.add(c.convertToDTO());
		}

		em.close();

		GenericEntity<Set<ConcertDTO>> wrapped = new GenericEntity<Set<ConcertDTO>>(concertDTOs) {
		};
		return Response.ok(wrapped).build();
	}

	@GET
	@Path("/performers")
	public Response getAllPerformers() {
		EntityManager em = PersistenceManager.instance().createEntityManager();
		em.getTransaction().begin();

		TypedQuery<Performer> performerQuery = em.createQuery("select p from Performer p", Performer.class);
		List<Performer> performers = performerQuery.getResultList();

		if (performers.isEmpty()) {
			return Response.noContent().build();
		}

		Set<PerformerDTO> performerDTOs = new HashSet<>();
		for (Performer p : performers) {
			performerDTOs.add(p.convertToDTO());
		}

		em.close();

		GenericEntity<Set<PerformerDTO>> wrapped = new GenericEntity<Set<PerformerDTO>>(performerDTOs) {
		};
		return Response.ok(wrapped).build();
	}

	@POST
	@Path("/user")
	public Response createNewUser(UserDTO newUser) {
		List<String> requiredDetails = new ArrayList<>();
		requiredDetails.add(newUser.getFirstname());
		requiredDetails.add(newUser.getLastname());
		requiredDetails.add(newUser.getPassword());
		requiredDetails.add(newUser.getUsername());
		for (String s : requiredDetails) {
			if ((s == null) || s.equals("")) {
				return Response.status(Response.Status.LENGTH_REQUIRED).build();
			}
		}

		EntityManager em = PersistenceManager.instance().createEntityManager();
		em.getTransaction().begin();

		try {
			8 if (em.find(User.class, newUser.getUsername()) != null) {
				return Response.status(Response.Status.CONFLICT).build();
			}

			User u = new User(newUser);
			UUID token = UUID.randomUUID();
			u.set_token(token.toString());
			u.set_tokenTimeStamp(LocalDateTime.now());
			em.persist(new User(newUser));
			em.getTransaction().commit();


			return Response
					.created(URI.create("/user/" + newUser.getUsername()))
					.entity(new GenericEntity<UserDTO>(newUser) {
					})
					.cookie(NewCookie.valueOf(token.toString()))
					.build();
		} catch (Exception e) {
			return Response.status(Response.Status.CONFLICT).build();
		} finally {
			em.close();
		}
	}

	@POST
	@Path("/login")
	public Response authenticateUser(UserDTO user) {
		List<String> requiredDetails = new ArrayList<>();
		requiredDetails.add(user.getPassword());
		requiredDetails.add(user.getUsername());
		for (String s : requiredDetails) {
			if ((s == null) || s.equals("")) {
				return Response.status(Response.Status.LENGTH_REQUIRED).build();
			}
		}

		EntityManager em = PersistenceManager.instance().createEntityManager();
		em.getTransaction().begin();

		try {
			User u = em.find(User.class, user.getUsername());
			if (u == null) {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
			if (!u.get_passwordHash().equals(user.getPassword())) {
				return Response.status(Response.Status.UNAUTHORIZED).build();
			}

			UUID token = UUID.randomUUID();
			u.set_token(token.toString());
			u.set_tokenTimeStamp(LocalDateTime.now());
			em.merge(u);
			em.getTransaction().commit();

			return Response
					.accepted()
					.entity(new GenericEntity<UserDTO>(user) {
					})
					.cookie(NewCookie.valueOf(token.toString()))
					.build();
		} finally {
			em.close();
		}
	}2
}
//2
//    /**2
//     * Retrieves a Concert based on its unique id. The HTTP response message
//     * has a status code of either 200 or 404, depending on whether the
//     * specified Concert is found.
//     *
//     * When clientId is null, the HTTP request message doesn't contain a cookie
//     * named clientId (Config.CLIENT_COOKIE), this method generates a new
//     * cookie, whose value is a randomly generated UUID. This method returns
//     * the new cookie as part of the HTTP response message.
//     *
//     * This method maps to the URI pattern <base-uri>/concerts/{id}.
//     *
//     * @param id the unique ID of the Concert.
//     *
//     * @return a Response object containing the required Concert.
//     */
//    @GET
//    @Path("{id}")
//    public Response retrieveConcert(@PathParam("id") long id) {
//        EntityManager em = PersistenceManager.instance().createEntityManager();
//        em.getTransaction().begin();
//
//        Concert c = em.find(Concert.class, id);
//        em.getTransaction().commit();
//
//        if (c == null) {
//            throw new WebApplicationException(Response.Status.NOT_FOUND);
//        }
//
//        return Response.ok(c).build();
//    }
//
//    /**
//     * Creates a new Concert. This method assigns an ID to the new Concert and
//     * stores it in memory. The HTTP Response message returns a Location header
//     * with the URI of the new Concert and a status code of 201.
//     *
//     * When clientId is null, the HTTP request message doesn't contain a cookie
//     * named clientId (Config.CLIENT_COOKIE), this method generates a new
//     * cookie, whose value is a randomly generated UUID. This method returns
//     * the new cookie as part of the HTTP response message.
//     *
//     * This method maps to the URI pattern <base-uri>/concerts.
//     *
//     * @param concert the new Concert to create.
//     *
//     * @return a Response object containing the status code 201 and a Location
//     * header.
//     */
//    @POST
//    public Response createConcert(Concert concert) {
//        EntityManager em = PersistenceManager.instance().createEntityManager();
//        em.getTransaction().begin();
//
//        em.persist(concert);
//        em.getTransaction().commit();
//
//        return Response.created(URI.create("/concerts/" + concert.get_cID())).build();
//    }
//
//
//    @PUT
//    public Response updateConcert(Concert concert) {
//        EntityManager em = PersistenceManager.instance().createEntityManager();
//        em.getTransaction().begin();
//        Concert c = em.find(Concert.class, concert.get_cID());
//        em.getTransaction().commit();
//
//        if (c != null) {
//            em.getTransaction().begin();
//            em.merge(concert);
//            em.getTransaction().commit();
//
//            return Response.status(204).build();
//        }
//
//        return null;
//    }
//
//    /**
//     * Deletes all Concerts, returning a status code of 204.
//     *
//     * When clientId is null, the HTTP request message doesn't contain a cookie
//     * named clientId (Config.CLIENT_COOKIE), this method generates a new
//     * cookie, whose value is a randomly generated UUID. This method returns
//     * the new cookie as part of the HTTP response message.
//     *
//     * This method maps to the URI pattern <base-uri>/concerts.
//     *
//     * @return a Response object containing the status code 204.
//     */
//    @DELETE
//    @Path("{id}")
//    public Response deleteConcert(@PathParam("id") long id) {
//        EntityManager em = PersistenceManager.instance().createEntityManager();
//        em.getTransaction().begin();
//        Concert c = em.find(Concert.class, id);
//        em.getTransaction().commit();
//
//        if (c != null) {
//            em.getTransaction().begin();
//            em.remove(c);
//            em.getTransaction().commit();
//
//            return Response.status(204).build();
//        }
//
//        throw new WebApplicationException(Response.Status.NOT_FOUND);
//    }
//
//    /**
//     * Deletes all Concerts, returning a status code of 204.
//     *
//     * When clientId is null, the HTTP request message doesn't contain a cookie
//     * named clientId (Config.CLIENT_COOKIE), this method generates a new
//     * cookie, whose value is a randomly generated UUID. This method returns
//     * the new cookie as part of the HTTP response message.
//     *
//     * This method maps to the URI pattern <base-uri>/concerts.
//     *
//     * @return a Response object containing the status code 204.
//     */
//    @DELETE
//    public Response deleteAllConcerts() {
//        EntityManager em = PersistenceManager.instance().createEntityManager();
//        em.getTransaction().begin();
//        Query cq = em.createQuery("delete from Concert c");
//        cq.executeUpdate();
//        em.getTransaction().commit();
//        return Response.noContent())
package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.common.dto.ConcertDTO;
import nz.ac.auckland.concert.common.dto.CreditCardDTO;
import nz.ac.auckland.concert.common.dto.PerformerDTO;
import nz.ac.auckland.concert.common.dto.UserDTO;
import nz.ac.auckland.concert.service.domain.Concert;
import nz.ac.auckland.concert.service.domain.CreditCard;
import nz.ac.auckland.concert.service.domain.Performer;
import nz.ac.auckland.concert.service.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Cookie;
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
			if (em.find(User.class, newUser.getUsername()) != null) {
				return Response.status(Response.Status.CONFLICT).build();
			}

			User u = new User(newUser);
			UUID token = UUID.randomUUID();
			u.set_token(token.toString());
			u.set_tokenTimeStamp(LocalDateTime.now());
			em.persist(u);
			em.getTransaction().commit();

			return Response
					.created(URI.create("/user/" + newUser.getUsername()))
					.entity(new GenericEntity<UserDTO>(newUser) {
					})
					.cookie(new NewCookie("authenticationToken", token.toString()))
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
			if (u.get_token() == null) {
				UUID token = UUID.randomUUID();
				u.set_token(token.toString());
				u.set_tokenTimeStamp(LocalDateTime.now());
				em.merge(u);
				em.getTransaction().commit();
			}

			return Response
					.accepted()
					.entity(new GenericEntity<UserDTO>(u.convertToDTO()) {
					})
					.cookie(new NewCookie("authenticationToken", u.get_token()))
					.build();
		} finally {
			em.close();
		}
	}

	@POST
	@Path("/add_credit_card")
	public Response addCreditCard(CreditCardDTO creditcard, @CookieParam("authenticationToken") Cookie token) {
		if (token == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}

		EntityManager em = PersistenceManager.instance().createEntityManager();
		em.getTransaction().begin();
		try {
			User u = (User) em.createQuery("SELECT U FROM User U WHERE U._token = :token")
					.setParameter("token", token.getValue()).getSingleResult();
			if (u == null) {//TODO check if timestamp is beyond limit for authorisation
				return Response.status(Response.Status.UNAUTHORIZED).build();
			}

			u.set_creditCard(new CreditCard(creditcard));
			em.persist(u.get_creditCard());
			em.merge(u);
			em.getTransaction().commit();

			return Response.accepted().build();
		} finally {
			em.close();
		}
	}
}
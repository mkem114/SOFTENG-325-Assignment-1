package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.common.dto.BookingDTO;
import nz.ac.auckland.concert.common.dto.ConcertDTO;
import nz.ac.auckland.concert.common.dto.CreditCardDTO;
import nz.ac.auckland.concert.common.dto.PerformerDTO;
import nz.ac.auckland.concert.common.dto.ReservationDTO;
import nz.ac.auckland.concert.common.dto.ReservationRequestDTO;
import nz.ac.auckland.concert.common.dto.SeatDTO;
import nz.ac.auckland.concert.common.dto.UserDTO;
import nz.ac.auckland.concert.service.domain.Concert;
import nz.ac.auckland.concert.service.domain.CreditCard;
import nz.ac.auckland.concert.service.domain.Performer;
import nz.ac.auckland.concert.service.domain.Reservation;
import nz.ac.auckland.concert.service.domain.Seat;
import nz.ac.auckland.concert.service.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.OptimisticLockException;
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

	@GET
	@Path("/bookings")
	public Response getAllBookings() {
		EntityManager em = PersistenceManager.instance().createEntityManager();
		em.getTransaction().begin();

		TypedQuery<Reservation> reservationQuery = em
				.createQuery("select r from Reservation r", Reservation.class);
		List<Reservation> reservations = reservationQuery.getResultList();

		if (reservations.isEmpty()) {
			return Response.noContent().build();
		}

		Set<BookingDTO> bookingDTOs = new HashSet<>();
		for (Reservation r : reservations) {
			if (r.get_confirmed()) {
				bookingDTOs.add(r.convertToDTO());
			}
		}

		em.close();

		GenericEntity<Set<BookingDTO>> wrapped = new GenericEntity<Set<BookingDTO>>(bookingDTOs) {
		};
		return Response.ok(wrapped).build();
	}

	@POST
	@Path("/reservations")
	public Response reservationRequest(ReservationRequestDTO dto, @CookieParam("authenticationToken") Cookie token) {
		if (token == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}

		EntityManager em = PersistenceManager.instance().createEntityManager();
		em.getTransaction().begin();
		TypedQuery<Concert> reservationQuery = em
				.createQuery("select c from Concert c where c._cID=:cid", Concert.class)
				.setParameter("cid", dto.getConcertId());
		Concert c = reservationQuery.getSingleResult();
		if (!c.get_dates().contains(dto.getDate())) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

		try {
			User u = (User) em.createQuery("SELECT U FROM User U WHERE U._token = :token")
					.setParameter("token", token.getValue()).getSingleResult();
			if (u == null) {//TODO check if timestamp is beyond limit for authorisation
				return Response.status(Response.Status.UNAUTHORIZED).build();
			}
			em.getTransaction().commit();

			boolean flag = true;
			while (flag) {
				try {
					em.getTransaction().begin();
					TypedQuery<Seat> seatQuery = em
							.createQuery("select s from Seat s where s._concert._cID=:cid AND " +
									"s._datetime=:date", Seat.class)
							.setParameter("cid", c.get_cID())
							.setParameter("date", dto.getDate())
							.setLockMode(LockModeType.OPTIMISTIC);
					List<Seat> seats = seatQuery.getResultList();
					if (seats.isEmpty()) {
						for (int i = 1; i <= 374; i++) {
							Seat s = new Seat();
							s.set_status(Seat.SeatStatus.FREE);
							s.set_concert(c);
							s.set_datetime(dto.getDate());
							s.set_timestamp(LocalDateTime.now());
							s.set_number(i);
							em.persist(s);
						}
					} else {
						for (Seat s : seats) {
							if (s.get_status().equals(Seat.SeatStatus.PENDING) && s.get_timestamp().isBefore(LocalDateTime.now().minusMinutes(5))) {
								s.set_status(Seat.SeatStatus.FREE);
								em.merge(s);
							}
						}
					}
					em.getTransaction().commit();
					flag = false;
				} catch (OptimisticLockException e) {
					flag = true;
				}
			}

			//em.getTransaction().begin();
			int min = 1;
			int max = 374;
			switch (dto.getSeatType()) {
				case PriceBandA:
					min = 1;
					max = Seat.BANDA_MAX;
					break;
				case PriceBandB:
					min = Seat.BANDA_MAX + 1;
					max = Seat.BANDA_MAX + Seat.BANDB_MAX;
					break;
				case PriceBandC:
					min = Seat.BANDA_MAX + Seat.BANDB_MAX + 1;
					max = Seat.BANDA_MAX + Seat.BANDB_MAX + Seat.BANDC_MAX;
					break;
			}
			flag = true;
			ReservationDTO reservationDTO = new ReservationDTO();
			while (flag) {
				try {
					em.getTransaction().begin();
					TypedQuery<Seat> seatQuery = em
							.createQuery("select s from Seat s where s._concert._cID=:cid AND " +
									"s._datetime=:date", Seat.class)
							.setParameter("cid", c.get_cID())
							.setParameter("date", dto.getDate())
							.setLockMode(LockModeType.OPTIMISTIC);
					List<Seat> seats = seatQuery.getResultList();
					List<Seat> pending = new ArrayList<>();
					for (Seat s : seats) {
						if ((s.get_status().equals(Seat.SeatStatus.FREE)) ||
								(Seat.SeatStatus.PENDING.equals(s.get_status()) && s.get_timestamp()
										.isBefore(LocalDateTime.now().minusMinutes(5)))) {
							if (s.get_number() >= min && s.get_number() <= max) {
								pending.add(s);
								s.set_status(Seat.SeatStatus.PENDING);
							}
						}
					}
					if (pending.size() >= dto.getNumberOfSeats()) {
						for (int i = pending.size() - 1; i >= dto.getNumberOfSeats(); i--) {
							pending.remove(i);
						}
					} else {
						return Response.status(Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE).build();
					}

					Reservation r = new Reservation();
					r.set_concert(c);
					r.set_confirmed(false);
					r.set_dateTime(dto.getDate());
					r.set_priceBand(dto.getSeatType());
					r.set_user(u);
					r.set_rID(UUID.randomUUID().getMostSignificantBits());
					em.persist(r);
					em.getTransaction().commit();

					Set<SeatDTO> sdtos = new HashSet<>();
					for (Seat s : pending) {
						sdtos.add(s.convertToDTO());
						em.persist(s);
					}
					reservationDTO = new ReservationDTO(r.get_rID(), dto, sdtos);

					flag = false;
				} catch (OptimisticLockException e) {
					flag = true;
				}
			}
			return Response.ok(new GenericEntity<ReservationDTO>(reservationDTO) {
			}).build();
		} finally {
			em.close();
		}
	}
}
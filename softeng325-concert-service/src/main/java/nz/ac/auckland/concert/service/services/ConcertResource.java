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
import javax.ws.rs.core.CacheControl;
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

import static nz.ac.auckland.concert.service.services.ConcertApplication.RESERVATION_EXPIRY_TIME_IN_SECONDS;

/**
 * Class to implement a simple REST Web service for managing Concerts.
 */
@Path("/resource")
@Produces({MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_XML})
public class ConcertResource {

	@GET
	@Path("/concerts")
	public Response getAllConcerts() {
		try {
			EntityManager em = PersistenceManager.instance().createEntityManager();
			em.getTransaction().begin();

			CacheControl cc = new CacheControl();
			cc.setMaxAge(5);//Should be fine tuned or changed on the fly. Want to be able to see concert releases but don't
			//want to have it so low it does nothing.
			cc.setPrivate(true);

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
			return Response.ok(wrapped).cacheControl(cc).build();
		} catch (Exception e) {
			return Response.serverError().build();
		}
	}

	@GET
	@Path("/performers")
	public Response getAllPerformers() {
		try {
			EntityManager em = PersistenceManager.instance().createEntityManager();
			em.getTransaction().begin();

			CacheControl cc = new CacheControl();
			cc.setMaxAge(5);//Should be fine tuned or changed on the fly. Want to be able to see concert releases but don't
			//want to have it so low it does nothing.
			cc.setPrivate(true);

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
			return Response.ok(wrapped).cacheControl(cc).build();
		} catch (Exception e) {
			return Response.serverError().build();
		}
	}

	@POST
	@Path("/user")
	public Response createNewUser(UserDTO newUser) {
		try {
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
		} catch (Exception e) {
			return Response.serverError().build();
		}
	}

	@POST
	@Path("/login")
	public Response authenticateUser(UserDTO user) {
		try {
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
		} catch (Exception e) {
			return Response.serverError().build();
		}
	}

	@POST
	@Path("/add_credit_card")
	public Response addCreditCard(CreditCardDTO creditcard, @CookieParam("authenticationToken") Cookie token) {
		try {
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
		} catch (Exception e) {
			return Response.serverError().build();
		}
	}

	@GET
	@Path("/bookings")
	public Response getAllBookings(@CookieParam("authenticationToken") Cookie token) {
		try {
			if (token == null) {
				return Response.status(Response.Status.NOT_FOUND).build();
			}

			EntityManager em = PersistenceManager.instance().createEntityManager();
			em.getTransaction().begin();

			User u = (User) em.createQuery("SELECT U FROM User U WHERE U._token = :token")
					.setParameter("token", token.getValue()).getSingleResult();
			if (u == null) {//TODO check if timestamp is beyond limit for authorisation
				return Response.status(Response.Status.UNAUTHORIZED).build();
			}

			TypedQuery<Reservation> reservationQuery = em
					.createQuery("select r from Reservation r where r._user._token=:token", Reservation.class)
					.setParameter("token", token.getValue());
			List<Reservation> reservations = reservationQuery.getResultList();

			Set<BookingDTO> bookingDTOs = new HashSet<>();
			for (Reservation r : reservations) {
				if (r.get_confirmed()) {
					BookingDTO b = r.convertToDTO();
					bookingDTOs.add(b);
				}
			}
			em.close();

			GenericEntity<Set<BookingDTO>> wrapped = new GenericEntity<Set<BookingDTO>>(bookingDTOs) {
			};
			return Response.ok(wrapped).build();
		} catch (Exception e) {
			return Response.serverError().build();
		}
	}

	@POST
	@Path("/reservations")
	public Response reservationRequest(ReservationRequestDTO dto, @CookieParam("authenticationToken") Cookie token) {
		try {
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
								if (s.get_status().equals(Seat.SeatStatus.PENDING) && s.get_timestamp().isBefore(LocalDateTime.now().minusSeconds(RESERVATION_EXPIRY_TIME_IN_SECONDS))) {
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
											.isBefore(LocalDateTime.now()
													.minusSeconds(RESERVATION_EXPIRY_TIME_IN_SECONDS)))) {
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

						Set<SeatDTO> sdtos = new HashSet<>();
						for (Seat s : pending) {
							s.set_reservation(r);
							sdtos.add(s.convertToDTO());
							em.persist(s);
						}
						reservationDTO = new ReservationDTO(r.get_rID(), dto, sdtos);

						em.persist(r);
						em.getTransaction().commit();

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
		} catch (Exception e) {
			return Response.serverError().build();
		}
	}

	@POST
	@Path("/book")
	public Response confirmReservation(ReservationDTO reservationDTO, @CookieParam("authenticationToken") Cookie token) {
		try {
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
				if (u.get_creditCard() == null) {
					return Response.status(Response.Status.PAYMENT_REQUIRED).build();
				}

				TypedQuery<Reservation> reservationQuery = em
						.createQuery("select r from Reservation r where r._rID=:rid", Reservation.class)
						.setParameter("rid", reservationDTO.getId());
				Reservation r = reservationQuery.getSingleResult();


				Set<Integer> seatnums = new HashSet<>();
				for (SeatDTO d : reservationDTO.getSeats()) {
					seatnums.add(new Seat(d).get_number());
				}
				TypedQuery<Seat> seatQuery = em
						.createQuery("select s from Seat s where s._concert._cID=:cid " +
								"AND s._datetime=:dt AND s._number IN (:seats)", Seat.class)
						.setParameter("cid", reservationDTO.getReservationRequest().getConcertId())
						.setParameter("dt", reservationDTO.getReservationRequest().getDate())
						.setParameter("seats", seatnums)
						.setLockMode(LockModeType.OPTIMISTIC);
				List<Seat> seats = seatQuery.getResultList();

				boolean flag = true;
				boolean seatsObtained = true;
				while (flag) {
					try {
						for (Seat s : seats) {
							if (s.get_timestamp().isAfter(LocalDateTime.now()
									.minusSeconds(RESERVATION_EXPIRY_TIME_IN_SECONDS)) &&
									s.get_reservation().get_rID().equals(r.get_rID())) {
								s.set_status(Seat.SeatStatus.BOOKED);
							} else {
								seatsObtained = false;
							}
						}
						em.getTransaction().commit();
						flag = false;
					} catch (OptimisticLockException e) {
						flag = true;
					}
				}

				em.getTransaction().begin();
				reservationQuery = em
						.createQuery("select r from Reservation r where r._rID=:rid", Reservation.class)
						.setParameter("rid", reservationDTO.getId());
				r = reservationQuery.getSingleResult();
				seatQuery = em
						.createQuery("select s from Seat s where s._concert._cID=:cid AND s._datetime=:dt AND " +
								"s._number IN (:seats)", Seat.class)
						.setParameter("cid", reservationDTO.getReservationRequest().getConcertId())
						.setParameter("dt", reservationDTO.getReservationRequest().getDate())
						.setParameter("seats", seatnums)
						.setLockMode(LockModeType.OPTIMISTIC);
				seats = seatQuery.getResultList();
				if (seatsObtained) {
					for (Seat s : seats) {
						r.get_seats().add(s.get_number());
					}
					r.set_confirmed(true);
					em.getTransaction().commit();
					return Response.ok().build();
				} else {
					for (Seat s : seats) {
						try {
							if (s.get_reservation().get_rID().equals(r.get_rID())) {
								s.set_status(Seat.SeatStatus.FREE);
							}
						} catch (Exception e) {
						}
					}
					em.getTransaction().commit();
					return Response.status(Response.Status.REQUEST_TIMEOUT).build();
				}
			} finally {
				em.close();
			}
		} catch (Exception e) {
			return Response.serverError().build();
		}
	}
}
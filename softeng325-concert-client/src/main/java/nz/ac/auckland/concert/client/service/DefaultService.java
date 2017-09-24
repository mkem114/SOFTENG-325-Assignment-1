package nz.ac.auckland.concert.client.service;

import nz.ac.auckland.concert.common.dto.BookingDTO;
import nz.ac.auckland.concert.common.dto.ConcertDTO;
import nz.ac.auckland.concert.common.dto.CreditCardDTO;
import nz.ac.auckland.concert.common.dto.PerformerDTO;
import nz.ac.auckland.concert.common.dto.ReservationDTO;
import nz.ac.auckland.concert.common.dto.ReservationRequestDTO;
import nz.ac.auckland.concert.common.dto.UserDTO;
import nz.ac.auckland.concert.common.message.Messages;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class DefaultService implements ConcertService {

	private static String WEB_SERVICE_URI = "http://localhost:10000/services/resource";

	@Override
	public Set<ConcertDTO> getConcerts() throws ServiceException {
		Client client = ClientBuilder.newClient();
		try {

			Invocation.Builder builder = client.target(WEB_SERVICE_URI + "/concerts").request().accept(MediaType.APPLICATION_XML);
			Response response = builder.get();
			Set<ConcertDTO> concerts;
			if (response.getStatus() == Response.Status.OK.getStatusCode()) {
				concerts = response.readEntity(new GenericType<Set<ConcertDTO>>() {
				});
			} else {
				concerts = new HashSet<>();
			}
			client.close();
			return concerts;
		} catch (Exception e) {
			throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
		} finally {
			client.close();
		}
	}

	@Override
	public Set<PerformerDTO> getPerformers() throws ServiceException {
		Client client = ClientBuilder.newClient();
		try {
			Invocation.Builder builder = client.target(WEB_SERVICE_URI + "/performers").request().accept(MediaType.APPLICATION_XML);
			Response response = builder.get();
			Set<PerformerDTO> performers;
			if (response.getStatus() == Response.Status.OK.getStatusCode()) {
				performers = response.readEntity(new GenericType<Set<PerformerDTO>>() {
				});
			} else {
				performers = new HashSet<>();
			}
			client.close();
			return performers;
		} catch (Exception e) {
			throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
		} finally {
			client.close();
		}
	}

	@Override
	public UserDTO createUser(UserDTO newUser) throws ServiceException {
		Client client = ClientBuilder.newClient();
		try {
			Invocation.Builder builder = client.target(WEB_SERVICE_URI + "/user").request(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML);
			Response response = builder.post(Entity.entity(newUser, MediaType.APPLICATION_XML));

			int responseCode = response.getStatus();
			if (responseCode == Response.Status.CREATED.getStatusCode()) {
				return response.readEntity(new GenericType<UserDTO>() {
				});
			} else if (responseCode == Response.Status.CONFLICT.getStatusCode()) {
				throw new ServiceException(Messages.CREATE_USER_WITH_NON_UNIQUE_NAME);
			} else if (responseCode == Response.Status.LENGTH_REQUIRED.getStatusCode()) {
				throw new ServiceException(Messages.CREATE_USER_WITH_MISSING_FIELDS);
			} else {
				throw new ServiceException("UNEXPECTED HTTP STATUS CODE");
			}
		} catch (Exception e) {
			if (ServiceException.class.isInstance(e)) {
				throw e;
			} else {
				throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
			}
		} finally {
			client.close();
		}
	}

	@Override
	public UserDTO authenticateUser(UserDTO user) throws ServiceException {
		Client client = ClientBuilder.newClient();
		try {
			Invocation.Builder builder = client.target(WEB_SERVICE_URI + "/login").request(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML);
			Response response = builder.post(Entity.entity(user, MediaType.APPLICATION_XML));

			int responseCode = response.getStatus();
			if (responseCode == Response.Status.ACCEPTED.getStatusCode()) {
				return response.readEntity(new GenericType<UserDTO>(){});
			} else if (responseCode == Response.Status.LENGTH_REQUIRED.getStatusCode()) {
				throw new ServiceException(Messages.AUTHENTICATE_USER_WITH_MISSING_FIELDS);
			} else if (responseCode == Response.Status.NOT_FOUND.getStatusCode()) {
				throw new ServiceException(Messages.AUTHENTICATE_NON_EXISTENT_USER);
			} else if (responseCode == Response.Status.UNAUTHORIZED.getStatusCode()) {
				throw new ServiceException(Messages.AUTHENTICATE_USER_WITH_ILLEGAL_PASSWORD);
			} else {
				throw new ServiceException("UNEXPECTED HTTP STATUS CODE");
			}
		} catch (Exception e) {
			if (ServiceException.class.isInstance(e)) {
				throw e;
			} else {
				throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
			}
		} finally {
			client.close();
		}
	}

	@Override
	public Image getImageForPerformer(PerformerDTO performer) throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ReservationDTO reserveSeats(ReservationRequestDTO reservationRequest) throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void confirmReservation(ReservationDTO reservation) throws ServiceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void registerCreditCard(CreditCardDTO creditCard) throws ServiceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<BookingDTO> getBookings() throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void subscribeForNewsItems(NewsItemListener listener) {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public void cancelSubscription() {
		throw new UnsupportedOperationException();
	}

}
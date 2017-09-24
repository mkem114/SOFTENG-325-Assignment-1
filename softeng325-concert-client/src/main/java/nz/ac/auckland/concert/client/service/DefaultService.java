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
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericEntity;
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
		try {
			Client _client;
			_client = ClientBuilder.newClient();
			Invocation.Builder builder = _client.target(WEB_SERVICE_URI + "/concerts").request().accept(MediaType.APPLICATION_XML);
			Response response = builder.get();
			Set<ConcertDTO> concerts;
			if (response.getStatus() == Response.Status.OK.getStatusCode()) {
				concerts = response.readEntity(new GenericType<Set<ConcertDTO>>() {
				});
			} else {
				concerts = new HashSet<>();
			}
			_client.close();
			return concerts;
		} catch (Exception e) {
			throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
		}
	}

	@Override
	public Set<PerformerDTO> getPerformers() throws ServiceException {
		try {
			Client _client;
			_client = ClientBuilder.newClient();
			Invocation.Builder builder = _client.target(WEB_SERVICE_URI + "/performers").request().accept(MediaType.APPLICATION_XML);
			Response response = builder.get();
			Set<PerformerDTO> performers;
			if (response.getStatus() == Response.Status.OK.getStatusCode()) {
				performers = response.readEntity(new GenericType<Set<PerformerDTO>>() {
				});
			} else {
				performers = new HashSet<>();
			}
			_client.close();
			return performers;
		} catch (Exception e) {
			throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
		}
	}

	@Override
	public UserDTO createUser(UserDTO newUser) throws ServiceException {
		try {
			Client _client;
			_client = ClientBuilder.newClient();
			Invocation.Builder builder = _client.target(WEB_SERVICE_URI + "/user").request(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML);

			GenericEntity<UserDTO> wrapper = new GenericEntity<UserDTO>(newUser) {
			};
			builder.post(wrapper);

			/*Response response = builder.get();
			Set<PerformerDTO> performers;
			if (response.getStatus() == Response.Status.OK.getStatusCode()) {
				performers = response.readEntity(new GenericType<Set<PerformerDTO>>() {
				});
			} else {
				performers = new HashSet<>();
			}
			_client.close();
			return performers;*/
		} catch (Exception e) {
			throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
		}
	}

	@Override
	public UserDTO authenticateUser(UserDTO user) throws ServiceException {
		// TODO Auto-generated method stub
		return null;
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

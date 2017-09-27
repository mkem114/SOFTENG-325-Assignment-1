package nz.ac.auckland.concert.client.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import nz.ac.auckland.concert.common.dto.BookingDTO;
import nz.ac.auckland.concert.common.dto.ConcertDTO;
import nz.ac.auckland.concert.common.dto.CreditCardDTO;
import nz.ac.auckland.concert.common.dto.NewsItemDTO;
import nz.ac.auckland.concert.common.dto.PerformerDTO;
import nz.ac.auckland.concert.common.dto.ReservationDTO;
import nz.ac.auckland.concert.common.dto.ReservationRequestDTO;
import nz.ac.auckland.concert.common.dto.UserDTO;
import nz.ac.auckland.concert.common.message.Messages;

import javax.imageio.ImageIO;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;

public class DefaultService implements ConcertService, ConcertService.NewsItemListener {

	//TODO Move stuff to methods; cookies, error checking
	//Go through slides for conventions
	//Any commenting required
	//Hints
	//Report
	//Code in hand out
	//Recommended HTTP codes

	// Name of the S3 bucket that stores images.
	private static final String AWS_BUCKET = "a-little-bit-bucket";

	private static String WEB_SERVICE_URI = "http://localhost:10000/services/resource";

	private DefaultService _inst = this;

	private Cookie authenticationToken;
	private Set<ConcertDTO> _concertCache;
	private Set<PerformerDTO> _performerCache;

	@Override
	public Set<ConcertDTO> getConcerts() throws ServiceException {
		Client client = ClientBuilder.newClient();
		try {
			Invocation.Builder builder = client.target(WEB_SERVICE_URI + "/concerts")
					.request().accept(MediaType.APPLICATION_XML);
			Response response = builder.get();
			Set<ConcertDTO> concerts;
			if (response.getStatus() == Response.Status.OK.getStatusCode()) {
				concerts = response.readEntity(new GenericType<Set<ConcertDTO>>() {
				});
				_concertCache = concerts;
			} else if (response.getStatus() == Response.Status.NOT_MODIFIED.getStatusCode()) {
				return _concertCache;
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
			Invocation.Builder builder = client.target(WEB_SERVICE_URI + "/performers")
					.request().accept(MediaType.APPLICATION_XML);
			Response response = builder.get();
			Set<PerformerDTO> performers;
			if (response.getStatus() == Response.Status.OK.getStatusCode()) {
				performers = response.readEntity(new GenericType<Set<PerformerDTO>>() {
				});
				_performerCache = performers;
			} else if (response.getStatus() == Response.Status.NOT_MODIFIED.getStatusCode()) {
				return _performerCache;
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
			Invocation.Builder builder = client.target(WEB_SERVICE_URI + "/user")
					.request(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML);
			Response response = builder.post(Entity.entity(newUser, MediaType.APPLICATION_XML));

			int responseCode = response.getStatus();
			if (responseCode == Response.Status.CREATED.getStatusCode()) {
				authenticationToken = (Cookie) response.getCookies().values().toArray()[0];
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
			Invocation.Builder builder = client.target(WEB_SERVICE_URI + "/login")
					.request(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML);
			Response response = builder.post(Entity.entity(user, MediaType.APPLICATION_XML));

			int responseCode = response.getStatus();
			if (responseCode == Response.Status.ACCEPTED.getStatusCode()) {
				authenticationToken = (Cookie) response.getCookies().values().toArray()[0];
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
		//Just stores images locally as needed because getting all of them for every client would place a great load on
		//the servers and clients. It's unlikely an image will change so storing locally will mean that we the app is re
		//used there is less to load. The only downside is that rarely used images will be stored. A good pre-emptive
		//fetcher could be developed to try and get the best of both worlds.
		try {
			String imgName = performer.getImageName();

			try {
				File pathToFile = new File(imgName);
				return ImageIO.read(pathToFile);
			} catch (Exception e) {
				//Failed to read so it musn't exist yet
			}

			AmazonS3 s3 = AmazonS3ClientBuilder
					.standard()
					.withRegion(Regions.AP_SOUTHEAST_2)
					.withCredentials(
							new AWSStaticCredentialsProvider(new BasicAWSCredentials("", "")))
					.build();

			S3Object o = s3.getObject(AWS_BUCKET, imgName);
			S3ObjectInputStream s3is = o.getObjectContent();
			File file = new File(imgName);
			FileOutputStream fos = new FileOutputStream(file);
			byte[] read_buf = new byte[1024];
			int read_len = 0;
			while ((read_len = s3is.read(read_buf)) > 0) {
				fos.write(read_buf, 0, read_len);
			}
			s3is.close();
			fos.close();

			return ImageIO.read(file);
		} catch (AmazonServiceException | IOException e) {
			//TODO talk to UI designer about no image being able to load
			return null;
		}
	}

	@Override
	public ReservationDTO reserveSeats(ReservationRequestDTO reservationRequest) throws ServiceException {
		if (authenticationToken == null) {
			throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
		}

		Client client = ClientBuilder.newClient();
		try {
			Invocation.Builder builder = client.target(WEB_SERVICE_URI + "/reservations")
					.request(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML);
			//TODO Turn mapping of authentication cookie into mapped to authtoken or something
			Response response = builder.cookie("authenticationToken", authenticationToken.getValue())
					.post(Entity.entity(reservationRequest, MediaType.APPLICATION_XML));

			int responseCode = response.getStatus();
			if (responseCode == Response.Status.OK.getStatusCode()) {
				return response.readEntity(ReservationDTO.class);
			} else if (responseCode == Response.Status.NOT_FOUND.getStatusCode()) {
				throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
			} else if (responseCode == Response.Status.UNAUTHORIZED.getStatusCode()) {
				throw new ServiceException(Messages.BAD_AUTHENTICATON_TOKEN);
			} else if (responseCode == Response.Status.BAD_REQUEST.getStatusCode()) {
				throw new ServiceException(Messages.CONCERT_NOT_SCHEDULED_ON_RESERVATION_DATE);
			} else if (responseCode == Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE.getStatusCode()) {
				throw new ServiceException(Messages.INSUFFICIENT_SEATS_AVAILABLE_FOR_RESERVATION);
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
	public void confirmReservation(ReservationDTO reservation) throws ServiceException {
		if (authenticationToken == null) {
			throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
		}

		Client client = ClientBuilder.newClient();
		try {
			Invocation.Builder builder = client.target(WEB_SERVICE_URI + "/book")
					.request(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML);
			//TODO Turn mapping of authentication cookie into mapped to authtoken or something
			Response response = builder.cookie("authenticationToken", authenticationToken.getValue())
					.post(Entity.entity(reservation, MediaType.APPLICATION_XML));

			int responseCode = response.getStatus();
			if (responseCode == Response.Status.OK.getStatusCode()) {
				return;
			} else if (responseCode == Response.Status.NOT_FOUND.getStatusCode()) {
				throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
			} else if (responseCode == Response.Status.UNAUTHORIZED.getStatusCode()) {
				throw new ServiceException(Messages.BAD_AUTHENTICATON_TOKEN);
			} else if (responseCode == Response.Status.PAYMENT_REQUIRED.getStatusCode()) {
				throw new ServiceException(Messages.CREDIT_CARD_NOT_REGISTERED);
			} else if (responseCode == Response.Status.REQUEST_TIMEOUT.getStatusCode()) {
				throw new ServiceException(Messages.EXPIRED_RESERVATION);
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
	public void registerCreditCard(CreditCardDTO creditCard) throws ServiceException {
		if (authenticationToken == null) {
			throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
		}

		Client client = ClientBuilder.newClient();
		try {
			Invocation.Builder builder = client.target(WEB_SERVICE_URI + "/add_credit_card")
					.request(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML);
			//TODO Turn mapping of authentication cookie into mapped to authtoken or something
			Response response = builder.cookie("authenticationToken", authenticationToken.getValue())
					.post(Entity.entity(creditCard, MediaType.APPLICATION_XML));

			int responseCode = response.getStatus();
			if (responseCode == Response.Status.ACCEPTED.getStatusCode()) {
				return;
			} else if (responseCode == Response.Status.NOT_FOUND.getStatusCode()) {
				throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
			} else if (responseCode == Response.Status.UNAUTHORIZED.getStatusCode()) {
				throw new ServiceException(Messages.BAD_AUTHENTICATON_TOKEN);
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
	public Set<BookingDTO> getBookings() throws ServiceException {
		if (authenticationToken == null) {
			throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
		}

		Client client = ClientBuilder.newClient();
		try {
			Invocation.Builder builder = client.target(WEB_SERVICE_URI + "/bookings")
					.request(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML);
			//TODO Turn mapping of authentication cookie into mapped to authtoken or something
			Response response = builder.cookie("authenticationToken", authenticationToken.getValue()).get();

			int responseCode = response.getStatus();
			if (responseCode == Response.Status.OK.getStatusCode()) {
				return response.readEntity(new GenericType<Set<BookingDTO>>() {
				});
			} else if (responseCode == Response.Status.NOT_FOUND.getStatusCode()) {
				throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
			} else if (responseCode == Response.Status.UNAUTHORIZED.getStatusCode()) {
				throw new ServiceException(Messages.BAD_AUTHENTICATON_TOKEN);
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
	public void subscribeForNewsItems(NewsItemListener listener) {
		if (authenticationToken == null) {
			throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
		}
		Client client = ClientBuilder.newClient();
		try {
			Future future = client
					.target(WEB_SERVICE_URI + "/subscribe").request().cookie(authenticationToken).async()
					.get(new InvocationCallback<NewsItemDTO>() {

						@Override
						public void completed(NewsItemDTO newsItemDTO) {
							_inst.newsItemReceived(newsItemDTO);
						}

						@Override
						public void failed(Throwable throwable) {
							//TODO Talk to client or architect
						}
					});
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
	public void cancelSubscription() {
		if (authenticationToken == null) {
			throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
		}
		Client client = ClientBuilder.newClient();
		try {
			Invocation.Builder builder = client.target(WEB_SERVICE_URI + "/subscribe")
					.request(MediaType.APPLICATION_XML);
			Response response = builder
					.cookie("authenticationToken", authenticationToken.getValue())
					.delete();
			int responseCode = response.getStatus();
			if (responseCode == Response.Status.OK.getStatusCode()) {
				return;
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
	public void newsItemReceived(NewsItemDTO newsItem) {
		Client client = ClientBuilder.newClient();
		final WebTarget target =
				client.target("chat");
		target.request()
				.async()
				.get(new InvocationCallback<String>() {
					public void completed(String message) {
						target.request().async().get(this);
					}

					public void failed(Throwable t) {
					}
				});
	}
}
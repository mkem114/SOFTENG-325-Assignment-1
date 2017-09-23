package nz.ac.auckland.concert.service.services;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * JAX-RS Application subclass for the Concert Web service.
 * 
 * 
 *
 */
public class ConcertApplication extends Application {

	// This property should be used by your Resource class. It represents the 
	// period of time, in seconds, that reservations are held for. If a
	// reservation isn't confirmed within this period, the reserved seats are
	// returned to the pool of seats available for booking.
	//
	// This property is used by class ConcertServiceTest.
	public static final int RESERVATION_EXPIRY_TIME_IN_SECONDS = 5;

	private Set<Object> _singlestons = new HashSet<>();
	private Set<Class<?>> _classes = new HashSet<>();

	public ConcertApplication() {
		_singlestons.add(new ConcertResource());
		PersistenceManager pm = new PersistenceManager().instance();
	}

	@Override
	public Set<Object> getSingletons() { return _singlestons;}

	@Override
	public Set<Class<?>> getClasses() {return _classes;}
}

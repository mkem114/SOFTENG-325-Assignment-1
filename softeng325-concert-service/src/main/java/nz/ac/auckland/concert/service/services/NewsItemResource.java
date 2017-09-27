package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.service.domain.NewsItem;
import nz.ac.auckland.concert.service.domain.User;

import javax.persistence.EntityManager;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewsItemResource {

	private Map<Cookie, AsyncResponse> _subscriptions = new HashMap<>();
	private Map<AsyncResponse, Cookie> _subscriptionsReverse = new HashMap<>();

	@POST
	@Path("/subscribe")
	public Response subscribe(@Suspended AsyncResponse response, @CookieParam("authenticationToken") Cookie token) {
		try {
			try {
				_subscriptions.put(token, response);
				_subscriptionsReverse.put(response, token);
				//Get latest updates
				EntityManager em = PersistenceManager.instance().createEntityManager();
				em.getTransaction().begin();
				try {
					User u = (User) em.createQuery("SELECT U FROM User U WHERE U._token = :token")
							.setParameter("token", _subscriptionsReverse.get(response).getValue())
							.getSingleResult();
					List<NewsItem> news = (List<NewsItem>) em.createQuery("SELECT n FROM NewsItem n")
							.getResultList();
					if (!(u == null)) {
						boolean after = false;
						for (NewsItem n : news) {
							if (after) {
								response.resume(n.get_content());
							}
							if (n.get_niID().equals(u.get_last().get_niID())) {
								after = true;
							}
						}
					}
				} finally {
					em.close();
				}
				return Response.ok().build();
			} catch (Exception e) {
				//Message wasn't sent,
				_subscriptions.remove(_subscriptionsReverse.get(response));
				_subscriptionsReverse.remove(response);
				return Response.serverError().build();
			}
		} catch (Exception e) {
			return Response.serverError().build();
		}
	}

	@DELETE
	@Path("/unsubscribe")
	public Response unsubscribe(@CookieParam("authenticationToken") Cookie token) {
		try {
			_subscriptionsReverse.remove(_subscriptions.get(token));
			_subscriptions.remove(token);
			return Response.ok().build();
		} catch (Exception e) {
			return Response.serverError().build();
		}
	}

	@POST
	public void send(String message) {
		for (AsyncResponse response : _subscriptions.values()) {
			try {
				response.resume(message);
				EntityManager em = PersistenceManager.instance().createEntityManager();
				em.getTransaction().begin();
				try {
					User u = (User) em.createQuery("SELECT U FROM User U WHERE U._token = :token")
							.setParameter("token", _subscriptionsReverse.get(response).getValue())
							.getSingleResult();
					NewsItem n = (NewsItem) em.createQuery("SELECT n FROM NewsItem n where n._content=:msg")
							.setParameter("msg", message)
							.getSingleResult();
					if (!(u == null)) {
						u.set_last(n);
						em.merge(u);
						em.getTransaction().commit();
					}
				} finally {
					em.close();
				}
			} catch (Exception e) {
				//Message wasn't sent,
				_subscriptions.remove(_subscriptionsReverse.get(response));
				_subscriptionsReverse.remove(response);
			}
		}
	}
}

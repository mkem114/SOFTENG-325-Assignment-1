package nz.ac.auckland.concert.service.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;


@Entity
@IdClass(SeatId.class)
@Table(name = "SEATS")
public class Seat {

	@Id
	@ManyToOne
	private Concert _concert;
	@Id
	@Column(name = "datetime", nullable = false)
	private LocalDateTime _datetime;
	@Id
	@Column(name = "number", nullable = false)
	private Integer _number;
	@ManyToOne
	private Reservation _reservation;
	@Column(name = "status", nullable = false)
	@Enumerated(EnumType.STRING)
	private SeatStatus _status = SeatStatus.FREE;

	public Concert get_concert() {
		return _concert;
	}

	public void set_concert(Concert _concert) {
		this._concert = _concert;
	}

	public LocalDateTime get_datetime() {
		return _datetime;
	}

	public void set_datetime(LocalDateTime _datetime) {
	}

	public Integer get_number() {
		return _number;
	}

	public void set_number(Integer _number) {
		this._number = _number;
	}

	public Reservation get_reservation() {
		return _reservation;
	}

	public void set_reservation(Reservation _reservation) {
		this._reservation = _reservation;
		_datetime = _reservation.get_dateTime();
	}

	public SeatStatus get_status() {
		return _status;
	}

	public void set_status(SeatStatus _status) {
		this._status = _status;
	}

	public enum SeatStatus {
		BOOKED, PENDING, FREE
	}
}

class SeatId implements Serializable {
	Concert _concert;
	LocalDateTime _datetime;
	Integer _number;
}

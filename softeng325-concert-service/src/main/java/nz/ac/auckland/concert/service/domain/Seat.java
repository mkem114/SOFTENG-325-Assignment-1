package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.dto.SeatDTO;
import nz.ac.auckland.concert.common.types.SeatNumber;
import nz.ac.auckland.concert.common.types.SeatRow;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;
import java.io.Serializable;
import java.time.LocalDateTime;

import static nz.ac.auckland.concert.common.types.SeatRow.E;
import static nz.ac.auckland.concert.common.types.SeatRow.F;
import static nz.ac.auckland.concert.common.types.SeatRow.G;
import static nz.ac.auckland.concert.common.types.SeatRow.H;
import static nz.ac.auckland.concert.common.types.SeatRow.J;
import static nz.ac.auckland.concert.common.types.SeatRow.K;
import static nz.ac.auckland.concert.common.types.SeatRow.L;
import static nz.ac.auckland.concert.common.types.SeatRow.M;
import static nz.ac.auckland.concert.common.types.SeatRow.N;
import static nz.ac.auckland.concert.common.types.SeatRow.O;
import static nz.ac.auckland.concert.common.types.SeatRow.P;
import static nz.ac.auckland.concert.common.types.SeatRow.R;


@Entity
@IdClass(SeatId.class)
@Table(name = "SEATS")
public class Seat {

	@Version
	private int _version;

	public static final int BANDA_MAX = 167;
	public static final int BANDB_MAX = 81;
	public static final int BANDC_MAX = 126;
	@Column(name = "timestamp", nullable = true)
	private LocalDateTime _timestamp;

	@Id
	@ManyToOne
	private Concert _concert;

	@Id
	@Column(name = "datetime", nullable = false)
	private LocalDateTime _datetime;

	@Id
	@Column(name = "number", nullable = false)
	private Integer _number;

	public Seat(Integer i) {
		_number = i.intValue();
	}

	@ManyToOne
	private Reservation _reservation;

	@Column(name = "status", nullable = false)
	@Enumerated(EnumType.STRING)
	private SeatStatus _status = SeatStatus.FREE;

	public Seat(SeatDTO dto) {
		SeatRow row = dto.getRow();
		int seatNumInRow = dto.getNumber().intValue();

		_number = 0;
		switch (row) {
			// BAND A
			case M:
				_number += 25;
			case L:
				_number += 25;
			case K:
				_number += 25;
			case J:
				_number += 23;
			case G:
				_number += 22;
			case F:
				_number += 21;
			case E:
				_number += seatNumInRow;
				break;
			//BAND B
			case D:
				_number += 21;
			case C:
				_number += 20;
			case B:
				_number += 19;
			case A:
				_number += seatNumInRow + BANDA_MAX;
				break;
			//BAND C
			case R:
				_number += 26;
			case P:
				_number += 26;
			case O:
				_number += 26;
			case N:
				_number += 22;
			case H:
				_number += seatNumInRow + BANDA_MAX + BANDB_MAX;
				break;
		}
	}

	public int get_version() {
		return _version;
	}

	public Seat() {
	}

	public SeatDTO convertToDTO() {
		int count = _number;
		int[] seatsInRows;
		SeatRow[] rows;
		if (_number <= BANDA_MAX) {
			seatsInRows = new int[]{21, 22, 23, 25, 25, 25, 26};
			rows = new SeatRow[]{E, F, G, J, K, L, M};
		} else if (_number <= BANDB_MAX) {
			seatsInRows = new int[]{19, 20, 21, 21};
			rows = new SeatRow[]{E, F, G, J, K, L, M};
			count -= BANDB_MAX;
		} else {
			seatsInRows = new int[]{22, 26, 26, 26, 26};
			rows = new SeatRow[]{H, N, O, P, R};
			count -= (BANDB_MAX + BANDA_MAX);
		}

		SeatDTO dto = new SeatDTO();
		for (int i = 0; i < rows.length; i++) {
			if (count > seatsInRows[i]) {
				count -= seatsInRows[i];
			} else {
				dto = new SeatDTO(rows[i], new SeatNumber(count));
				break;
			}
		}
		return dto;
	}

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

	public void set_version(int _version) {
		this._version = _version;
	}

	public LocalDateTime get_timestamp() {
		return _timestamp;
	}

	public void set_timestamp(LocalDateTime _timestamp) {
		this._timestamp = _timestamp;
	}

	public enum SeatStatus {
		BOOKED, PENDING, FREE
	}
}

class SeatId implements Serializable {
	Concert _concert;
	LocalDateTime _datetime;
	Integer _number;

	@Override
	public int hashCode() {
		int result = _concert != null ? _concert.hashCode() : 0;
		result = 31 * result + (_datetime != null ? _datetime.hashCode() : 0);
		result = 31 * result + (_number != null ? _number.hashCode() : 0);
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		SeatId seatId = (SeatId) o;

		if (_concert != null ? !_concert.equals(seatId._concert) : seatId._concert != null) return false;
		if (_datetime != null ? !_datetime.equals(seatId._datetime) : seatId._datetime != null) return false;
		return _number != null ? _number.equals(seatId._number) : seatId._number == null;
	}
}

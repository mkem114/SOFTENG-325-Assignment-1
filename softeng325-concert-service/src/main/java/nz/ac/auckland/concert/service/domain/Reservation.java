package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.types.PriceBand;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="RESERVATIONS")
public class Reservation {

    @Id
    @GeneratedValue
    @Column(name = "rid", nullable = false)
    private Long _rID;

    @ManyToOne
    @JoinColumn(name = "user", nullable = false)
    private User _user;

    @ManyToOne
    @JoinColumn(name = "concert", nullable = false)
    private Concert _concert;

    @Column(name = "datetime")
    private LocalDateTime _dateTime;

    //SEATS
    @Column(name = "priceband", nullable = false)
    @Enumerated(EnumType.STRING)
    private PriceBand _priceBand;

    public Long get_rID() {
        return _rID;
    }

    public void set_rID(Long _rID) {
        this._rID = _rID;
    }

    public User get_user() {
        return _user;
    }

    public void set_user(User _user) {
        this._user = _user;
    }

    public Concert get_concert() {
        return _concert;
    }

    public void set_concert(Concert _concert) {
        this._concert = _concert;
    }

    public LocalDateTime get_dateTime() {
        return _dateTime;
    }

    public void set_dateTime(LocalDateTime _dateTime) {
        this._dateTime = _dateTime;
    }
/*
    public Set<SeatDTO> get_seats() {
        return _seats;
    }

    public void set_seats(Set<SeatDTO> _seats) {
        this._seats = _seats;
    }*/

    public PriceBand get_priceBand() {
        return _priceBand;
    }

    public void set_priceBand(PriceBand _priceBand) {
        this._priceBand = _priceBand;
    }
}

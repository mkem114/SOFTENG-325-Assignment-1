package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.dto.CreditCardDTO;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name="CREDITCARDS")
public class CreditCard {

    @Id
    @GeneratedValue
    @Column(name = "ccid", nullable = false)
    private int _ccID;

    // Just doing a dumb implementation of the credit card. IRL should store the authorisation. Also never store CCV
    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private CreditCardDTO.Type _type;

    @Column(name = "name", nullable = false)
    private String _name;

    @Column(name = "number", nullable = false)
    private String _number;

    @Column(name = "date", nullable = false)
    private LocalDate _expiryDate;

    public int get_ccID() {
        return _ccID;
    }

    public void set_ccID(int _ccID) {
        this._ccID = _ccID;
    }

    public CreditCardDTO.Type get_type() {
        return _type;
    }

    public void set_type(CreditCardDTO.Type _type) {
        this._type = _type;
    }

    public String get_name() {
        return _name;
    }

    public void set_name(String _name) {
        this._name = _name;
    }

    public String get_number() {
        return _number;
    }

    public void set_number(String _number) {
        this._number = _number;
    }

    public LocalDate get_expiryDate() {
        return _expiryDate;
    }

    public void set_expiryDate(LocalDate _expiryDate) {
        this._expiryDate = _expiryDate;
    }
}

package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.dto.CreditCardDTO;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name="CREDITCARDS")
public class CreditCard {

	@Id
	@Column(name = "number", nullable = false, unique = true)
	private String _number;

    // Just doing a dumb implementation of the credit card. IRL should store the authorisation. Also never store CCV
    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private CreditCardDTO.Type _type;

    @Column(name = "name", nullable = false)
    private String _name;

    @Column(name = "date", nullable = false)
    private LocalDate _expiryDate;

	public CreditCard(CreditCardDTO dto) {
		_number = dto.getNumber();
		_name = dto.getName();
		_expiryDate = dto.getExpiryDate();
		_type = dto.getType();
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

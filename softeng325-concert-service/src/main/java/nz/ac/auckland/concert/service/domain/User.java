package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.dto.UserDTO;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name="USERS")
public class User {

    @Id
	@Column(name = "username", nullable = false, unique = true)
	private String _username;

    @Column(name = "passwordhash", nullable = false)
    private String _passwordHash; //I CBF implementing just know I thought of it

    @ManyToOne
	@JoinColumn(name = "creditcard", nullable = true)
	private CreditCard _creditCard;

    @Column(name = "firstname", nullable = false)
    private String _firstname;

    @Column(name = "lastname", nullable = false)
    private String _lastname;

	@Column(name = "token", nullable = true)
	private String _token;

	@Column(name = "tokenTimeStamp", nullable = true)
	private LocalDateTime _tokenTimeStamp;

	public User() {
	}

	public User(UserDTO dto) {
		this._username = dto.getUsername();
		this._passwordHash = dto.getPassword();
		this._firstname = dto.getFirstname();
		this._lastname = dto.getLastname();
	}

	public String get_username() {
		return _username;
    }

	public void set_user2name(String _username) {
		this._username = _username;
    }

    public String get_passwordHash() {
        return _passwordHash;
    }

    public void set_passwordHash(String _passwordHash) {
        this._passwordHash = _passwordHash;
    }

    public CreditCard get_card() {
        return _creditCard;
    }

    public void set_card(CreditCard _card) {
        this._creditCard = _card;
    }

    public String get_firstname() {
        return _firstname;
    }

    public void set_firstname(String _firstname) {
        this._firstname = _firstname;
    }

    public String get_lastname() {
        return _lastname;
    }

    public void set_lastname(String _lastname) {
        this._lastname = _lastname;
    }

	public CreditCard get_creditCard() {
		return _creditCard;
	}

	public void set_creditCard(CreditCard _creditCard) {
		this._creditCard = _creditCard;
	}

	public String get_token() {
		return _token;
	}

	public void set_token(String _token) {
		this._token = _token;
	}

	public LocalDateTime get_tokenTimeStamp() {
		return _tokenTimeStamp;
	}

	public void set_tokenTimeStamp(LocalDateTime _tokenTimeStamp) {
		this._tokenTimeStamp = _tokenTimeStamp;
	}

	public UserDTO convertToDTO() {
		return new UserDTO(_username, _passwordHash, _lastname, _firstname);
	}
}

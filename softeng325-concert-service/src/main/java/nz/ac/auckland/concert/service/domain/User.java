package nz.ac.auckland.concert.service.domain;

import javax.persistence.*;

@Entity
@Table(name="USERS")
public class User {

    @Id
    @Column(name = "username", nullable = false)
    private String _username;

    @Column(name = "passwordhash", nullable = false)
    private String _passwordHash; //I CBF implementing just know I thought of it

    @ManyToOne
    @JoinColumn(name = "creditcard", nullable = false)
    private CreditCard _creditCard;

    @Column(name = "firstname", nullable = false)
    private String _firstname;

    @Column(name = "lastname", nullable = false)
    private String _lastname;

    public String get_username() {
        return _username;
    }

    public void set_username(String _username) {
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
}

package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.dto.PerformerDTO;
import nz.ac.auckland.concert.common.types.Genre;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="PERFORMERS")
public class Performer {

    @Id
    @GeneratedValue
    @Column(name = "pid", nullable = false)
    private Long _pID;

    @Column(name = "name", nullable = false)
    private String _name;

    @Column(name = "image_name", nullable = true)
    private String _imageName;

    @Column(name = "genre", nullable = false)
    @Enumerated(EnumType.STRING)
    private Genre _genre;

    @ManyToMany
    @JoinTable(name = "CONCERT_PERFORMER", joinColumns = @JoinColumn(name = "pid"), inverseJoinColumns = @JoinColumn(name = "cid"))
    @Column(name = "concert", nullable = false)
    private Set<Concert> _concerts;

    public Long get_pID() {
        return _pID;
    }

    public void set_pID(Long _pID) {
        this._pID = _pID;
    }

    public String get_name() {
        return _name;
    }

    public void set_name(String _name) {
        this._name = _name;
    }

    public String get_imageName() {
        return _imageName;
    }

    public void set_imageName(String _imageName) {
        this._imageName = _imageName;
    }

    public Genre get_genre() {
        return _genre;
    }

    public void set_genre(Genre _genre) {
        this._genre = _genre;
    }

    public Set<Concert> get_concerts() {
        return _concerts;
    }

    public void set_concerts(Set<Concert> _concerts) {
        this._concerts = _concerts;
    }

	public PerformerDTO convertToDTO() {
		Set<Long> concertIDs = new HashSet<>();
		for (Concert c : _concerts) {
			concertIDs.add(c.get_cID());
		}
		return new PerformerDTO(_pID, _name, _imageName, _genre, concertIDs);
	}
}

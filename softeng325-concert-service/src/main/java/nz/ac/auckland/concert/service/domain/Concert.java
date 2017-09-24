package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.types.PriceBand;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.MapKeyColumn;
import javax.persistence.MapKeyEnumerated;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name="CONCERTS")
public class Concert {

    @Id
    @GeneratedValue
    @Column(name = "cid", nullable = false)
    private Long _cID;

    @Column(name = "title", nullable = false)
    private String _title;

    @ElementCollection
    @CollectionTable(name = "CONCERT_DATES", joinColumns =@JoinColumn(name = "cid"))
	@Column(name = "datetime", nullable = false)
	private Set<LocalDateTime> _dates;

    @ElementCollection
    @JoinTable(name = "CONCERT_TARIFFS", joinColumns = @JoinColumn(name = "cid"))
    @MapKeyColumn(name = "price_band")
    @Column(name = "tariff", nullable = false)
    @MapKeyEnumerated(EnumType.STRING)
    private Map<PriceBand, BigDecimal> _tariff;

    @ManyToMany
	@JoinTable(name = "CONCERT_PERFORMER",
			joinColumns = @JoinColumn(name = "cid"),
			inverseJoinColumns = @JoinColumn(name = "pid"))
	@Column(name = "performer", nullable = false)
    private Set<Performer> _performerIds;

    public Long get_cID() {
        return _cID;
    }

    public void set_cID(Long _cID) {
        this._cID = _cID;
    }

    public String get_title() {
        return _title;
    }

    public void set_title(String _title) {
        this._title = _title;
    }

    public Set<LocalDateTime> get_dates() {
        return _dates;
    }

    public void set_dates(Set<LocalDateTime> _dates) {
        this._dates = _dates;
    }

    public Map<PriceBand, BigDecimal> get_tariff() {
        return _tariff;
    }

    public void set_tariff(Map<PriceBand, BigDecimal> _tariff) {
        this._tariff = _tariff;
    }

    public Set<Performer> get_performerIds() {
        return _performerIds;
    }

    public void set_performerIds(Set<Performer> _performerIds) {
        this._performerIds = _performerIds;
    }
}

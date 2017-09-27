package nz.ac.auckland.concert.service.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name="NEWSITEMS")
public class NewsItem {

    @Id
    @GeneratedValue
    @Column(name = "nid", nullable = false)
    private Long _niID;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime _timestamp;

    @Column(name = "content", nullable = false)
    private String _content;

	public NewsItem() {
	}

	public Long get_niID() {
		return _niID;
    }

    public void set_niID(Long _niID) {
        this._niID = _niID;
    }

    public LocalDateTime get_timestamp() {
        return _timestamp;
    }

    public void set_timestamp(LocalDateTime _timestamp) {
        this._timestamp = _timestamp;
    }

    public String get_content() {
        return _content;
    }

    public void set_content(String _content) {
        this._content = _content;
    }

	@Override
	public int hashCode() {
		return _niID != null ? _niID.hashCode() : 0;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof NewsItem)) return false;

		NewsItem newsItem = (NewsItem) o;

		return _niID != null ? _niID.equals(newsItem._niID) : newsItem._niID == null;
	}
}

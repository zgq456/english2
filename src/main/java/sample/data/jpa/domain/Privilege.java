package sample.data.jpa.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name = "priv")
public class Privilege {

	private static final long serialVersionUID = -7360469848024656212L;

	@Id
	@GeneratedValue
	private Long id;

	@Column(nullable = false, length = 40)
	private String oepration;

	@Column(nullable = false, length = 40)
	private String target;

	@Column(nullable = false, length = 40)
	private String url;

	// @ManyToOne
	// @JoinColumn(name = "role_id", nullable = false)
	// private RoleEntity role;
	@ManyToMany(mappedBy = "privileges")
	private Set<Role> roles = new HashSet<Role>();

	public String getOepration() {
		return this.oepration;
	}

	public void setOepration(String oepration) {
		this.oepration = oepration;
	}

	public String getTarget() {
		return this.target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public Set<Role> getRoles() {
		return this.roles;
	}

	public void setRole(Set<Role> roles) {
		this.roles = roles;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}
}

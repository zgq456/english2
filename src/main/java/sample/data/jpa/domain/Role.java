package sample.data.jpa.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

@Entity
public class Role {
	@Id
	@GeneratedValue
	private Long id;

	@Column(nullable = false, length = 40, unique = true)
	private String name;

	@ManyToMany(mappedBy = "roles")
	private Set<User> users = new HashSet<User>();

	// @OneToMany(mappedBy="role", fetch=FetchType.EAGER, cascade=CascadeType.ALL)
	@ManyToMany
	@JoinTable(name = "role_priv", joinColumns = { @JoinColumn(name = "role_id") }, inverseJoinColumns = { @JoinColumn(name = "priv_id") })
	private Set<Privilege> privileges = new HashSet<Privilege>();

	public Set<Privilege> getPrivileges() {
		return this.privileges;
	}

	public void setPrivileges(Set<Privilege> privileges) {
		this.privileges = privileges;
	}

	public Set<User> getUsers() {
		return this.users;
	}

	public void setUsers(Set<User> users) {
		this.users = users;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return this.name;
	}
}

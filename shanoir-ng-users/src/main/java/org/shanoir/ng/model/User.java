package org.shanoir.ng.model;

import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.joda.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

/**
 * User
 */
@Entity
@Table(name = "users")
public class User {

	@JsonProperty("id")
	@Id
	private Long id = null;

	@JsonProperty("username")
	private String username = null;

	@JsonProperty("password")
	private String password = null;

	@JsonProperty("firstName")
	private String firstName = null;

	@JsonProperty("lastName")
	private String lastName = null;

	@JsonProperty("email")
	private String email = null;

	@JsonProperty("teamName")
	private String teamName = null;

	@JsonProperty("isMedical")
	private Boolean isMedical = null;

	@JsonProperty("canImportFromPACS")
	private Boolean canImportFromPACS = null;

	@JsonProperty("creationDate")
	private LocalDate creationDate = null;

	@JsonProperty("expirationDate")
	private LocalDate expirationDate = null;

	@JsonProperty("lastLogin")
	private LocalDate lastLogin = null;

	@JsonProperty("canAccessToDicomAssociation")
	private Boolean canAccessToDicomAssociation = null;

	@JsonProperty("motivation")
	private String motivation = null;

	@JsonProperty("role")
	@ManyToOne @JoinColumn(name="ROLE_ID")
	private Role role = null;

	public User id(Long id) {
		this.id = id;
		return this;
	}

	/**
	 * Get id
	 *
	 * @return id
	 **/
	@ApiModelProperty(value = "")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User username(String username) {
		this.username = username;
		return this;
	}

	/**
	 * Get username
	 *
	 * @return username
	 **/
	@ApiModelProperty(required = true, value = "")
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public User password(String password) {
		this.password = password;
		return this;
	}

	/**
	 * Get password
	 *
	 * @return password
	 **/
	@ApiModelProperty(value = "")
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public User firstName(String firstName) {
		this.firstName = firstName;
		return this;
	}

	/**
	 * Get firstName
	 *
	 * @return firstName
	 **/
	@ApiModelProperty(value = "")
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public User lastName(String lastName) {
		this.lastName = lastName;
		return this;
	}

	/**
	 * Get lastName
	 *
	 * @return lastName
	 **/
	@ApiModelProperty(value = "")
	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public User email(String email) {
		this.email = email;
		return this;
	}

	/**
	 * Get email
	 *
	 * @return email
	 **/
	@ApiModelProperty(required = true, value = "")
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public User teamName(String teamName) {
		this.teamName = teamName;
		return this;
	}

	/**
	 * Get teamName
	 *
	 * @return teamName
	 **/
	@ApiModelProperty(value = "")
	public String getTeamName() {
		return teamName;
	}

	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}

	public User isMedical(Boolean isMedical) {
		this.isMedical = isMedical;
		return this;
	}

	/**
	 * Get isMedical
	 *
	 * @return isMedical
	 **/
	@ApiModelProperty(value = "")
	public Boolean getIsMedical() {
		return isMedical;
	}

	public void setIsMedical(Boolean isMedical) {
		this.isMedical = isMedical;
	}

	public User canImportFromPACS(Boolean canImportFromPACS) {
		this.canImportFromPACS = canImportFromPACS;
		return this;
	}

	/**
	 * Get canImportFromPACS
	 *
	 * @return canImportFromPACS
	 **/
	@ApiModelProperty(value = "")
	public Boolean getCanImportFromPACS() {
		return canImportFromPACS;
	}

	public void setCanImportFromPACS(Boolean canImportFromPACS) {
		this.canImportFromPACS = canImportFromPACS;
	}

	public User creationDate(LocalDate creationDate) {
		this.creationDate = creationDate;
		return this;
	}

	/**
	 * Get creationDate
	 *
	 * @return creationDate
	 **/
	@ApiModelProperty(value = "")
	public LocalDate getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(LocalDate creationDate) {
		this.creationDate = creationDate;
	}

	public User expirationDate(LocalDate expirationDate) {
		this.expirationDate = expirationDate;
		return this;
	}

	/**
	 * Get expirationDate
	 *
	 * @return expirationDate
	 **/
	@ApiModelProperty(value = "")
	public LocalDate getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(LocalDate expirationDate) {
		this.expirationDate = expirationDate;
	}

	public User lastLogin(LocalDate lastLogin) {
		this.lastLogin = lastLogin;
		return this;
	}

	/**
	 * Get lastLogin
	 *
	 * @return lastLogin
	 **/
	@ApiModelProperty(value = "")
	public LocalDate getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(LocalDate lastLogin) {
		this.lastLogin = lastLogin;
	}

	public User canAccessToDicomAssociation(Boolean canAccessToDicomAssociation) {
		this.canAccessToDicomAssociation = canAccessToDicomAssociation;
		return this;
	}

	/**
	 * Get canAccessToDicomAssociation
	 *
	 * @return canAccessToDicomAssociation
	 **/
	@ApiModelProperty(value = "")
	public Boolean getCanAccessToDicomAssociation() {
		return canAccessToDicomAssociation;
	}

	public void setCanAccessToDicomAssociation(Boolean canAccessToDicomAssociation) {
		this.canAccessToDicomAssociation = canAccessToDicomAssociation;
	}

	public User motivation(String motivation) {
		this.motivation = motivation;
		return this;
	}

	/**
	 * Get motivation
	 *
	 * @return motivation
	 **/
	@ApiModelProperty(value = "")
	public String getMotivation() {
		return motivation;
	}

	public void setMotivation(String motivation) {
		this.motivation = motivation;
	}

	public User role(Role role) {
		this.role = role;
		return this;
	}

	/**
	 * Get role
	 *
	 * @return role
	 **/
	@ApiModelProperty(value = "")
	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		User user = (User) o;
		if (this.id != null && user.id != null) {
			return Objects.equals(this.id, user.id);
		}
		return Objects.equals(this.username, user.username) && Objects.equals(this.password, user.password)
				&& Objects.equals(this.firstName, user.firstName) && Objects.equals(this.lastName, user.lastName)
				&& Objects.equals(this.email, user.email) && Objects.equals(this.teamName, user.teamName)
				&& Objects.equals(this.isMedical, user.isMedical)
				&& Objects.equals(this.canImportFromPACS, user.canImportFromPACS)
				&& Objects.equals(this.creationDate, user.creationDate)
				&& Objects.equals(this.expirationDate, user.expirationDate)
				&& Objects.equals(this.lastLogin, user.lastLogin)
				&& Objects.equals(this.canAccessToDicomAssociation, user.canAccessToDicomAssociation)
				&& Objects.equals(this.motivation, user.motivation) && Objects.equals(this.role, user.role);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, username, password, firstName, lastName, email, teamName, isMedical, canImportFromPACS,
				creationDate, expirationDate, lastLogin, canAccessToDicomAssociation, motivation, role);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class User {\n");

		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    username: ").append(toIndentedString(username)).append("\n");
		sb.append("    password: ").append(toIndentedString(password)).append("\n");
		sb.append("    firstName: ").append(toIndentedString(firstName)).append("\n");
		sb.append("    lastName: ").append(toIndentedString(lastName)).append("\n");
		sb.append("    email: ").append(toIndentedString(email)).append("\n");
		sb.append("    teamName: ").append(toIndentedString(teamName)).append("\n");
		sb.append("    isMedical: ").append(toIndentedString(isMedical)).append("\n");
		sb.append("    canImportFromPACS: ").append(toIndentedString(canImportFromPACS)).append("\n");
		sb.append("    creationDate: ").append(toIndentedString(creationDate)).append("\n");
		sb.append("    expirationDate: ").append(toIndentedString(expirationDate)).append("\n");
		sb.append("    lastLogin: ").append(toIndentedString(lastLogin)).append("\n");
		sb.append("    canAccessToDicomAssociation: ").append(toIndentedString(canAccessToDicomAssociation))
				.append("\n");
		sb.append("    motivation: ").append(toIndentedString(motivation)).append("\n");
		sb.append("    role: ").append(toIndentedString(role)).append("\n");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Convert the given object to string with each line indented by 4 spaces
	 * (except the first line).
	 */
	private String toIndentedString(java.lang.Object o) {
		if (o == null) {
			return "null";
		}
		return o.toString().replace("\n", "\n    ");
	}
}

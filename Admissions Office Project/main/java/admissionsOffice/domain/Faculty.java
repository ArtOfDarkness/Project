import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name = "faculty")
public class Faculty implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "faculty_id")
	private Integer id;
	@Column
	@NotBlank(message = "Назва факультету не може бути пустою!")
	private String title;
	
	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "subject_faculty", joinColumns = @JoinColumn(name = "faculty_id"), inverseJoinColumns = @JoinColumn(name = "subject_id"))
	private Set<Subject> examSubjects;
	
	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "subject_coeffs")
	@MapKeyColumn(name = "subject_id")
	private Map<Subject, Double> subjectCoeffs;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "faculty")
	@Column(nullable = false)
	private Set<Speciality> specialities;

	
	public Faculty() {	}

	public Faculty(String title, Set<Subject> examSubjects) {
		this.title = title;
		this.examSubjects = examSubjects;		
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Set<Subject> getExamSubjects() {
		return examSubjects;
	}

	public void setExamSubjects(Set<Subject> examSubjects) {
		this.examSubjects = examSubjects;
	}

	public Map<Subject, Double> getSubjectCoeffs() {
		return subjectCoeffs;
	}

	public void setSubjectCoeffs(Map<Subject, Double> subjectCoeffs) {
		this.subjectCoeffs = subjectCoeffs;
	}

	public Set<Speciality> getSpecialities() {
		return specialities;
	}

	public void setSpecialities(Set<Speciality> specialities) {
		this.specialities = specialities;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Faculty other = (Faculty) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Faculty [id=" + id + ", title=" + title + "]";
	}
}

package al.bytesquad.petstoreandclinic.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;
import java.util.Objects;

@Table(name = "service")
@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class PetServices {
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        PetServices petService = (PetServices) o;
        return id != null && Objects.equals(id, petService.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public long getId() {
        return this.id;
    }
}

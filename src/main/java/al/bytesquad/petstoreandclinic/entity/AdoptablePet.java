package al.bytesquad.petstoreandclinic.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;


import java.time.LocalDate;

@Table(name = "adoptablePet")
@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class AdoptablePet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "species")
    private String species;

    @Column(name = "breed")
    private String breed;

    @Column(name = "gender")
    private String gender;

    @Column(name = "age")
    private LocalDate age;

    @Column(name = "colour")
    private String colour;

    @Column(name = "size")
    private String size;

    @Column(name = "behavior")
    private String behavior;
}

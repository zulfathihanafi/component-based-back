package al.bytesquad.petstoreandclinic.payload.entityDTO;

import java.time.LocalDate;

import lombok.Data;

@Data
public class AdoptablePetDTO {
    private Long id;
    private String name;
    private String species;
    private String breed;
    private String gender;
    private LocalDate age;
    private String colour;
    private String size;
    private String behavior;
}

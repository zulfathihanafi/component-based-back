package al.bytesquad.petstoreandclinic.payload.entityDTO;

import lombok.Data;

@Data
public class ClientDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
}

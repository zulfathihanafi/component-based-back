package al.bytesquad.petstoreandclinic.payload.entityDTO;

import al.bytesquad.petstoreandclinic.entity.Client;
import al.bytesquad.petstoreandclinic.entity.Doctor;
import al.bytesquad.petstoreandclinic.entity.Pet;
import al.bytesquad.petstoreandclinic.entity.PetServices;
import lombok.Data;

@Data
public class AppointmentDTO {
    private long id;
    private Client client;
    private Pet pet;
    private Doctor doctor;
    private PetServices petServices;
    private String startTime;
    private String finishTime;
}

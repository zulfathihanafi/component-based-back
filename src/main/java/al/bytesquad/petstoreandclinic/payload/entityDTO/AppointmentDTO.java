package al.bytesquad.petstoreandclinic.payload.entityDTO;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

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
    @JsonFormat(pattern = "EEE MMM dd HH:mm:ss z yyyy", timezone = "Asia/Kuala_Lumpur")
    private Date startTime;

    @JsonFormat(pattern = "EEE MMM dd HH:mm:ss z yyyy", timezone = "Asia/Kuala_Lumpur")
    private Date finishTime;
}

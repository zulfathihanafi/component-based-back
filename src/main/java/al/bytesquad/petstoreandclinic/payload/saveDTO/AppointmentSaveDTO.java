package al.bytesquad.petstoreandclinic.payload.saveDTO;


import lombok.Data;

@Data
public class AppointmentSaveDTO {
    private Long petId;
    private Long doctorId;
    private Long clientId;
    private Long serviceId;
    private String startTime;
}

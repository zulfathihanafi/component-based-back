package al.bytesquad.petstoreandclinic.payload.saveDTO;


import lombok.Data;

import java.util.Date;

@Data
public class AppointmentSaveDTO {

    private Long petId;
    private Long doctorId;
    private Long clientId;
    private Long serviceId;
    private Date startTime;
    private Date finishTime;

}

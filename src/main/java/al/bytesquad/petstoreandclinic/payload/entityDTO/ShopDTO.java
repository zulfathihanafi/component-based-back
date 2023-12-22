package al.bytesquad.petstoreandclinic.payload.entityDTO;

import java.time.LocalTime;

import lombok.Data;

@Data
public class ShopDTO {
    private Long id;
    private String name;
    private String address;
    private String city;
    private String country;
    private String about;
    private String phone;
    private Long managerId;
    private LocalTime startWorkingTime;
    private LocalTime endWorkingTime;
}

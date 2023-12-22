package al.bytesquad.petstoreandclinic.payload.saveDTO;

import lombok.Data;

import java.time.LocalTime;

import org.springframework.lang.Nullable;

@Data
public class ShopSaveDTO {
    private String name;
    private String address;
    private String city;
    private String country;
    private String about;
    private String phone;
    private LocalTime startWorkingTime;
    private LocalTime endWorkingTime;
    @Nullable
    private Long managerId;
}

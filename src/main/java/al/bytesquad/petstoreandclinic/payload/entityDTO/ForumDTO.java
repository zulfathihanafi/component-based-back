package al.bytesquad.petstoreandclinic.payload.entityDTO;

import lombok.Data;

@Data
public class ForumDTO {
    private String title;
    private String description;
    private String post;
    private Long userId;
}

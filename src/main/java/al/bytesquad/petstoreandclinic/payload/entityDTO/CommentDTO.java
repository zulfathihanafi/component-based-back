package al.bytesquad.petstoreandclinic.payload.entityDTO;

import lombok.Data;

@Data
public class CommentDTO {
    private Long user_id;
    private String comment;
    private Long forum_id;   
}

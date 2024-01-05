package al.bytesquad.petstoreandclinic.payload.entityDTO;

import lombok.Data;

@Data
public class FBackDTO {
    // private Long shopId;
    private Long userId;
    private String month;
    private String title;
    private String message;
    // private String sentiment;
    private String titleSentiment;  // Add this line
    private String messageSentiment;  // Add this line

}

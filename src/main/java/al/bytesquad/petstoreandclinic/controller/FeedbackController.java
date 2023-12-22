package al.bytesquad.petstoreandclinic.controller;

import al.bytesquad.petstoreandclinic.payload.entityDTO.FeedbackDTO;
import al.bytesquad.petstoreandclinic.service.FeedbackService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//sentiment service
import al.bytesquad.petstoreandclinic.service.SentimentService;


import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final SentimentService sentimentService;

    public FeedbackController(FeedbackService feedbackService, SentimentService sentimentService) {
        this.feedbackService = feedbackService;
        this.sentimentService = sentimentService;
    }

    @PostMapping("/create")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<FeedbackDTO> leaveFeedback(@Valid @RequestBody String feedbackSaveDTO, Principal principal) throws JsonProcessingException {
        return new ResponseEntity<>(feedbackService.create(feedbackSaveDTO, principal), HttpStatus.CREATED);
    }

    @GetMapping
    @CrossOrigin(origins = "http://localhost:3000")
    public List<FeedbackDTO> getAll(@RequestParam(required = false) String keyword, Principal principal) {
        return feedbackService.getAll(keyword, principal);
    }

    /*@GetMapping("/{id}")
    public ResponseEntity<FeedbackDTO> getById(@PathVariable(name = "id") long id) {
        return new ResponseEntity<>(feedbackService.getById(id), HttpStatus.OK);
    }*/

    @GetMapping("/remove/{id}")
    @CrossOrigin(origins = "http://localhost:3000")
    public String delete(@PathVariable(name = "id") long id) {
        return feedbackService.delete(id);
    }

    @PostMapping("/sentiment")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<String> analyzeSentiment(@RequestBody String text) {

        // try {
        //     String sentiment = sentimentService.analyzeSentiment(text);
        //     return ResponseEntity.ok(sentiment);
        // } catch (Exception e) {
        //     // Log the exception for debugging
        //     e.printStackTrace();
        //     return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during sentiment analysis");
        // } 

        String sentiment = sentimentService.analyzeSentiment(text);
        return ResponseEntity.ok(sentiment);


        // Perform sentiment analysis logic here...
        // For simplicity, let's assume a basic positive/negative check.
        // if (text.toLowerCase().contains("positive")) {
        //     return ResponseEntity.ok("Positive");
        // } else {
        //     return ResponseEntity.ok("Negative");
        // }
    }
}

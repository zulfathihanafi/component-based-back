package al.bytesquad.petstoreandclinic.controller;

//service
import al.bytesquad.petstoreandclinic.service.FeedbackService;
// import al.bytesquad.petstoreandclinic.service.exception.SentimentService;
import al.bytesquad.petstoreandclinic.service.UserService;

//entity
import al.bytesquad.petstoreandclinic.entity.FeedbackEntity;

//other imports
import org.springframework.beans.factory.annotation.Autowired;
import al.bytesquad.petstoreandclinic.service.exception.ResourceNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

// @RestController
// @RequestMapping("/nlp")

// public class SentimentController {
//     private final SentimentService sentimentService;

//     @Autowired
//     public SentimentController(SentimentService sentimentService) {
//         this.sentimentService = sentimentService;
//     }

//     @PostMapping("/sentiment")
//     @CrossOrigin(origins = "http://localhost:3000")
//     public String getSentiment(@RequestBody String text)
//     {
//         String res = sentimentService.getSentiment(text);
//         return res;
//     }
// }

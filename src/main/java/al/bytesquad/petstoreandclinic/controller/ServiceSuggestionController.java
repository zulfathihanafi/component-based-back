package al.bytesquad.petstoreandclinic.controller;

import al.bytesquad.petstoreandclinic.payload.entityDTO.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;

import al.bytesquad.petstoreandclinic.service.ServiceSuggestionService;
import al.bytesquad.petstoreandclinic.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/service-suggestion")
public class ServiceSuggestionController {

    private final ServiceSuggestionService serviceSuggestionService;

    @Autowired
    public ServiceSuggestionController(ServiceSuggestionService serviceSuggestionService) {
        this.serviceSuggestionService = serviceSuggestionService;
    }

    @PostMapping("/suggestion")
    @CrossOrigin(origins = "http://localhost:3000")
    public String suggestService(@RequestBody String text) {
        long value = Long.parseLong(text);
        String suggestedService = serviceSuggestionService.getServiceSuggestion(value);
        return suggestedService;
    }

}

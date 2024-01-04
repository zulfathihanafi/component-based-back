package al.bytesquad.petstoreandclinic.controller;


import al.bytesquad.petstoreandclinic.payload.entityDTO.UserDTO;
import al.bytesquad.petstoreandclinic.service.NLPService;
import al.bytesquad.petstoreandclinic.service.ReceptionistService;
import al.bytesquad.petstoreandclinic.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/nlp")
public class NLPController {

    private final NLPService nlpService;

    @Autowired
    public NLPController(NLPService nlpService) {
        this.nlpService = nlpService;
    }

    @PostMapping("/analyse")
    @CrossOrigin(origins = "http://localhost:3000")
    public String analyse(@RequestBody String text) {
        String res = nlpService.analyse(text);
        return res;
    }
    
    
}

package al.bytesquad.petstoreandclinic.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Collection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import al.bytesquad.petstoreandclinic.entity.AdoptablePet;
import al.bytesquad.petstoreandclinic.service.AdoptablePetService;

@RestController
@RequestMapping("/adoptablePet")
@CrossOrigin(origins = "http://localhost:3000")
public class AdoptablePetController {
    private final AdoptablePetService adoptablePetService;

    @Autowired
    public AdoptablePetController(AdoptablePetService adoptablePetService) {
        this.adoptablePetService = adoptablePetService;
    }

    @GetMapping
    @CrossOrigin(origins = "http://localhost:3000")
    public List<AdoptablePet> get(@RequestParam(required = false) String keyword, Principal principal) {
        return adoptablePetService.getList();
    }

    @PostMapping("/create")
    public ResponseEntity<AdoptablePet> createPost(@RequestBody String post)throws JsonProcessingException{
         Collection<? extends GrantedAuthority> roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities();

            // Check if the user has the role ROLE_MANAGER
            if (roles.stream().anyMatch(role -> "ROLE_MANAGER".equals(role.getAuthority()))) {
                AdoptablePet createdPost = adoptablePetService.create(post);
                return new ResponseEntity<>(createdPost, HttpStatus.CREATED);
            } else {
                // If the user doesn't have the required role, return forbidden access
                // return new ResponseEntity<>("Access denied. Insufficient privileges.", HttpStatus.FORBIDDEN);
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);

            }
        // AdoptablePet createdPost = adoptablePetService.create(post);
        // return new ResponseEntity<>(createdPost, HttpStatus.CREATED);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<AdoptablePet> updateTutorial(@PathVariable("id") long id, @RequestBody String adoptablePetString)throws JsonProcessingException{
        Collection<? extends GrantedAuthority> roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        
        if (roles.stream().anyMatch(role -> "ROLE_MANAGER".equals(role.getAuthority()))) {
            return new ResponseEntity<>(adoptablePetService.update(adoptablePetString,id),HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        
    }

    

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable(name = "id") long id){
        
        try{
            adoptablePetService.delete(id);
            return new ResponseEntity<>("Deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            // If an error occurs during deletion, return an error response with HTTP status 500
            return new ResponseEntity<>("Error deleting the record: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

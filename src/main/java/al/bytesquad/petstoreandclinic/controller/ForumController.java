package al.bytesquad.petstoreandclinic.controller;

import al.bytesquad.petstoreandclinic.entity.Comment;
import al.bytesquad.petstoreandclinic.entity.Forum;
import al.bytesquad.petstoreandclinic.entity.User;
import al.bytesquad.petstoreandclinic.payload.entityDTO.AdminDTO;
import al.bytesquad.petstoreandclinic.payload.entityDTO.ForumDTO;
import al.bytesquad.petstoreandclinic.payload.entityDTO.PetDTO;
import al.bytesquad.petstoreandclinic.payload.entityDTO.ShopDTO;
import al.bytesquad.petstoreandclinic.service.AdminService;
import al.bytesquad.petstoreandclinic.service.ForumService;
import al.bytesquad.petstoreandclinic.service.ShopService;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.security.Principal;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/forum")
@CrossOrigin(origins = "http://localhost:3000")
public class ForumController {
    private final ForumService forumService;

    @Autowired
    public ForumController(ForumService forumService) {
        this.forumService = forumService;
    }

    @GetMapping
    @CrossOrigin(origins = "http://localhost:3000")
    public List<Forum> get(@RequestParam(required = false) String keyword, Principal principal) {
        return forumService.getList();
    }

    //create pet
    @PostMapping("/create")
    public ResponseEntity<Forum> createPost(@RequestBody String post) throws JsonProcessingException{
        Forum createdPost = forumService.create(post);
        return new ResponseEntity<>(createdPost, HttpStatus.CREATED);
    }

    // update
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateTutorial(@PathVariable("id") long id, @RequestBody String forumString) throws JsonProcessingException{
        Forum existingForum = forumService.getForumById(id);

        // Check if the comment exists
        if (existingForum == null) {
            return new ResponseEntity<>("Comment not found.", HttpStatus.NOT_FOUND);
        }

        // Check if the user are correct : Manager | Own Comment
        Collection<? extends GrantedAuthority> roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        
        if (!(existingForum.getUser().getEmail().equals(currentUsername) || roles.stream().anyMatch(role -> "ROLE_MANAGER".equals(role.getAuthority())))) {
            return new ResponseEntity<>("Access denied. Insufficient privileges.", HttpStatus.FORBIDDEN);
        }
        
        try {
            // If deletion is successful, return a success response with HTTP status 200
            return new ResponseEntity<>(forumService.update(forumString, id), HttpStatus.OK);
        } catch (Exception e) {
            // If an error occurs during deletion, return an error response with HTTP status 500
            return new ResponseEntity<>("Error deleting the record: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
      
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable(name = "id") long id) {
        Forum existingForum = forumService.getForumById(id);

        // Check if the comment exists
        if (existingForum == null) {
            return new ResponseEntity<>("Comment not found.", HttpStatus.NOT_FOUND);
        }

        // Check if the user are correct : Manager | Own Comment
        Collection<? extends GrantedAuthority> roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        
        if (!(existingForum.getUser().getEmail().equals(currentUsername) || roles.stream().anyMatch(role -> "ROLE_MANAGER".equals(role.getAuthority())))) {
            return new ResponseEntity<>("Access denied. Insufficient privileges.", HttpStatus.FORBIDDEN);
        }

        try {
            forumService.delete(id);
            // If deletion is successful, return a success response with HTTP status 200
            return new ResponseEntity<>("Deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            // If an error occurs during deletion, return an error response with HTTP status 500
            return new ResponseEntity<>("Error deleting the record: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

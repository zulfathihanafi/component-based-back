package al.bytesquad.petstoreandclinic.controller;

import al.bytesquad.petstoreandclinic.entity.Comment;
import al.bytesquad.petstoreandclinic.entity.Forum;
import al.bytesquad.petstoreandclinic.entity.User;
import al.bytesquad.petstoreandclinic.entity.FBack;
import al.bytesquad.petstoreandclinic.payload.entityDTO.AdminDTO;
import al.bytesquad.petstoreandclinic.payload.entityDTO.ForumDTO;
import al.bytesquad.petstoreandclinic.payload.entityDTO.FBackDTO;
import al.bytesquad.petstoreandclinic.payload.entityDTO.PetDTO;
import al.bytesquad.petstoreandclinic.payload.entityDTO.ShopDTO;
import al.bytesquad.petstoreandclinic.service.AdminService;
import al.bytesquad.petstoreandclinic.service.ForumService;
import al.bytesquad.petstoreandclinic.service.FBackService;
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
@RequestMapping("/fback")
@CrossOrigin(origins = "http://localhost:3000")
public class FBackController {
    private final FBackService fbackService;

    @Autowired
    public FBackController(FBackService fbackService) {
        this.fbackService = fbackService;
    }

    @GetMapping
    @CrossOrigin(origins = "http://localhost:3000")
    public List<FBack> get(@RequestParam(required = false) String keyword, Principal principal) {
        return fbackService.getList();
    }

    @GetMapping("/by-month/{month}")
    public ResponseEntity<List<FBack>> getByMonth(@PathVariable String month) {
    List<FBack> fback = fbackService.getByMonth(month);
    return new ResponseEntity<>(fback, HttpStatus.OK);
    }

//     @GetMapping("/by-month-and-shop")
//     public ResponseEntity<List<FBack>> getByMonthAndShop(
//     @RequestParam String month,
//     @RequestParam Long shopId
// ) {
//     List<FBack> fback = fbackService.getByMonthAndShop(month, shopId);
//     return new ResponseEntity<>(fback, HttpStatus.OK);
// }

    //create fback
    @PostMapping("/create")
    public ResponseEntity<FBack> createPost(@RequestBody String post) throws JsonProcessingException {
        FBack createdPost = fbackService.create(post);
        return new ResponseEntity<>(createdPost, HttpStatus.CREATED);
    }

    // update
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateTutorial(@PathVariable("id") long id, @RequestBody String fbackString) throws JsonProcessingException{
        FBack existingFBack = fbackService.getFBackById(id);

        // Check if the comment exists
        if (existingFBack == null) {
            return new ResponseEntity<>("Comment not found.", HttpStatus.NOT_FOUND);
        }

        // Check if the user are correct : Manager | Own Comment
        Collection<? extends GrantedAuthority> roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        
        if (!(existingFBack.getUser().getEmail().equals(currentUsername) || roles.stream().anyMatch(role -> "ROLE_MANAGER".equals(role.getAuthority())))) {
            return new ResponseEntity<>("Access denied. Insufficient privileges.", HttpStatus.FORBIDDEN);
        }
        
        try {
            // If deletion is successful, return a success response with HTTP status 200
            return new ResponseEntity<>(fbackService.update(fbackString, id), HttpStatus.OK);
        } catch (Exception e) {
            // If an error occurs during deletion, return an error response with HTTP status 500
            return new ResponseEntity<>("Error deleting the record: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
      
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable(name = "id") long id) {
        FBack existingFback = fbackService.getFBackById(id);

        // Check if the comment exists
        if (existingFback == null) {
            return new ResponseEntity<>("Comment not found.", HttpStatus.NOT_FOUND);
        }

        // Check if the user are correct : Manager | Own Comment
        Collection<? extends GrantedAuthority> roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        
        if (!(existingFback.getUser().getEmail().equals(currentUsername) || roles.stream().anyMatch(role -> "ROLE_MANAGER".equals(role.getAuthority())))) {
            return new ResponseEntity<>("Access denied. Insufficient privileges.", HttpStatus.FORBIDDEN);
        }

        try {
            fbackService.delete(id);
            // If deletion is successful, return a success response with HTTP status 200
            return new ResponseEntity<>("Deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            // If an error occurs during deletion, return an error response with HTTP status 500
            return new ResponseEntity<>("Error deleting the record: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

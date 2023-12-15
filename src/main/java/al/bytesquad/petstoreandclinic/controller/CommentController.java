package al.bytesquad.petstoreandclinic.controller;

import al.bytesquad.petstoreandclinic.entity.Admin;
import al.bytesquad.petstoreandclinic.entity.Comment;
import al.bytesquad.petstoreandclinic.entity.Forum;
import al.bytesquad.petstoreandclinic.entity.User;
import al.bytesquad.petstoreandclinic.payload.entityDTO.AdminDTO;
import al.bytesquad.petstoreandclinic.payload.entityDTO.ForumDTO;
import al.bytesquad.petstoreandclinic.payload.entityDTO.PetDTO;
import al.bytesquad.petstoreandclinic.payload.entityDTO.ShopDTO;
import al.bytesquad.petstoreandclinic.service.AdminService;
import al.bytesquad.petstoreandclinic.service.CommentService;
import al.bytesquad.petstoreandclinic.service.ForumService;
import al.bytesquad.petstoreandclinic.service.NLPService;
import al.bytesquad.petstoreandclinic.service.ShopService;
import al.bytesquad.petstoreandclinic.service.exception.ResourceNotFoundException;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.security.Principal;
import java.util.List;
import java.util.Objects;


@RestController
@RequestMapping("/comment")
@CrossOrigin(origins = "http://localhost:3000")
public class CommentController {
    private final CommentService commentService;
    private final ForumService forumService;

    @Autowired
    public CommentController(CommentService commentService, ForumService forumService) {
        this.commentService = commentService;
        this.forumService = forumService;
    }

    @GetMapping
    @CrossOrigin(origins = "http://localhost:3000")
    public List<Comment> get(@RequestParam(required = false) String keyword, Principal principal) {
        return commentService.getAllComments();
    }

    @GetMapping("/forum/{forum_id}/comments")
    public ResponseEntity<List<Comment>> getAllCommentsByTutorialId(@PathVariable(value = "forum_id") Long forumID) {
    if (Objects.isNull(forumService.getForumById(forumID))) {
      throw new ResourceNotFoundException("Forum", "id", forumID);
    }

    List<Comment> comments = commentService.getCommentByForum(forumID);
    return new ResponseEntity<>(comments, HttpStatus.OK);
  }
    

    // create pet
    @PostMapping("/create")
    public ResponseEntity<?> createPost(@RequestBody String post) throws JsonProcessingException{
        try{
            Comment createdPost = commentService.create(post);
            return new ResponseEntity<>(createdPost, HttpStatus.CREATED);
        }catch(CommentService.OffensiveComments ex)
            {
                // Handle offensive comments exception here
                String errorMessage = "Offensive comment detected: " + ex.getMessage();
                return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);

            }
        }
    // update
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateTutorial(@PathVariable("id") long id, @RequestBody String commentString)
            throws JsonProcessingException {

        try {
            Comment updatedComment = commentService.update(commentString, id);
            return new ResponseEntity<>(updatedComment, HttpStatus.OK);
        } catch (CommentService.OffensiveComments ex) {
            // Handle offensive comments exception here
            String errorMessage = "Problematic comment detected: " + ex.getMessage();
            return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable(name = "id") long id) {
        try {
            commentService.delete(id);
            // If deletion is successful, return a success response with HTTP status 200
            return new ResponseEntity<>("Deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            // If an error occurs during deletion, return an error response with HTTP status
            // 500
            return new ResponseEntity<>("Error deleting the record: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

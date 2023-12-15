package al.bytesquad.petstoreandclinic.service;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import al.bytesquad.petstoreandclinic.entity.Comment;
import al.bytesquad.petstoreandclinic.entity.Forum;
import al.bytesquad.petstoreandclinic.entity.User;
import al.bytesquad.petstoreandclinic.payload.entityDTO.CommentDTO;
import al.bytesquad.petstoreandclinic.payload.entityDTO.ForumDTO;
import al.bytesquad.petstoreandclinic.repository.CommentRepository;
import al.bytesquad.petstoreandclinic.repository.ForumRepository;
import al.bytesquad.petstoreandclinic.repository.UserRepository;
import al.bytesquad.petstoreandclinic.service.exception.ResourceNotFoundException;

@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final ForumRepository forumRepository;
    private final ModelMapper modelMapper;
    private final NLPService nlpService;
    public CommentService(ModelMapper modelMapper, UserRepository userRepository, ForumRepository forumRepository, ObjectMapper objectMapper, NLPService nlpService){
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.forumRepository = forumRepository;
        this.objectMapper = objectMapper;
        this.nlpService = nlpService;
    }

    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }

    public Comment getCommentById(Long id) {
        return commentRepository.findById(id).orElse(null);
    }

    public List<Comment> getCommentByForum(Long id){
        return commentRepository.findByForumId(id);
    }
    public Comment create(@RequestBody String forumString) throws JsonProcessingException, OffensiveComments{
        
        CommentDTO commentDTO = objectMapper.readValue(forumString, CommentDTO.class);

        String category = nlpService.analyse(commentDTO.getComment());
        if(!nlpService.analyse(commentDTO.getComment()).equals("None")){
            throw new OffensiveComments("This comment contains "+category+" message");
        }

        Comment comment = modelMapper.map(commentDTO, Comment.class);
        System.out.println("DTO : "+commentDTO);
        
        // User user = userRepository.findUserById(commentDTO.getUser_id()).orElseThrow(()-> new ResourceNotFoundException(forumString, forumString, 0));
        // System.out.println("User"+ user);
        Forum forum = forumRepository.findById(commentDTO.getForum_id()).orElseThrow(()-> new ResourceNotFoundException(forumString, forumString, 0));
        System.out.println("Forum : "+forum);
        
        // comment.setUser(user);
        comment.setForum(forum);

        return commentRepository.save(comment);
    }
    
    public Comment update(@RequestBody String commentString, long id) throws JsonProcessingException, OffensiveComments{
        
        CommentDTO commentDTO = objectMapper.readValue(commentString, CommentDTO.class);
        String category = nlpService.analyse(commentDTO.getComment());
        if(!nlpService.analyse(commentDTO.getComment()).equals("None")){
            throw new OffensiveComments("This comment contains "+category+" message");
        }

        Comment comment = commentRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException(commentString, commentString, 0));
        comment.setComment(commentDTO.getComment());
        
        return commentRepository.save(comment);
    }


    public void delete(long id){
        commentRepository.deleteById(id);
    }

    public static class OffensiveComments extends Exception {
        public OffensiveComments(String message) {
            super(message);
        }
    }
}


package al.bytesquad.petstoreandclinic.service;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import al.bytesquad.petstoreandclinic.entity.Forum;
import al.bytesquad.petstoreandclinic.entity.Pet;
import al.bytesquad.petstoreandclinic.entity.User;
import al.bytesquad.petstoreandclinic.payload.entityDTO.ForumDTO;
import al.bytesquad.petstoreandclinic.payload.entityDTO.PetDTO;
import al.bytesquad.petstoreandclinic.payload.saveDTO.PetSaveDTO;
import al.bytesquad.petstoreandclinic.repository.BillRepository;
import al.bytesquad.petstoreandclinic.repository.ForumRepository;
import al.bytesquad.petstoreandclinic.repository.UserRepository;
import al.bytesquad.petstoreandclinic.service.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import al.bytesquad.petstoreandclinic.service.exception.ResourceNotFoundException;

@Service
public class ForumService {
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final ForumRepository forumRepository;
    private final ObjectMapper objectMapper;

     @Autowired
    public ForumService(ModelMapper modelMapper, UserRepository userRepository, ForumRepository forumRepository, ObjectMapper objectMapper){
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.forumRepository = forumRepository;
        this.objectMapper = objectMapper;
    }

    public List<Forum> getList(){
        return forumRepository.findAll();
    }

    // get by forum id
    public Forum getForumById(Long id){
        return forumRepository.findById(id).orElse(null);
    }
    
    // get by user id
    // public List<Forum> getListById(Long id){
    //     return forumRepository.getListByUser(id);
    // }

    public Forum create(@RequestBody String forumString) throws JsonProcessingException{
        ForumDTO forumDTO = objectMapper.readValue(forumString, ForumDTO.class);
        Forum forum = modelMapper.map(forumDTO, Forum.class);
        forum.setUser(userRepository.findUserById(forumDTO.getUserId()).orElseThrow(()-> new ResourceNotFoundException(forumString, forumString, 0)));
        System.out.println("Within service"+ forum);
        return forumRepository.save(forum);
    }

    public Forum update(@RequestBody String forumString, long id) throws JsonProcessingException{
        
        
        ForumDTO forumDTO = objectMapper.readValue(forumString, ForumDTO.class);
        Forum forum = forumRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException(forumString, forumString, 0));
        
        forum.setDescription(forumDTO.getDescription());
        forum.setTitle(forumDTO.getTitle());
        forum.setPost(forumDTO.getPost());
      
      return forumRepository.save(forum);
    }

    public void delete(long id){
        forumRepository.deleteById(id);
    }
}

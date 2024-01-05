package al.bytesquad.petstoreandclinic.service;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import al.bytesquad.petstoreandclinic.entity.FBack;
import al.bytesquad.petstoreandclinic.entity.Pet;
import al.bytesquad.petstoreandclinic.entity.User;
import al.bytesquad.petstoreandclinic.payload.entityDTO.FBackDTO;
import al.bytesquad.petstoreandclinic.payload.entityDTO.PetDTO;
import al.bytesquad.petstoreandclinic.payload.saveDTO.PetSaveDTO;
import al.bytesquad.petstoreandclinic.repository.BillRepository;
import al.bytesquad.petstoreandclinic.repository.FBackRepository;
import al.bytesquad.petstoreandclinic.repository.FBackRepository;
import al.bytesquad.petstoreandclinic.repository.UserRepository;
import al.bytesquad.petstoreandclinic.service.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import al.bytesquad.petstoreandclinic.service.exception.ResourceNotFoundException;

@Service
public class FBackService {
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final FBackRepository fbackRepository;
    private final ObjectMapper objectMapper;

     @Autowired
    public FBackService(ModelMapper modelMapper, UserRepository userRepository, FBackRepository fbackRepository, ObjectMapper objectMapper){
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.fbackRepository = fbackRepository;
        this.objectMapper = objectMapper;
    }

    public List<FBack> getList(){
        return fbackRepository.findAll();
    }

    // get by fback id
    public FBack getFBackById(Long id){
        return fbackRepository.findById(id).orElse(null);
    }    

    // get fback by month
    public List<FBack> getByMonth(String month) {
        return fbackRepository.findByMonth(month);
    }

    // // get fback by month and shop
    // public List<FBack> getByMonthAndShop(String month, Long shopId) {
    //     return fbackRepository.findByMonthAndUser_Shop_Id(month, shopId);
    // }
    

    
    // get by user id
    // public List<FBack> getListById(Long id){
    //     return forumRepository.getListByUser(id);
    // }

    public FBack create(@RequestBody String fbackString) throws JsonProcessingException {
        FBackDTO fbackDTO = objectMapper.readValue(fbackString, FBackDTO.class);
        FBack fback = modelMapper.map(fbackDTO, FBack.class);
        fback.setUser(userRepository.findUserById(fbackDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(fbackString, fbackString, 0)));
        fback.setMonth(fbackDTO.getMonth()); // Set the month
        return fbackRepository.save(fback);
    }

    public FBack update(@RequestBody String fbackString, long id) throws JsonProcessingException {
        FBackDTO fbackDTO = objectMapper.readValue(fbackString, FBackDTO.class);
        FBack fback = fbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(fbackString, fbackString, 0));
        

                fback.setTitle(fbackDTO.getTitle());
                fback.setMessage(fbackDTO.getMessage());
                fback.setMonth(fbackDTO.getMonth()); // Update the month
            
                return fbackRepository.save(fback);
    }

    public void delete(long id){
        fbackRepository.deleteById(id);
    }
}

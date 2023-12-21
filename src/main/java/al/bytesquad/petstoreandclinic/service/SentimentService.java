//Sentiment Service

package al.bytesquad.petstoreandclinic.service;

import org.modelmapper.ModelMapper;
import org.modelmapper.ModelMapper; 
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.HashMap;
import java.util.Map;


//entity
import al.bytesquad.petstoreandclinic.entity.User;
import al.bytesquad.petstoreandclinic.payload.entityDTO.UserDTO;

//repo
import al.bytesquad.petstoreandclinic.repository.UserRepository;


//sus
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


//import repository and entity
import al.bytesquad.petstoreandclinic.entity.User;
import al.bytesquad.petstoreandclinic.repository.UserRepository;
import al.bytesquad.petstoreandclinic.service.exception.ResourceNotFoundException;


@Service
public class SentimentService {

    @Autowired
    public SentimentService() {
    }

    // public String getSentiment(String text) {
    //     String res = sentiment(text);
    //     return res;
    // }

}



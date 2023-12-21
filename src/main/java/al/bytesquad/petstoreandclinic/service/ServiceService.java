package al.bytesquad.petstoreandclinic.service;

import al.bytesquad.petstoreandclinic.entity.PetServices;
import al.bytesquad.petstoreandclinic.payload.entityDTO.ServiceDTO;
import al.bytesquad.petstoreandclinic.payload.saveDTO.ServiceSaveDTO;
import al.bytesquad.petstoreandclinic.repository.ServiceRepository;
import al.bytesquad.petstoreandclinic.service.exception.ResourceNotFoundException;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServiceService {

    private final ServiceRepository serviceRepository;
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;

    @Autowired
    public ServiceService(ServiceRepository serviceRepository, ModelMapper modelMapper, ObjectMapper objectMapper) {
        this.serviceRepository = serviceRepository;
        this.modelMapper = modelMapper;
        this.objectMapper = objectMapper;

        modelMapper.addMappings(new PropertyMap<PetServices, ServiceDTO>() {
            @Override
            protected void configure() {
                map().setId(source.getId());
            }
        });
    }

    public ServiceDTO create(String jsonString) throws JsonProcessingException {
        ServiceSaveDTO serviceSaveDTO = objectMapper.readValue(jsonString, ServiceSaveDTO.class);

        PetServices petService = modelMapper.map(serviceSaveDTO, PetServices.class);
        return modelMapper.map(serviceRepository.save(petService), ServiceDTO.class);
    }

    public List<ServiceDTO> getAll(String keyword, Principal principal) {
        if (keyword == null)
            return serviceRepository.findAll().stream()
                    .map(petService -> modelMapper.map(petService, ServiceDTO.class))
                    .collect(Collectors.toList());

        List<String> keyValues = List.of(keyword.split(","));
        HashMap<String, String> pairs = new HashMap<>();
        for (String s : keyValues) {
            String[] strings = s.split(":");
            pairs.put(strings[0], strings[1]);
        }

        List<PetServices> petServices = serviceRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            for (String key : pairs.keySet()) {
                Path<Object> fieldPath = root.get(key);
                predicates.add(criteriaBuilder.equal(fieldPath, pairs.get(key)));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });

        return petServices.stream().map(petService -> modelMapper.map(petService, ServiceDTO.class))
                .collect(Collectors.toList());
    }

    public ServiceDTO update(String jsonString, long id) throws JsonProcessingException {
        ServiceSaveDTO serviceSaveDTO = objectMapper.readValue(jsonString, ServiceSaveDTO.class);
       PetServices petService = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service", "id", id));
        petService.setTitle(serviceSaveDTO.getTitle());
        petService.setDescription(serviceSaveDTO.getDescription());
      
        return modelMapper.map(serviceRepository.save(petService), ServiceDTO.class);
    }

    public void delete(long id){
        serviceRepository.deleteById(id);
    }
}

package al.bytesquad.petstoreandclinic.service;

import al.bytesquad.petstoreandclinic.entity.Appointment;
import al.bytesquad.petstoreandclinic.entity.PetServices;
import al.bytesquad.petstoreandclinic.payload.entityDTO.ServiceDTO;
import al.bytesquad.petstoreandclinic.payload.saveDTO.ServiceSaveDTO;
import al.bytesquad.petstoreandclinic.repository.AppointmentRepository;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServiceService {

    private final ServiceRepository serviceRepository;
    private final AppointmentRepository appointmentRepository;
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;

    @Autowired
    public ServiceService(ServiceRepository serviceRepository, AppointmentRepository appointmentRepository, ModelMapper modelMapper, ObjectMapper objectMapper) {
        this.serviceRepository = serviceRepository;
        this.appointmentRepository = appointmentRepository;
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

    public String getServiceSuggestion(Long petId) {
        List<Appointment> appointments = appointmentRepository.findByPetId(petId);

        // Find the latest appointment
        Appointment latestAppointment = appointments.stream()
                .max(Comparator.comparing(Appointment::getFinishTime)) 
                .orElse(null);

        if (latestAppointment != null) {
            PetServices lastService = latestAppointment.getPetServices();
            if (lastService != null) {
                Long lastServiceId = lastService.getId();

                // Suggest the next service based on the latest appointment service record
                switch (lastServiceId.intValue()) {
                    case 1:
                        return "Service Suggestion: Vaccination";
                    case 2:
                        return "Service Suggestion: Deworming";
                    case 3:
                        return "Service Suggestion: Neutering/Spaying";
                    default:
                        return "Service Suggestion: General Health Checkup";
                }
            }
        }
        return "Service Suggestion: General Health Checkup"; // Default suggestion
    }
}

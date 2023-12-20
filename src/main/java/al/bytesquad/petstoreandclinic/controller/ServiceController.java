package al.bytesquad.petstoreandclinic.controller;

import al.bytesquad.petstoreandclinic.payload.entityDTO.ServiceDTO;
import al.bytesquad.petstoreandclinic.payload.saveDTO.ServiceSaveDTO;
import al.bytesquad.petstoreandclinic.repository.ServiceRepository;
import al.bytesquad.petstoreandclinic.service.ServiceService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/services")
public class ServiceController {

    private final ServiceService serviceService;
    private final ServiceRepository serviceRepository;

    @Autowired
    public ServiceController(ServiceService serviceService,
                             ServiceRepository serviceRepository) {
        this.serviceService = serviceService;
        this.serviceRepository = serviceRepository;
    }

    // Get all services
    @GetMapping
    @CrossOrigin(origins = "http://localhost:3000")
    public List<ServiceDTO> getAll(@RequestParam(required = false) String keyword, Principal principal) {
        return serviceService.getAll(keyword, principal);
    }


    // Create service
    @PostMapping("/create")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<ServiceDTO> addNew(@Valid @RequestBody String serviceSaveDTO) throws JsonProcessingException {
        ServiceDTO createdService = serviceService.create(serviceSaveDTO);
        return new ResponseEntity<>(createdService, HttpStatus.CREATED);
    }
}

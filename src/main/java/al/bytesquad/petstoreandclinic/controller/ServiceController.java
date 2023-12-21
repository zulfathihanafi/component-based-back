package al.bytesquad.petstoreandclinic.controller;

import al.bytesquad.petstoreandclinic.payload.entityDTO.ServiceDTO;
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

    //update service
    @PutMapping("/update/{id}")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<ServiceDTO> update(@Valid @RequestBody String serviceSaveDTO, @PathVariable(name = "id") long id) throws JsonProcessingException {
        return new ResponseEntity<>(serviceService.update(serviceSaveDTO, id), HttpStatus.OK);
    }

    //delete service
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable(name = "id") long id) {
        try {
            serviceService.delete(id);
            // If deletion is successful, return a success response with HTTP status 200
            return new ResponseEntity<>("Deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            // If an error occurs during deletion, return an error response with HTTP status 500
            return new ResponseEntity<>("Error deleting the record: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
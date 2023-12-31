package al.bytesquad.petstoreandclinic.controller;

import al.bytesquad.petstoreandclinic.payload.entityDTO.ServiceDTO;
import al.bytesquad.petstoreandclinic.repository.ServiceRepository;
import al.bytesquad.petstoreandclinic.service.ServiceService;
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
    public ResponseEntity<?> getAll(@RequestParam(required = false) String keyword, Principal principal) {
        Collection<? extends GrantedAuthority> roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
    
        // Check user role
        boolean isPrivilegedUser = roles.stream().anyMatch(role -> 
            "ROLE_CLIENT".equals(role.getAuthority()) || 
            "ROLE_ADMIN".equals(role.getAuthority()) || 
            "ROLE_MANAGER".equals(role.getAuthority()) ||
            "ROLE_DOCTOR".equals(role.getAuthority()) ||
            "ROLE_RECEPTIONIST".equals(role.getAuthority())
        );
    
        // Condition to determine access
        boolean hasAccess = isPrivilegedUser;
    
        if (!hasAccess) {
            return new ResponseEntity<>("Please login!.", HttpStatus.FORBIDDEN);
        }
    
        try {
            List<ServiceDTO> services = serviceService.getAll(keyword, principal);
            return ResponseEntity.ok(services);
        } catch (Exception e) {
            // Handle the exception or return an appropriate response
            String errorMessage = "An error occurred while fetching the services.";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }
    }
    


    // Create service
    @PostMapping("/create")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<?> addNew(@Valid @RequestBody String serviceSaveDTO) throws JsonProcessingException {
        Collection<? extends GrantedAuthority> roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities();

        // Check admin role
        boolean isPrivilegedUser = roles.stream().anyMatch(role -> 
            "ROLE_ADMIN".equals(role.getAuthority())
        );

        // Condition to determine access
        boolean hasAccess = isPrivilegedUser;

        if (!hasAccess) {
            return new ResponseEntity<>("Access denied. Insufficient privileges.", HttpStatus.FORBIDDEN);
        }

        ServiceDTO createdService = serviceService.create(serviceSaveDTO);
        return new ResponseEntity<>(createdService, HttpStatus.CREATED);
    }

    //update service
    @PutMapping("/update/{id}")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<?> update(@Valid @RequestBody String serviceSaveDTO, @PathVariable(name = "id") long id) throws JsonProcessingException {
        Collection<? extends GrantedAuthority> roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities();

        // Check admin role
        boolean isPrivilegedUser = roles.stream().anyMatch(role -> 
            "ROLE_ADMIN".equals(role.getAuthority())
        );

        // Condition to determine access
        boolean hasAccess = isPrivilegedUser;

        if (!hasAccess) {
            return new ResponseEntity<>("Access denied. Insufficient privileges.", HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<>(serviceService.update(serviceSaveDTO, id), HttpStatus.OK);
    }

    //delete service
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable(name = "id") long id) {
        Collection<? extends GrantedAuthority> roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities();

        // Check admin role
        boolean isPrivilegedUser = roles.stream().anyMatch(role -> 
            "ROLE_ADMIN".equals(role.getAuthority())
        );

        // Condition to determine access
        boolean hasAccess = isPrivilegedUser;

        if (!hasAccess) {
            return new ResponseEntity<>("Access denied. Insufficient privileges.", HttpStatus.FORBIDDEN);
        }

        try {
            serviceService.delete(id);
            // If deletion is successful, return a success response with HTTP status 200
            return new ResponseEntity<>("Deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            // If an error occurs during deletion, return an error response with HTTP status 500
            return new ResponseEntity<>("Error deleting the record: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/suggestion")
    @CrossOrigin(origins = "http://localhost:3000")
    public String suggestService(@RequestBody String text) {
        long value = Long.parseLong(text);
        String suggestedService = serviceService.getServiceSuggestion(value);
        Collection<? extends GrantedAuthority> roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities();

        // Check client role
        boolean isPrivilegedUser = roles.stream().anyMatch(role -> 
            "ROLE_CLIENT".equals(role.getAuthority())
        );

        // Condition to determine access
        boolean hasAccess = isPrivilegedUser;

        if (!hasAccess) {
            return "Access denied. Insufficient privileges.";
        }

        return suggestedService;
    }
}
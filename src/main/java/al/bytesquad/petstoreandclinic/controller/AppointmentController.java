package al.bytesquad.petstoreandclinic.controller;

import al.bytesquad.petstoreandclinic.entity.Appointment;
import al.bytesquad.petstoreandclinic.entity.Client;
import al.bytesquad.petstoreandclinic.entity.Doctor;
import al.bytesquad.petstoreandclinic.entity.Pet;
import al.bytesquad.petstoreandclinic.payload.entityDTO.AppointmentDTO;
import al.bytesquad.petstoreandclinic.service.AppointmentService;
import al.bytesquad.petstoreandclinic.service.ClientService;
import al.bytesquad.petstoreandclinic.service.DoctorService;
import al.bytesquad.petstoreandclinic.service.PetService;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final PetService petService;
    private final DoctorService doctorService;
    private final ClientService clientService;

    public AppointmentController(AppointmentService appointmentService, PetService petService,
            DoctorService doctorService, ClientService clientService) {
        this.appointmentService = appointmentService;
        this.petService = petService;
        this.doctorService = doctorService;
        this.clientService = clientService;
    }

    // get all appointments based on petId
    @GetMapping("/pet/{petId}")
    public ResponseEntity<?> getAppointmentsByPetId(@PathVariable Long petId) {
        Pet pet = petService.getPetById(petId);

        // Check if the appointment exists
        if (pet == null) {
            return new ResponseEntity<>("Pet not found.", HttpStatus.NOT_FOUND);
        }

        // Check if the user are correct
        Collection<? extends GrantedAuthority> roles = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities();
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!(pet.getClient().getEmail().equals(currentUsername)
                || roles.stream().anyMatch(role -> "ROLE_ADMIN".equals(role.getAuthority()))
                || roles.stream().anyMatch(role -> "ROLE_MANAGER".equals(role.getAuthority()))
                || roles.stream().anyMatch(role -> "ROLE_RECEPTIONIST".equals(role.getAuthority()))
                || roles.stream().anyMatch(role -> "ROLE_DOCTOR".equals(role.getAuthority())))) {
            return new ResponseEntity<>("Access denied. Insufficient privileges.", HttpStatus.FORBIDDEN);
        }

        // User has the required privilege or is associated with the pet
        List<Appointment> appointments = appointmentService.getAppointmentsByPetId(petId);
        return ResponseEntity.ok(appointments);
    }

    // get all appointments based on doctor Id
    @GetMapping("doctor/{doctorId}")
    public ResponseEntity<?> getAppointmentsByDoctorId(@PathVariable Long doctorId) {
        Doctor doctor = doctorService.getDoctorById(doctorId);

        // Check if the appointment exists
        if (doctor == null) {
            return new ResponseEntity<>("Doctor not found.", HttpStatus.NOT_FOUND);
        }

        // Check if the user are correct
        Collection<? extends GrantedAuthority> roles = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities();

        if (!(roles.stream().anyMatch(role -> "ROLE_ADMIN".equals(role.getAuthority()))
                || roles.stream().anyMatch(role -> "ROLE_MANAGER".equals(role.getAuthority()))
                || roles.stream().anyMatch(role -> "ROLE_RECEPTIONIST".equals(role.getAuthority()))
                || roles.stream().anyMatch(role -> "ROLE_DOCTOR".equals(role.getAuthority())))) {
            return new ResponseEntity<>("Access denied. Insufficient privileges.", HttpStatus.FORBIDDEN);
        }

        // User has the required privilege
        List<Appointment> appointments = appointmentService.getAppointmentsByDoctorId(doctorId);
        return ResponseEntity.ok(appointments);
    }

    // get all appointments based on client Id
    @GetMapping("client/{clientId}")
    public ResponseEntity<?> getAppointmentsByClientId(@PathVariable Long clientId) {
        Client client = clientService.getClientById(clientId);

        // Check if the appointment exists
        if (client == null) {
            return new ResponseEntity<>("Client not found.", HttpStatus.NOT_FOUND);
        }

        // Check if the user are correct
        Collection<? extends GrantedAuthority> roles = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities();
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!(client.getEmail().equals(currentUsername)
                || roles.stream().anyMatch(role -> "ROLE_ADMIN".equals(role.getAuthority()))
                || roles.stream().anyMatch(role -> "ROLE_MANAGER".equals(role.getAuthority()))
                || roles.stream().anyMatch(role -> "ROLE_RECEPTIONIST".equals(role.getAuthority()))
                || roles.stream().anyMatch(role -> "ROLE_DOCTOR".equals(role.getAuthority())))) {
            return new ResponseEntity<>("Access denied. Insufficient privileges.", HttpStatus.FORBIDDEN);
        }

        // User has the required privilege
        List<Appointment> appointments = appointmentService.getAppointmentsByClientId(clientId);
        return ResponseEntity.ok(appointments);
    }

    // get all appointments based on shop Id and date
    @GetMapping("shop/{shopId}/{date}")
    public ResponseEntity<?> getAppointmentsByShopIdAndDate(@PathVariable Long shopId, @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
    
        Collection<? extends GrantedAuthority> roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities();

        // Check user role
        boolean isPrivilegedUser = roles.stream().anyMatch(role -> 
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
            return appointmentService.getAppointmentsByShopIdAndDate(shopId, date);
        } catch (JsonProcessingException | ParseException ex) {
            String errorMessage = "An error occurred while fetching the appointment.";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }
    }

    // get all available appointment slots based on shop Id and date
    @GetMapping("available-slots/shop/{shopId}/{date}")
    public ResponseEntity<?> getAvailableTimeSlotsByShopIdAndDate(
            @PathVariable Long shopId,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        
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
            return appointmentService.getAvailableTimeSlots(shopId, date);
        } catch (JsonProcessingException | ParseException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ex.getMessage()); // Return the custom error message
        }
    }

    // book appointment
    @PostMapping("/create")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<?> book(@Valid @RequestBody String appointmentSaveDTO, Principal principal) {

        Collection<? extends GrantedAuthority> roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities();

        // Check user role
        boolean isPrivilegedUser = roles.stream().anyMatch(role -> 
            "ROLE_CLIENT".equals(role.getAuthority()) || 
            "ROLE_ADMIN".equals(role.getAuthority()) || 
            "ROLE_MANAGER".equals(role.getAuthority()) ||
            "ROLE_RECEPTIONIST".equals(role.getAuthority())
        );

        // Condition to determine access
        boolean hasAccess = isPrivilegedUser;

        if (!hasAccess) {
            return new ResponseEntity<>("Please login!.", HttpStatus.FORBIDDEN);
        }

        try {
            return appointmentService.book(appointmentSaveDTO, principal);
        } catch (JsonProcessingException | ParseException ex) {
            // Handle the exception or return an appropriate response
            String errorMessage = "An error occurred while booking the appointment.";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }
    }

    // update appointment
    @PutMapping("/update/{id}")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<?> update(@Valid @RequestBody String appointmentSaveDTO, @PathVariable(name = "id") long id) {

        Appointment existingAppointment = appointmentService.getAppointmentById(id);

        // Check if the appointment exists
        if (existingAppointment == null) {
            return new ResponseEntity<>("Appointment not found.", HttpStatus.NOT_FOUND);
        }

        // Check if the user are correct
        Collection<? extends GrantedAuthority> roles = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities();
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!(existingAppointment.getClient().getEmail().equals(currentUsername)
                || roles.stream().anyMatch(role -> "ROLE_ADMIN".equals(role.getAuthority()))
                || roles.stream().anyMatch(role -> "ROLE_MANAGER".equals(role.getAuthority()))
                || roles.stream().anyMatch(role -> "ROLE_RECEPTIONIST".equals(role.getAuthority())))) {
            return new ResponseEntity<>("Access denied. Insufficient privileges.", HttpStatus.FORBIDDEN);
        }

        try {
            ResponseEntity<?> responseEntity = appointmentService.update(appointmentSaveDTO, id);
            return ResponseEntity.status(responseEntity.getStatusCode()).body(responseEntity.getBody());
        } catch (JsonProcessingException | ParseException ex) {
            // Handle the exception or return an appropriate response
            String errorMessage = "An error occurred while updating the appointment.";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }
    }

    // delete appointment
    @DeleteMapping("/remove/{id}")
    public ResponseEntity<String> delete(@PathVariable(name = "id") long id) {

        Appointment existingAppointment = appointmentService.getAppointmentById(id);

        // Check if the appointment exists
        if (existingAppointment == null) {
            return new ResponseEntity<>("Appointment not found.", HttpStatus.NOT_FOUND);
        }

        // Check if the user are correct
        Collection<? extends GrantedAuthority> roles = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities();
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!(existingAppointment.getClient().getEmail().equals(currentUsername)
                || roles.stream().anyMatch(role -> "ROLE_ADMIN".equals(role.getAuthority()))
                || roles.stream().anyMatch(role -> "ROLE_MANAGER".equals(role.getAuthority()))
                || roles.stream().anyMatch(role -> "ROLE_RECEPTIONIST".equals(role.getAuthority())))) {
            return new ResponseEntity<>("Access denied. Insufficient privileges.", HttpStatus.FORBIDDEN);
        }

        try {
            appointmentService.delete(id);
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

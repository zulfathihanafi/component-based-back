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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

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
import java.util.Collection;
import java.util.List;
import java.util.Map;

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

    // book appointment
    @PostMapping("/create")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<?> book(@Valid @RequestBody String appointmentSaveDTO, Principal principal) {

        Collection<? extends GrantedAuthority> roles = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities();

        try {

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> data = objectMapper.readValue(appointmentSaveDTO, new TypeReference<Map<String, Object>>() {});

            // Now you can access the clientId from the map
            Object clientIdObj = data.get("clientId");

            Long clientId = Long.parseLong(clientIdObj.toString());

            // Extract the username (email) of the logged-in user
            String currentUsername = principal.getName();

            // Get the Client object from the appointmentDTO
            Client client = clientService.getClientById(clientId);

            // Check if the email of the client matches the email of the logged-in user
            String clientEmail = client.getEmail();
            if (clientEmail.equals(currentUsername)||roles.stream().anyMatch(role -> "ROLE_ADMIN".equals(role.getAuthority()))
                || roles.stream().anyMatch(role -> "ROLE_MANAGER".equals(role.getAuthority()))
                || roles.stream().anyMatch(role -> "ROLE_RECEPTIONIST".equals(role.getAuthority()))) {
                // Email matches, proceed to book the appointment
                return appointmentService.book(appointmentSaveDTO, principal);
            } else {
                // Email doesn't match, return an unauthorized response
                return new ResponseEntity<>("Access denied. Insufficient privileges.",
                        HttpStatus.FORBIDDEN);
            }
        } catch (JsonProcessingException | ParseException ex) {
            // Handle the exception or return an appropriate response
            String errorMessage = "An error occurred while booking the appointment.";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }
    }

    // get all appointments based on petId
    @GetMapping("/pet/{petId}")
    public ResponseEntity<?> getAppointmentsByPetId(@PathVariable Long petId) {
        Pet pet = petService.getPetById(petId);

        // Check if the pet exists
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

        // Check if the doctor exists
        if (doctor == null) {
            return new ResponseEntity<>("Doctor not found.", HttpStatus.NOT_FOUND);
        }

        // Check if the user are correct
        Collection<? extends GrantedAuthority> roles = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities();
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!(doctor.getEmail().equals(currentUsername)
                || roles.stream().anyMatch(role -> "ROLE_ADMIN".equals(role.getAuthority()))
                || roles.stream().anyMatch(role -> "ROLE_MANAGER".equals(role.getAuthority()))
                || roles.stream().anyMatch(role -> "ROLE_RECEPTIONIST".equals(role.getAuthority())))) {
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

        // Check if the client exists
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
    @GetMapping("shop/{shopId}/doctor/{doctorId}/{date}")
    public ResponseEntity<?> getAppointmentsByShopIdAndDate(@PathVariable Long shopId, @PathVariable Long doctorId,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        Collection<? extends GrantedAuthority> roles = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities();

        // Check user role
        boolean isPrivilegedUser = roles.stream().anyMatch(role -> "ROLE_ADMIN".equals(role.getAuthority()) ||
                "ROLE_MANAGER".equals(role.getAuthority()) ||
                "ROLE_DOCTOR".equals(role.getAuthority()) ||
                "ROLE_RECEPTIONIST".equals(role.getAuthority()));

        // Condition to determine access
        boolean hasAccess = isPrivilegedUser;

        if (!hasAccess) {
            return new ResponseEntity<>("Access denied. Insufficient privileges.", HttpStatus.FORBIDDEN);
        }

        try {
            return appointmentService.getAppointmentsByShopIdAndDate(shopId, doctorId, date);
        } catch (JsonProcessingException | ParseException ex) {
            String errorMessage = "An error occurred while fetching the appointment.";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }
    }

    // get all available appointment slots based on shop Id and date
    @GetMapping("available-slots/shop/{shopId}/doctor/{doctorId}/{date}")
    public ResponseEntity<?> getAvailableTimeSlotsByShopIdAndDate(
            @PathVariable Long shopId,
            @PathVariable Long doctorId,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        Collection<? extends GrantedAuthority> roles = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities();

        // Check user role
        boolean isPrivilegedUser = roles.stream().anyMatch(role -> "ROLE_CLIENT".equals(role.getAuthority()) ||
                "ROLE_ADMIN".equals(role.getAuthority()) ||
                "ROLE_MANAGER".equals(role.getAuthority()) ||
                "ROLE_DOCTOR".equals(role.getAuthority()) ||
                "ROLE_RECEPTIONIST".equals(role.getAuthority()));

        // Condition to determine access
        boolean hasAccess = isPrivilegedUser;

        if (!hasAccess) {
            return new ResponseEntity<>("Access denied. Insufficient privileges.", HttpStatus.FORBIDDEN);
        }

        try {
            return appointmentService.getAvailableTimeSlots(shopId, doctorId, date);
        } catch (JsonProcessingException | ParseException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ex.getMessage()); // Return the custom error message
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

    private AppointmentDTO convertJsonToAppointmentDTO(String json) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, AppointmentDTO.class);
    }

}

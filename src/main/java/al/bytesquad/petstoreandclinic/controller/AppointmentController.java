package al.bytesquad.petstoreandclinic.controller;

import al.bytesquad.petstoreandclinic.entity.Appointment;
import al.bytesquad.petstoreandclinic.service.AppointmentService;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    // get all appointments based on petId
    @GetMapping("pet/{petId}")
    public ResponseEntity<List<Appointment>> getAppointmentsByPetId(@PathVariable Long petId) {
        List<Appointment> appointments = appointmentService.getAppointmentsByPetId(petId);
        return ResponseEntity.ok(appointments);
    }

    // get all appointments based on doctor Id
    @GetMapping("doctor/{doctorId}")
    public ResponseEntity<List<Appointment>> getAppointmentsByDoctorId(@PathVariable Long doctorId) {
        List<Appointment> appointments = appointmentService.getAppointmentsByDoctorId(doctorId);
        return ResponseEntity.ok(appointments);
    }

    // get all appointments based on client Id
    @GetMapping("client/{clientId}")
    public ResponseEntity<List<Appointment>> getAppointmentsByClientId(@PathVariable Long clientId) {
        List<Appointment> appointments = appointmentService.getAppointmentsByClientId(clientId);
        return ResponseEntity.ok(appointments);
    }

    // book appointment
    @PostMapping("/create")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<?> book(@Valid @RequestBody String appointmentSaveDTO, Principal principal) {
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
    // @DeleteMapping("/remove/{id}")
    // public ResponseEntity<String> delete(@PathVariable(name = "id") long id,
    // Principal principal) {
    // try {
    // appointmentService.delete(id, principal);
    // // If deletion is successful, return a success response with HTTP status 200
    // return new ResponseEntity<>("Deleted successfully", HttpStatus.OK);
    // } catch (Exception e) {
    // // If an error occurs during deletion, return an error response with HTTP
    // status 500
    // return new ResponseEntity<>("Error deleting the record: " + e.getMessage(),
    // HttpStatus.INTERNAL_SERVER_ERROR);
    // }
    // }

}

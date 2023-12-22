package al.bytesquad.petstoreandclinic.controller;

import al.bytesquad.petstoreandclinic.payload.entityDTO.AppointmentDTO;
import al.bytesquad.petstoreandclinic.service.AppointmentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    //get all appointments
    @GetMapping
    @CrossOrigin(origins = "http://localhost:3000")
    public List<AppointmentDTO> getAll(@RequestParam(required = false) String keyword, Principal principal) {
        return appointmentService.getAll(keyword, principal);
    }

    //book appointment
    @PostMapping("/create")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<AppointmentDTO> book(@Valid @RequestBody String appointmentSaveDTO, Principal principal) throws JsonProcessingException {
        return new ResponseEntity<>(appointmentService.book(appointmentSaveDTO, principal), HttpStatus.CREATED);
    }

    //update appointment
    @PutMapping("/update/{id}")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<AppointmentDTO> update(@Valid @RequestBody String appointmentSaveDTO, @PathVariable(name = "id") long id) throws JsonProcessingException {
        return new ResponseEntity<>(appointmentService.update(appointmentSaveDTO, id), HttpStatus.OK);
    }

    //delete appointment
    @DeleteMapping("/remove/{id}")
    public ResponseEntity<String> delete(@PathVariable(name = "id") long id, Principal principal) {
        try {
            appointmentService.delete(id, principal);
            // If deletion is successful, return a success response with HTTP status 200
            return new ResponseEntity<>("Deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            // If an error occurs during deletion, return an error response with HTTP status 500
            return new ResponseEntity<>("Error deleting the record: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
}

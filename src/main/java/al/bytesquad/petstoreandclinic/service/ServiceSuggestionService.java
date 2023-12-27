package al.bytesquad.petstoreandclinic.service;

import java.util.List;
import java.util.Comparator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import al.bytesquad.petstoreandclinic.entity.Appointment;
import al.bytesquad.petstoreandclinic.entity.PetServices;
import al.bytesquad.petstoreandclinic.repository.AppointmentRepository;

@Service
public class ServiceSuggestionService {
    private final AppointmentRepository appointmentRepository;

    @Autowired
    public ServiceSuggestionService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public String getServiceSuggestion(Long petId) {
        List<Appointment> appointments = appointmentRepository.findByPetId(petId);

        // Find the latest appointment
        Appointment latestAppointment = appointments.stream()
                .max(Comparator.comparing(Appointment::getFinishTime)) // Replace 'getFinishTime' with your date/time field
                .orElse(null);

        if (latestAppointment != null) {
            PetServices lastService = latestAppointment.getPetServices();
            if (lastService != null) {
                Long lastServiceId = lastService.getId();

                // Suggest the next service based on the last service ID
                switch (lastServiceId.intValue()) {
                    case 1:
                        return "Suggested Service: Vaccination";
                    case 2:
                        return "Suggested Service: Deworming";
                    case 3:
                        return "Suggested Service: Neutering/Spaying";
                    default:
                        return "Suggested Service: General Health Checkup";
                }
            }
        }
        return "Suggested Service: General Health Checkup"; // Default suggestion
    }
}

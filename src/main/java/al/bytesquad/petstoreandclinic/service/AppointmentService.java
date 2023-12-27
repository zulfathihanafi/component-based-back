package al.bytesquad.petstoreandclinic.service;

import al.bytesquad.petstoreandclinic.entity.*;
import al.bytesquad.petstoreandclinic.payload.entityDTO.AppointmentDTO;
import al.bytesquad.petstoreandclinic.payload.saveDTO.AppointmentSaveDTO;
import al.bytesquad.petstoreandclinic.repository.*;
import al.bytesquad.petstoreandclinic.service.exception.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ModelMapper modelMapper;
    private final ClientRepository clientRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DoctorRepository doctorRepository;
    private final ManagerRepository managerRepository;
    private final ObjectMapper objectMapper;
    private final PetRepository petRepository;
    private final ProductRepository productRepository;

    public AppointmentService(AppointmentRepository appointmentRepository, ModelMapper modelMapper,
            ClientRepository clientRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            DoctorRepository doctorRepository,
            ManagerRepository managerRepository, ObjectMapper objectMapper,
            PetRepository petRepository,
            ProductRepository productRepository,
            ServiceRepository serviceRepository) {
        this.appointmentRepository = appointmentRepository;
        this.modelMapper = modelMapper;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.doctorRepository = doctorRepository;
        this.managerRepository = managerRepository;
        this.serviceRepository = serviceRepository;
        this.objectMapper = objectMapper;

        modelMapper.addMappings(new PropertyMap<Appointment, AppointmentDTO>() {
            @Override
            protected void configure() {
                map().setId(source.getId());
            }
        });
        this.petRepository = petRepository;
        this.productRepository = productRepository;
    }

    public AppointmentDTO book(String jsonString, Principal principal) throws JsonProcessingException, ParseException {
        AppointmentSaveDTO appointmentSaveDTO = objectMapper.readValue(jsonString, AppointmentSaveDTO.class);

        // Fetch the shop of the selected doctor
        Doctor doctor = doctorRepository.findById(appointmentSaveDTO.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", appointmentSaveDTO.getDoctorId()));

        Shop shop = doctor.getShop();

        // Parse the start and finish times from the provided strings
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kuala_Lumpur")); // Set the timezone to Malaysia
        Date requestedStartTime = dateFormat.parse(appointmentSaveDTO.getStartTime());

        // Calculate the requestedEndTime as 1 hour after the requestedStartTime
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(requestedStartTime);
        calendar.add(Calendar.MINUTE, 59);
        Date computedFinishTime = calendar.getTime();

        // Check if the shop is open during the requested appointment time
        LocalTime startTime = shop.getStartWorkingTime().minusMinutes(1);
        LocalTime endTime = shop.getEndWorkingTime();
        LocalTime breakStartTime = LocalTime.of(13, 0); // 1:00 PM
        LocalTime breakEndTime = LocalTime.of(13, 59); // 2:00 PM

        // Convert the requested time to LocalTime
        LocalTime requestedStartTimeLocal = requestedStartTime.toInstant().atZone(ZoneId.of("Asia/Kuala_Lumpur"))
                .toLocalTime();
        LocalTime requestedEndTimeLocal = computedFinishTime.toInstant().atZone(ZoneId.of("Asia/Kuala_Lumpur"))
                .toLocalTime();

        // Check if the appointment falls within the shop's working hours
        if (requestedStartTimeLocal.isAfter(startTime) && requestedEndTimeLocal.isBefore(endTime)) {
            // Check if the appointment falls within the break time (strictly not allowed)
            if ((requestedStartTimeLocal.isBefore(breakStartTime) && requestedEndTimeLocal.isBefore(breakStartTime))
                    || (requestedStartTimeLocal.isAfter(breakEndTime) && requestedEndTimeLocal.isAfter(breakEndTime))) {
                // Check for overlapping appointments during non-break hours
                List<Appointment> overlappingAppointments = appointmentRepository
                        .findByDoctorIdAndStartTimeBetweenAndFinishTimeBetween(
                                doctor.getId(), requestedStartTime, computedFinishTime, requestedStartTime,
                                computedFinishTime);

                if (!overlappingAppointments.isEmpty()) {
                    throw new RuntimeException("Appointment overlaps with existing appointments");
                } else {
                    // The appointment is valid, proceed with saving it
                    Appointment appointment = modelMapper.map(appointmentSaveDTO, Appointment.class);
                    appointment.setStartTime(requestedStartTime);
                    appointment.setFinishTime(computedFinishTime);
                    Client client = clientRepository.findById(appointmentSaveDTO.getClientId())
                            .orElseThrow(() -> new ResourceNotFoundException("Client", "id",
                                    appointmentSaveDTO.getClientId()));
                    Pet pet = petRepository.findById(appointmentSaveDTO.getPetId())
                            .orElseThrow(
                                    () -> new ResourceNotFoundException("Pet", "id", appointmentSaveDTO.getPetId()));
                    PetServices petService = serviceRepository.findById(appointmentSaveDTO.getServiceId())
                            .orElseThrow(() -> new ResourceNotFoundException("Service", "id",
                                    appointmentSaveDTO.getServiceId()));

                    // Check if the requested petId belongs to the client
                    if (!pet.getClient().getId().equals(client.getId())) {
                        throw new RuntimeException("You can only update appointments for your own pets.");
                    } else {
                        appointment.setClient(client);
                        appointment.setDoctor(doctor);
                        appointment.setPet(pet);
                        appointment.setPetServices(petService);

                        // Save the appointment
                        Appointment newAppointment = appointmentRepository.save(appointment);

                        return modelMapper.map(newAppointment, AppointmentDTO.class);
                    }

                }

            } else {
                throw new RuntimeException("Appointment cannot be booked during the break time");
            }

        } else {
            throw new RuntimeException("Appointment time is not valid");
        }
    }

    public AppointmentDTO update(String jsonString, long id) throws JsonProcessingException, ParseException {
        AppointmentSaveDTO appointmentSaveDTO = objectMapper.readValue(jsonString, AppointmentSaveDTO.class);
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));

        // Fetch the shop of the selected doctor
        Doctor doctor = doctorRepository.findById(appointmentSaveDTO.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", appointmentSaveDTO.getDoctorId()));
        Shop shop = doctor.getShop();

        // Parse the start and finish times from the provided strings
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kuala_Lumpur")); // Set the timezone to Malaysia
        Date requestedStartTime = dateFormat.parse(appointmentSaveDTO.getStartTime());

        // Calculate the requestedEndTime as 1 hour after the requestedStartTime
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(requestedStartTime);
        calendar.add(Calendar.MINUTE, 59);
        Date computedFinishTime = calendar.getTime();

        // Check if the shop is open during the requested appointment time
        LocalTime startTime = shop.getStartWorkingTime().minusMinutes(1);
        LocalTime endTime = shop.getEndWorkingTime();
        LocalTime breakStartTime = LocalTime.of(13, 0); // 1:00 PM
        LocalTime breakEndTime = LocalTime.of(13, 59); // 2:00 PM

        // Convert the requested time to LocalTime
        LocalTime requestedStartTimeLocal = requestedStartTime.toInstant().atZone(ZoneId.of("Asia/Kuala_Lumpur"))
                .toLocalTime();
        LocalTime requestedEndTimeLocal = computedFinishTime.toInstant().atZone(ZoneId.of("Asia/Kuala_Lumpur"))
                .toLocalTime();

        // Check if the appointment falls within the shop's working hours
        if (requestedStartTimeLocal.isAfter(startTime) && requestedEndTimeLocal.isBefore(endTime)) {
            // Check if the appointment falls within the break time (strictly not allowed)
            if ((requestedStartTimeLocal.isBefore(breakStartTime) && requestedEndTimeLocal.isBefore(breakStartTime))
                    || (requestedStartTimeLocal.isAfter(breakEndTime) && requestedEndTimeLocal.isAfter(breakEndTime))) {
                // Check for overlapping appointments during non-break hours
                List<Appointment> overlappingAppointments = appointmentRepository
                        .findByDoctorIdAndStartTimeBetweenAndFinishTimeBetweenAndIdNot(
                                doctor.getId(), requestedStartTime, computedFinishTime, requestedStartTime,
                                computedFinishTime, id);

                if (!overlappingAppointments.isEmpty()) {
                    throw new RuntimeException("Appointment overlaps with existing appointments");
                } else {
                    // Update the appointment fields
                    appointment.setStartTime(requestedStartTime);
                    appointment.setFinishTime(computedFinishTime);

                    // Retrieve the client, doctor, pet, and service entities
                    Client client = clientRepository.findById(appointmentSaveDTO.getClientId())
                            .orElseThrow(() -> new ResourceNotFoundException("Client", "id",
                                    appointmentSaveDTO.getClientId()));
                    Pet pet = petRepository.findById(appointmentSaveDTO.getPetId())
                            .orElseThrow(
                                    () -> new ResourceNotFoundException("Pet", "id", appointmentSaveDTO.getPetId()));
                    PetServices petService = serviceRepository.findById(appointmentSaveDTO.getServiceId())
                            .orElseThrow(() -> new ResourceNotFoundException("Service", "id",
                                    appointmentSaveDTO.getServiceId()));

                    // Check if the requested petId belongs to the client
                    if (!pet.getClient().getId().equals(client.getId())) {
                        throw new RuntimeException("You can only update appointments for your own pets.");
                    } else {
                        // Update the appointment with the new relationships
                        appointment.setClient(client);
                        appointment.setDoctor(doctor);
                        appointment.setPet(pet);
                        appointment.setPetServices(petService);

                        // Save the updated appointment
                        Appointment updatedAppointment = appointmentRepository.save(appointment);

                        // Return the updated appointment DTO
                        return modelMapper.map(updatedAppointment, AppointmentDTO.class);
                    }

                }
            } else {
                throw new RuntimeException("Appointment cannot be booked during the break time");
            }
        } else {
            throw new RuntimeException("Appointment time is not valid");
        }
    }

    // public String delete(long id, Principal principal) {
    // String clientEmail = principal.getName();
    // Appointment appointment = appointmentRepository.findById(id)
    // .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));
    // if (clientEmail.equals(appointment.getClient().getEmail())) {
    // appointmentRepository.delete(appointment);
    // return "Appointment deleted successfully!";
    // }
    // return "Cannot delete appointment!";
    // }

    public void delete(long id) {
        appointmentRepository.deleteById(id);
    }

    public List<Appointment> getAppointmentsByPetId(Long petId) {
        List<Appointment> appointments = appointmentRepository.findByPetId(petId);
        return appointments;
    }

    public List<Appointment> getAppointmentsByDoctorId(Long doctorId) {
        List<Appointment> appointments = appointmentRepository.findByDoctorId(doctorId);
        return appointments;
    }

    public List<Appointment> getAppointmentsByClientId(Long clientId) {
        List<Appointment> appointments = appointmentRepository.findByClientId(clientId);
        return appointments;
    }

}

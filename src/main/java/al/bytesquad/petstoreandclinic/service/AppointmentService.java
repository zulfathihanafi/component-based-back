package al.bytesquad.petstoreandclinic.service;

import al.bytesquad.petstoreandclinic.entity.*;
import al.bytesquad.petstoreandclinic.payload.entityDTO.AppointmentDTO;
import al.bytesquad.petstoreandclinic.payload.saveDTO.AppointmentSaveDTO;
import al.bytesquad.petstoreandclinic.repository.*;
import al.bytesquad.petstoreandclinic.service.exception.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;


@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ModelMapper modelMapper;
    private final ClientRepository clientRepository;
    private final DoctorRepository doctorRepository;
    private final PetRepository petRepository;
    private final ServiceRepository serviceRepository;
    private final ShopRepository shopRepository;
    private final ObjectMapper objectMapper;

    public AppointmentService(
            AppointmentRepository appointmentRepository, 
            ModelMapper modelMapper,
            ClientRepository clientRepository,
            DoctorRepository doctorRepository,
            PetRepository petRepository,
            ServiceRepository serviceRepository, 
            ShopRepository shopRepository,
            ObjectMapper objectMapper) {
        this.appointmentRepository = appointmentRepository;
        this.modelMapper = modelMapper;
        this.clientRepository = clientRepository;
        this.doctorRepository = doctorRepository;
        this.petRepository = petRepository;
        this.serviceRepository = serviceRepository;
        this.shopRepository = shopRepository;
        this.objectMapper = objectMapper;

        modelMapper.addMappings(new PropertyMap<Appointment, AppointmentDTO>() {
            @Override
            protected void configure() {
                map().setId(source.getId());
            }
        });      
    }

    // book appointment
    public ResponseEntity<?> book(String jsonString, Principal principal)
            throws JsonProcessingException, ParseException {
        AppointmentSaveDTO appointmentSaveDTO = objectMapper.readValue(jsonString, AppointmentSaveDTO.class);

        // Fetch the shop of the selected doctor
        Doctor doctor = doctorRepository.findById(appointmentSaveDTO.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", appointmentSaveDTO.getDoctorId()));

        // Check if the doctor is enabled
        if (!doctor.isEnabled()) {
            // Return a custom error response with a 400 Bad Request status and an error message
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Doctor is not enabled for appointments");
        }

        Shop shop = doctor.getShop();

        // Check if the shop is enabled
        if (!shop.isEnabled()) {
            // Return a custom error response with a 400 Bad Request status and an error message
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Clinic is not enabled for appointments");
        }

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
                    // Return a custom error response with a 400 Bad Request status and an error message
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Appointment overlaps with existing appointments");
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
                        // Return a custom error response with a 400 Bad Request status and an error message
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body("You can only create appointments for your own pets.");
                    } else {
                        appointment.setClient(client);
                        appointment.setDoctor(doctor);
                        appointment.setPet(pet);
                        appointment.setPetServices(petService);
                        appointment.setShop(shop);

                        // Save the appointment
                        Appointment newAppointment = appointmentRepository.save(appointment);

                        // Return the appointment DTO with a 200 OK status
                        return ResponseEntity.ok(modelMapper.map(newAppointment, AppointmentDTO.class));
                    }
                }
            } else {
                // Return a custom error response with a 400 Bad Request status and an error message
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Appointment cannot be booked during the break time 1pm-2pm (13:00-14:00)");
            }
        } else {
            // Return a custom error response with a 400 Bad Request status and an error message
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Appointment time is not valid (Out of Operation Hours)");
        }
    }

    // get by appointment id
    public Appointment getAppointmentById(Long id){
        return appointmentRepository.findById(id).orElse(null);
    }

    // get appointment by pet id
    public List<Appointment> getAppointmentsByPetId(Long petId) {
        List<Appointment> appointments = appointmentRepository.findByPetId(petId);
        return appointments;
    }

    // get appointment by doctor id
    public List<Appointment> getAppointmentsByDoctorId(Long doctorId) {
        List<Appointment> appointments = appointmentRepository.findByDoctorId(doctorId);
        return appointments;
    }

    // get appointment by client id
    public List<Appointment> getAppointmentsByClientId(Long clientId) {
        List<Appointment> appointments = appointmentRepository.findByClientId(clientId);
        return appointments;
    }

    // retrive all the appointments based on shopId and date
    public ResponseEntity<?> getAppointmentsByShopIdAndDate(Long shopId, LocalDate date)
            throws JsonProcessingException, ParseException {
        // Convert the LocalDate to Date with the specified timezone (e.g.,
        // Asia/Kuala_Lumpur)
        Date startDate = Date.from(date.atStartOfDay(ZoneId.of("Asia/Kuala_Lumpur")).toInstant());
        Date endDate = Date.from(date.atStartOfDay(ZoneId.of("Asia/Kuala_Lumpur")).plusDays(1).toInstant());

        // Fetch the shop by its ID to check if it is enabled
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop", "id", shopId));

        // Check if the shop is enabled
        if (!shop.isEnabled()) {
            // Return a custom error response with a 400 Bad Request status and an error message
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Clinic is not enabled for appointments");
        } else {
            // Use a repository method to fetch appointments based on the shopId and date
            List<Appointment> appointments = appointmentRepository.findByShopIdAndStartTimeBetween(shopId, startDate,
                    endDate);

            return ResponseEntity.ok(appointments);
        }
    }

    // get all available appointments slots based on shop Id and date
    public ResponseEntity<?> getAvailableTimeSlots(Long shopId, LocalDate date)
            throws JsonProcessingException, ParseException {
        Date startDate = Date.from(date.atStartOfDay(ZoneId.of("Asia/Kuala_Lumpur")).toInstant());
        Date endDate = Date.from(date.atStartOfDay(ZoneId.of("Asia/Kuala_Lumpur")).plusDays(1).toInstant());

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop", "id", shopId));

        if (!shop.isEnabled()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Clinic is not enabled for appointments");
        } else {
            List<Appointment> bookedAppointments = appointmentRepository.findByShopIdAndStartTimeBetween(
                    shopId, startDate, endDate);

            // Fetch shop's working hours
            LocalTime startTime = shop.getStartWorkingTime();
            LocalTime endTime = shop.getEndWorkingTime();

            // Calculate available time slots
            List<String> availableTimeSlots = calculateAvailableTimeSlots(startDate, startTime, endTime, bookedAppointments);

            return ResponseEntity.ok(availableTimeSlots);
        }
    }

    // Calculate available time slots
    private List<String> calculateAvailableTimeSlots(Date startDate, LocalTime startTime, LocalTime endTime, List<Appointment> bookedAppointments) {
        List<String> availableTimeSlots = new ArrayList<>();

        // Define time slot duration (59 mins)
        int slotDurationMinutes = 59;

        // Create a calendar instance to iterate over time slots
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("Asia/Kuala_Lumpur"));
        calendar.setTime(startDate);

        // Initialize calendar time to the start time
        calendar.set(Calendar.HOUR_OF_DAY, startTime.getHour());
        calendar.set(Calendar.MINUTE, startTime.getMinute());

        // End time as calendar
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTimeZone(TimeZone.getTimeZone("Asia/Kuala_Lumpur"));
        endCalendar.setTime(startDate);
        endCalendar.set(Calendar.HOUR_OF_DAY, endTime.getHour());
        endCalendar.set(Calendar.MINUTE, endTime.getMinute());

        while (calendar.before(endCalendar)) {
            // Check if the current time slot is within the shop's working hours
            LocalTime slotLocalTime = LocalTime.of(calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE));

            if (slotLocalTime.isAfter(startTime.minusMinutes(1)) &&
                    slotLocalTime.isBefore(endTime)) {

                // Check if the current time slot is within the break time
                LocalTime breakStartTime = LocalTime.of(13, 0); // 1:00 PM
                LocalTime breakEndTime = LocalTime.of(14, 0); // 1:59 PM

                if (!(slotLocalTime.equals(breakStartTime) || (slotLocalTime.isAfter(breakStartTime) && slotLocalTime.isBefore(breakEndTime)))) {
                    // Check if the current time slot is available (not in booked appointments)
                    Date slotStartTime = calendar.getTime();
                    calendar.add(Calendar.MINUTE, slotDurationMinutes);
                    Date slotEndTime = calendar.getTime();

                    boolean isSlotAvailable = true;
                    for (Appointment appointment : bookedAppointments) {
                        if (slotStartTime.equals(appointment.getStartTime())) {
                            // There is an overlap with a booked appointment
                            isSlotAvailable = false;
                            break;
                        }
                    }

                    if (isSlotAvailable) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
                        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kuala_Lumpur")); // Set the timezone to Malaysia
                        String startTimeStr = dateFormat.format(slotStartTime);
                        String finishTimeStr = dateFormat.format(slotEndTime);
                        availableTimeSlots.add("startTime: " + startTimeStr + ", finishTime: " + finishTimeStr);
                    }
                }
            }
            calendar.add(Calendar.MINUTE, 1); // Move to the next time slot
        }

        return availableTimeSlots;
    }

    // update appointment
    public ResponseEntity<?> update(String jsonString, long id) throws JsonProcessingException, ParseException {
        AppointmentSaveDTO appointmentSaveDTO = objectMapper.readValue(jsonString, AppointmentSaveDTO.class);
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));

        // Fetch the shop of the selected doctor
        Doctor doctor = doctorRepository.findById(appointmentSaveDTO.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", appointmentSaveDTO.getDoctorId()));

        // Check if the doctor is enabled
        if (!doctor.isEnabled()) {
            // Return a custom error response with a 400 Bad Request status and an error message
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Doctor is not enabled for appointments");
        }

        Shop shop = doctor.getShop();

        // Check if the doctor is enabled
        if (!shop.isEnabled()) {
            // Return a custom error response with a 400 Bad Request status and an error message
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Clinic is not enabled for appointments");
        }

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
                    // Return a custom error response with a 400 Bad Request status and an error message
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Appointment overlaps with existing appointments");
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
                        // Return a custom error response with a 400 Bad Request status and an error message
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body("You can only update appointments for your own pets.");
                    } else {
                        // Update the appointment with the new relationships
                        appointment.setClient(client);
                        appointment.setDoctor(doctor);
                        appointment.setShop(shop);
                        appointment.setPet(pet);
                        appointment.setPetServices(petService);
                        appointment.setShop(shop);

                        // Save the updated appointment
                        Appointment updatedAppointment = appointmentRepository.save(appointment);

                        // Return the updated appointment DTO with a 200 OK status
                        return ResponseEntity.ok(modelMapper.map(updatedAppointment, AppointmentDTO.class));
                    }
                }
            } else {
                // Return a custom error response with a 400 Bad Request status and an error message
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Appointment cannot be booked during the break time 1pm-2pm (13:00-14:00)");
            }
        } else {
            // Return a custom error response with a 400 Bad Request status and an error message
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Appointment time is not valid (Out of Operation Hours)");
        }
    }

    // delete appointment
    public void delete(long id) {
        appointmentRepository.deleteById(id);
    }

}

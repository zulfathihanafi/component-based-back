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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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

    public AppointmentDTO book(String jsonString, Principal principal) throws JsonProcessingException {
        AppointmentSaveDTO appointmentSaveDTO = objectMapper.readValue(jsonString, AppointmentSaveDTO.class);

        Appointment appointment = modelMapper.map(appointmentSaveDTO, Appointment.class);
        Client client = clientRepository.findClientById(appointmentSaveDTO.getClientId()).orElseThrow(() -> new ResourceNotFoundException("Client", "id", appointmentSaveDTO.getClientId()));
        Doctor doctor = doctorRepository.findDoctorById(appointmentSaveDTO.getDoctorId()).orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", appointmentSaveDTO.getDoctorId()));
        Pet pet = petRepository.findById(appointmentSaveDTO.getPetId()).orElseThrow(() -> new ResourceNotFoundException("Pet", "id", appointmentSaveDTO.getPetId()));
        PetServices petService = serviceRepository.findById(appointmentSaveDTO.getServiceId()).orElseThrow(() -> new ResourceNotFoundException("Service", "id", appointmentSaveDTO.getServiceId()));
        appointment.setClient(client);
        appointment.setDoctor(doctor);
        appointment.setPet(pet);
        appointment.setPetServices(petService);
        /*String email = principal.getName();
        Client client = clientRepository.findByEmail(email);
        appointment.setClient(client);*/
        Appointment newAppointment = appointmentRepository.save(appointment);
        return modelMapper.map(newAppointment, AppointmentDTO.class);
    }

    public AppointmentDTO update(String jsonString, long id) throws JsonProcessingException {
        AppointmentSaveDTO appointmentSaveDTO = objectMapper.readValue(jsonString, AppointmentSaveDTO.class);
        Appointment appointment = appointmentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));
        appointment.setStartTime(appointmentSaveDTO.getStartTime());
        appointment.setFinishTime(appointmentSaveDTO.getFinishTime());
        if (appointmentSaveDTO.getClientId() != null)
            appointment.setClient(clientRepository.findById(appointmentSaveDTO.getClientId()).orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id)));
        if (appointmentSaveDTO.getDoctorId() != null)
            appointment.setDoctor(doctorRepository.findById(appointmentSaveDTO.getDoctorId()).orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id)));
        if (appointmentSaveDTO.getPetId() != null)
            appointment.setPet(petRepository.findById(appointmentSaveDTO.getPetId()).orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id)));
        if (appointmentSaveDTO.getServiceId() != null)
            appointment.setPetServices(serviceRepository.findById(appointmentSaveDTO.getServiceId()).orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id)));
        return modelMapper.map(appointmentRepository.save(appointment), AppointmentDTO.class);
    }

    public String delete(long id, Principal principal) {
        String clientEmail = principal.getName();
        Appointment appointment = appointmentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));
        if (clientEmail.equals(appointment.getClient().getEmail())) {
            appointmentRepository.delete(appointment);
            return "Appointment deleted successfully!";
        }
        return "Cannot delete appointment!";
    }

    public List<AppointmentDTO> getAll(String keyword, Principal principal) {
        if (keyword == null)
            return appointmentRepository.findAll().stream().map(appointment -> modelMapper.map(appointment, AppointmentDTO.class)).collect(Collectors.toList());

        List<String> keyValues = List.of(keyword.split(","));
        HashMap<String, String> pairs = new HashMap<>();
        for (String s : keyValues) {
            String[] strings = s.split(":");
            pairs.put(strings[0], strings[1]);
        }

        List<Appointment> appointments = appointmentRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            for (String key : pairs.keySet()) {
                Path<Object> fieldPath = root.get(key);
                predicates.add(criteriaBuilder.equal(fieldPath, pairs.get(key)));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });

        /*List<Appointment> filteredList;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loggedInEmail = principal.getName();
        User user = userRepository.findByEmail(loggedInEmail);
        List<String> userRoles = user.getRoles().stream()
                .map(Role::getName).toList();

        String selectedRole = null;

        if (authentication != null && userRoles.contains(authentication.getAuthorities().iterator().next().getAuthority())) {
            selectedRole = authentication.getAuthorities().iterator().next().getAuthority();
            // Assuming the authority is in the format "ROLE_{ROLE_NAME}"
            selectedRole = selectedRole.substring("ROLE_".length()).toLowerCase();
        }

        if ("client".equals(selectedRole)) {
            filteredList = appointments.stream()
                    .filter(appointment -> appointment.getClient().equals(clientRepository.findByEmail(loggedInEmail)))
                    .collect(Collectors.toList());
        } else if ("receptionist".equals(selectedRole)) {
            filteredList = null;
        } else if ("doctor".equals(selectedRole)) {
            filteredList = appointments.stream()
                    .filter(appointment -> appointment.getDoctor().equals(doctorRepository.findByEmail(loggedInEmail)))
                    .collect(Collectors.toList());
        } else if ("manager".equals(selectedRole)) {
            filteredList = appointments.stream()
                    .filter(appointment -> appointment.getDoctor().getShop().equals(managerRepository.findByEmail(loggedInEmail).getShop()))
                    .collect(Collectors.toList());
        } else {
            filteredList = appointments;
        }

        if (filteredList == null)
            return null;*/
        return appointments.stream().map(appointment -> modelMapper.map(appointment, AppointmentDTO.class)).collect(Collectors.toList());
    }

//     private AppointmentDTO mapAppointmentToDTO(Appointment appointment) {
//     AppointmentDTO dto = modelMapper.map(appointment, AppointmentDTO.class);

//     // Parse the datetime(6) values into Date objects
//     SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

//     try {
//         Date startTime = dateFormat.parse(appointment.getStartTime().toString()); // Access the attribute directly
//         Date finishTime = dateFormat.parse(appointment.getFinishTime().toString()); // Access the attribute directly

//         dto.setStartTime(startTime);
//         dto.setFinishTime(finishTime);
//     } catch (ParseException e) {
//         // Handle parsing error
//         e.printStackTrace();
//     }

//     return dto;
// }

}

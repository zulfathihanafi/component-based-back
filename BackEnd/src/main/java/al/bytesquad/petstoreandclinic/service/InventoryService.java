package al.bytesquad.petstoreandclinic.service;

import al.bytesquad.petstoreandclinic.entity.Inventory;
import al.bytesquad.petstoreandclinic.payload.entityDTO.InventoryDTO;
import al.bytesquad.petstoreandclinic.payload.saveDTO.InventorySaveDTO;
import al.bytesquad.petstoreandclinic.repository.InventoryRepository;
import al.bytesquad.petstoreandclinic.repository.UserRepository;
import al.bytesquad.petstoreandclinic.service.exception.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public InventoryService(InventoryRepository inventoryRepository, ModelMapper modelMapper, UserRepository userRepository, ObjectMapper objectMapper) {
        this.inventoryRepository = inventoryRepository;
        this.modelMapper = modelMapper;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;

        modelMapper.addMappings(new PropertyMap<Inventory, InventoryDTO>() {
            @Override
            protected void configure() {
                map().setId(source.getId());
            }
        });
    }

    public InventoryDTO create(String jsonString) throws JsonProcessingException {
        InventorySaveDTO inventorySaveDTO = objectMapper.readValue(jsonString, InventorySaveDTO.class);
        Inventory inventory = modelMapper.map(inventorySaveDTO, Inventory.class);
        return modelMapper.map(inventoryRepository.save(inventory), InventoryDTO.class);
    }

    public InventoryDTO update(String jsonString, long id) throws JsonProcessingException {
        InventorySaveDTO inventorySaveDTO = objectMapper.readValue(jsonString, InventorySaveDTO.class);
        Inventory inventory = inventoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", id));
        inventory.setName(inventorySaveDTO.getName());
        inventory.setPricePerUnit(inventorySaveDTO.getPricePerUnit());
        inventory.setStock(inventorySaveDTO.getStock());
        inventory.setType(inventorySaveDTO.getType());
        return modelMapper.map(inventoryRepository.save(inventory), InventoryDTO.class);
    }

    public String delete(long id) {
        Inventory inventory = inventoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", id));
        inventory.setEnabled(false);
        inventoryRepository.save(inventory);
        return "Inventory deleted successfully!";
    }

    public List<InventoryDTO> get(String keyword, Principal principal) {
        if (keyword == null)
            return inventoryRepository.findAllByEnabled(true).stream().map(inventory -> modelMapper.map(inventory, InventoryDTO.class)).collect(Collectors.toList());

        List<String> keyValues = List.of(keyword.split(","));
        HashMap<String, String> pairs = new HashMap<>();
        for (String s : keyValues) {
            String[] strings = s.split(":");
            pairs.put(strings[0], strings[1]);
        }
        pairs.put("enabled", "1");

        List<Inventory> inventorys = inventoryRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            for (String key : pairs.keySet()) {
                Path<Object> fieldPath = root.get(key);
                predicates.add(criteriaBuilder.equal(fieldPath, pairs.get(key)));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });

        /*List<Inventory> filteredInventorys;

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

        if (selectedRole.equals("doctor"))
            filteredInventorys = inventorys.stream().filter(inventory -> inventory.getType().equalsIgnoreCase("medical")).collect(Collectors.toList());
        else if (!selectedRole.equals("client"))
            filteredInventorys = inventorys;
        else
            filteredInventorys = null;

        if (filteredInventorys == null)
            return null;*/
        return inventorys.stream().map(inventory -> modelMapper.map(inventory, InventoryDTO.class)).collect(Collectors.toList());
    }

    public InventoryDTO addStock(long id, double imported) {
        Inventory inventory = inventoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", id));
        inventory.setStock(inventory.getStock()+imported);
        return modelMapper.map(inventoryRepository.save(inventory), InventoryDTO.class);
    }
}

package al.bytesquad.petstoreandclinic.service;

import al.bytesquad.petstoreandclinic.entity.Admin;
import al.bytesquad.petstoreandclinic.entity.Role;
import al.bytesquad.petstoreandclinic.entity.Shop;
import al.bytesquad.petstoreandclinic.entity.User;
import al.bytesquad.petstoreandclinic.payload.entityDTO.AdminDTO;
import al.bytesquad.petstoreandclinic.payload.entityDTO.ShopDTO;
import al.bytesquad.petstoreandclinic.payload.saveDTO.ShopSaveDTO;
import al.bytesquad.petstoreandclinic.repository.ManagerRepository;
import al.bytesquad.petstoreandclinic.repository.ShopRepository;
import al.bytesquad.petstoreandclinic.repository.UserRepository;
import al.bytesquad.petstoreandclinic.service.exception.ResourceNotFoundException;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShopService {

    private final ShopRepository shopRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final ManagerRepository managerRepository;

    @Autowired
    public ShopService(ShopRepository shopRepository, ModelMapper modelMapper, UserRepository userRepository, ManagerRepository managerRepository) {
        this.shopRepository = shopRepository;
        this.modelMapper = modelMapper;
        this.userRepository = userRepository;
        this.managerRepository = managerRepository;

        modelMapper.addMappings(new PropertyMap<Shop, ShopDTO>() {
            @Override
            protected void configure() {
                map().setId(source.getId());
            }
        });
    }

    public ShopDTO create(ShopSaveDTO shopSaveDTO) {
        Shop shop = modelMapper.map(shopSaveDTO, Shop.class);
        return modelMapper.map(shopRepository.save(shop), ShopDTO.class);
    }

    public ShopDTO update(ShopSaveDTO shopSaveDTO, long id) {
        Shop shop = shopRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Shop", "id", id));
        shop.setName(shopSaveDTO.getName());
        shop.setLocation(shopSaveDTO.getLocation());
        shop.setManager(shopSaveDTO.getManager());
        return modelMapper.map(shopRepository.save(shop), ShopDTO.class);
    }

    public void delete(long id) {
        Shop shop = shopRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Shop", "id", id));
        shop.setEnabled(false);
        modelMapper.map(shopRepository.save(shop), ShopDTO.class);
    }

    public List<ShopDTO> get(String keyword, Principal principal) {
        if(keyword == null)
            return shopRepository.findAllByEnabled(true).stream().map(shop -> modelMapper.map(shop, ShopDTO.class)).collect(Collectors.toList());

        List<String> keyValues = List.of(keyword.split(","));
        HashMap<String, String> pairs = new HashMap<>();
        for (String s : keyValues) {
            String[] strings = s.split(":");
            pairs.put(strings[0], strings[1]);
        }
        pairs.put("enabled", "1");

        List<Shop> shops = shopRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            for (String key : pairs.keySet()) {
                Path<Object> fieldPath = root.get(key);
                predicates.add(criteriaBuilder.equal(fieldPath, pairs.get(key)));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });

        List<Shop> filtered;

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

        if (selectedRole.equals("admin") || selectedRole.equals("client"))
            filtered = shops;
        else if (selectedRole.equals("manager"))
            filtered = shops.stream().filter(shop -> shop.getManager().equals(managerRepository.findByEmail(loggedInEmail))).collect(Collectors.toList());
        else filtered = null;

        if (filtered == null)
            return null;
        return filtered.stream().map(shop -> modelMapper.map(shop, ShopDTO.class)).collect(Collectors.toList());
    }
}

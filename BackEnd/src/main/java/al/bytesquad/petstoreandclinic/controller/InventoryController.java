package al.bytesquad.petstoreandclinic.controller;

import al.bytesquad.petstoreandclinic.payload.entityDTO.InventoryDTO;
import al.bytesquad.petstoreandclinic.repository.InventoryRepository;
import al.bytesquad.petstoreandclinic.service.InventoryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;
    private final InventoryRepository inventoryRepository;

    @Autowired
    public InventoryController(InventoryService inventoryService,
                               InventoryRepository inventoryRepository) {
        this.inventoryService = inventoryService;
        this.inventoryRepository = inventoryRepository;
    }

    @PostMapping("/create")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<InventoryDTO> addNew(@Valid @RequestBody String inventorySaveDTO) throws JsonProcessingException {
        return new ResponseEntity<>(inventoryService.create(inventorySaveDTO), HttpStatus.CREATED);
    }

    @PostMapping("/update/{id}")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<InventoryDTO> update(@Valid @RequestBody String inventorySaveDTO, @PathVariable(name = "id") long id) throws JsonProcessingException {
        return new ResponseEntity<>(inventoryService.update(inventorySaveDTO, id), HttpStatus.OK);
    }

    @PostMapping("/addStock/{id}")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<InventoryDTO> addStock(@PathVariable(name = "id") long id, @RequestParam double imported){
        return new ResponseEntity<>(inventoryService.addStock(id, imported), HttpStatus.OK);
    }

    @PutMapping("/remove/{id}")
    @CrossOrigin(origins = "http://localhost:3000")
    public String delete(@PathVariable(name = "id") long id) {
        return inventoryService.delete(id);
    }

    @GetMapping
    @CrossOrigin(origins = "http://localhost:3000")
    public List<InventoryDTO> get(@RequestParam(required = false) String keyword, Principal principal) {
        return inventoryService.get(keyword, principal);
    }
}

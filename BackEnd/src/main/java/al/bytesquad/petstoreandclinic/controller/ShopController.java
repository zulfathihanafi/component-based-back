package al.bytesquad.petstoreandclinic.controller;

import al.bytesquad.petstoreandclinic.payload.entityDTO.ShopDTO;
import al.bytesquad.petstoreandclinic.payload.saveDTO.ShopSaveDTO;
import al.bytesquad.petstoreandclinic.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/shops")
public class ShopController {

    private final ShopService shopService;

    @Autowired
    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @PostMapping("/create")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<ShopDTO> addNew(@Valid @RequestBody ShopSaveDTO shopSaveDTO) {
        return new ResponseEntity<>(shopService.create(shopSaveDTO), HttpStatus.CREATED);
    }

    @PutMapping("/update/{id}")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<ShopDTO> update(@Valid @RequestBody ShopSaveDTO shopSaveDTO, @PathVariable(name = "id") long id) {
        return new ResponseEntity<>(shopService.update(shopSaveDTO, id), HttpStatus.OK);
    }

    @PutMapping("/remove/{id}")
    @CrossOrigin(origins = "http://localhost:3000")
    public void delete(@PathVariable(name = "id") long id) {
        shopService.delete(id);
    }

    @GetMapping
    @CrossOrigin(origins = "http://localhost:3000")
    public List<ShopDTO> get(@RequestParam(required = false) String keyword, Principal principal) {
        return shopService.get(keyword, principal);
    }
}

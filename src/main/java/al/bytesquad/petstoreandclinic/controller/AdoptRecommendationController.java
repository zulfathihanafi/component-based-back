package al.bytesquad.petstoreandclinic.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import al.bytesquad.petstoreandclinic.entity.AdoptablePet;
import al.bytesquad.petstoreandclinic.service.AdoptablePetService;

@RestController
@RequestMapping("/adoptRecommendation")
@CrossOrigin(origins = "http://localhost:3000")
public class AdoptRecommendationController {

    @Autowired
    private AdoptablePetService adoptablePetService;

    @GetMapping("/pet")
    public ResponseEntity<?> recommendPet(
        @RequestParam(required = true) String species,
        @RequestParam(required = true) String breed
    ){
        AdoptablePet recommendedPet = adoptablePetService.findPetBySpeciesAndBreed(species, breed);
        if(recommendedPet != null) {
            return new ResponseEntity<>(recommendedPet, HttpStatus.OK);
        } 
            String message = "The species " + species + " and breed " + breed + " is not available";
            return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
        
    }

    @GetMapping("/randomPetBySpecies")
    public List<AdoptablePet> get(@RequestParam(required = false) String keyword, Principal principal) {
        return adoptablePetService.getList();
    }

    @GetMapping("/pets")
    public ResponseEntity<List<AdoptablePet>> getPetsBySpecies(
            @RequestParam(required = true) String species
    ) {
        List<AdoptablePet> pets = adoptablePetService.findPetsBySpecies(species);
        return ResponseEntity.ok(pets);
    }

    @GetMapping("/suggestedPets")
    public ResponseEntity<List<AdoptablePet>> suggestPetsBasedOnSpeciesAndBusyState(
            @RequestParam(required = true) String species,
            @RequestParam(required = true) boolean busy
    ) {
        // Your logic to suggest pets based on both species and the busy state
        // You can call your service method with both parameters here

        List<AdoptablePet> suggestedPets = adoptablePetService.suggestPetsByBehavior(species, busy);

        if (!suggestedPets.isEmpty()) {
            return ResponseEntity.ok(suggestedPets);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }  

    @GetMapping("/suggestedPetsByGender")
    public ResponseEntity<List<AdoptablePet>> suggestPetsBasedOnGender(
            @RequestParam(required = true) String species,
            @RequestParam(required = false) String gender,
            @RequestParam(required = true) boolean busy
    ) {
        List<AdoptablePet> suggestedPets = adoptablePetService.suggestPetsByGender(species, gender, busy);

        if (!suggestedPets.isEmpty()) {
            return ResponseEntity.ok(suggestedPets);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

     @GetMapping("/suggestedPetsByBreed")
    public ResponseEntity<List<AdoptablePet>> suggestPetsBasedOnBreed(
            @RequestParam(required = true) String species,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String breed,
            @RequestParam(required = true) boolean busy
    ) {
        List<AdoptablePet> suggestedPets = adoptablePetService.suggestPetsByBreed(species, gender,breed, busy);

        if (!suggestedPets.isEmpty()) {
            return ResponseEntity.ok(suggestedPets);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

}

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
    public ResponseEntity<AdoptablePet> getRandomPets(@RequestParam(required = true) String species) {
        AdoptablePet randomPet = adoptablePetService.recommendRandomPetBySpecies(species);

        if (randomPet != null) {
            return ResponseEntity.ok(randomPet);
        } 
            return ResponseEntity.notFound().build();
        
    }

     @GetMapping("/recommendation")
    public ResponseEntity<?> suggestPetsBasedOnBreed(
            @RequestParam(required = true) String species,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String breed,
            @RequestParam(required = true) boolean busy
    ) {
        List<AdoptablePet> suggestedPets = adoptablePetService.suggestPetsByUserPreference(species, gender,breed, busy);

        if (!suggestedPets.isEmpty()) {
            return ResponseEntity.ok(suggestedPets);
        }
        String message = "The species " + species + " and breed " + breed + " is not available";
        return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
    }

}

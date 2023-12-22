package al.bytesquad.petstoreandclinic.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import al.bytesquad.petstoreandclinic.entity.AdoptablePet;
import al.bytesquad.petstoreandclinic.payload.entityDTO.AdoptablePetDTO;
import al.bytesquad.petstoreandclinic.repository.AdoptablePetRepository;
import al.bytesquad.petstoreandclinic.service.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

@Service
public class AdoptablePetService {
    
    private final AdoptablePetRepository adoptablePetRepository;
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;

    

    @Autowired
    public AdoptablePetService(AdoptablePetRepository adoptablePetRepository,ModelMapper modelMapper,ObjectMapper objectMapper) {
        this.adoptablePetRepository = adoptablePetRepository;
        this.modelMapper = modelMapper;
        this.objectMapper = objectMapper;

        modelMapper.addMappings(new PropertyMap<AdoptablePet, AdoptablePetDTO>() {
            @Override
            protected void configure() {
                map().setId(source.getId());
            }
        });
    }

    // get all adoptable pet list
    public List<AdoptablePet> getList(){
        return adoptablePetRepository.findAll();
    }

    // get all adoptable pet id
    public AdoptablePet getPetById(Long id){
        return adoptablePetRepository.findById(id).orElse(null);
    }

    public AdoptablePet create(@RequestBody String adoptablePetString) throws JsonProcessingException{
        AdoptablePetDTO adoptablePetDTO = objectMapper.readValue(adoptablePetString, AdoptablePetDTO.class);
        AdoptablePet adoptablePet = modelMapper.map(adoptablePetDTO, AdoptablePet.class);
        return adoptablePetRepository.save(adoptablePet);
    }

    public AdoptablePet update(@RequestBody String adoptablePetString, long id) throws JsonProcessingException{
        AdoptablePetDTO adoptablePetDTO = objectMapper.readValue(adoptablePetString, AdoptablePetDTO.class);
        AdoptablePet adoptablePet = adoptablePetRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException(adoptablePetString, adoptablePetString, 0));
        adoptablePet.setName(adoptablePetDTO.getName());
        adoptablePet.setSpecies(adoptablePetDTO.getSpecies());
        adoptablePet.setBreed(adoptablePetDTO.getBreed());
        adoptablePet.setGender(adoptablePetDTO.getGender());
        adoptablePet.setAge(adoptablePetDTO.getAge());
        adoptablePet.setColour(adoptablePetDTO.getColour());
        adoptablePet.setSize(adoptablePetDTO.getSize());
        adoptablePet.setBehavior(adoptablePetDTO.getBehavior());
        return adoptablePetRepository.save(adoptablePet);
    }

    public void delete(long id){
        adoptablePetRepository.deleteById(id);
    }

    public AdoptablePet recommendPet(String species, String breed, String colour, String size){
        colour = (colour != null && colour.isEmpty()) ? null : colour;
        size = (size != null && size.isEmpty()) ? null : size;
        return adoptablePetRepository.findBySpeciesAndBreed(species, breed);
    }

    public List<AdoptablePet> getPetsBySpecies(String species) {
        return adoptablePetRepository.findBySpecies(species);
    }
    

    public Long findPetIdBySpeciesAndBreed(String species, String breed) {
        AdoptablePet pet = adoptablePetRepository.findBySpeciesAndBreed(species, breed);
        if (pet != null) {
            return pet.getId();
        }
        return null;
    }

    public AdoptablePet findPetBySpeciesAndBreed(String species, String breed) {
        return adoptablePetRepository.findBySpeciesAndBreed(species, breed);
    }

    public List<AdoptablePet> findPetsBySpecies(String species){
        return adoptablePetRepository.findBySpecies(species);
    }

    public AdoptablePet recommendRandomPetBySpecies(String species) {
        List<AdoptablePet> pets = adoptablePetRepository.findBySpecies(species);
        System.out.println(pets+"petssss");
        if (!pets.isEmpty()) {
            // Get a random index within the list size
            int randomIndex = new Random().nextInt(pets.size());
            return pets.get(randomIndex);
        }
        return null; // Or you can throw an exception if no pets are found for that species
    }

    public List<AdoptablePet> suggestPetsByBehavior(String species, boolean isBusy) {
        List<AdoptablePet> allPets = adoptablePetRepository.findBySpecies(species);
        List<AdoptablePet> suggestedPets = new ArrayList<>();
    
        for (AdoptablePet pet : allPets) {
            String behavior = pet.getBehavior();
    
            if ((isBusy && (hasModerateBehavior(behavior))) ||
                (!isBusy && hasEnergeticBehavior(behavior))) {
                suggestedPets.add(pet);
            }
        }
    
        return suggestedPets;
    }
    
    private boolean hasEnergeticBehavior(String behavior) {
        return behavior != null && behavior.toLowerCase().contains("energetic");
    }
    
    private boolean hasModerateBehavior(String behavior) {
        return behavior != null && behavior.toLowerCase().contains("moderately");
    }

    public List<AdoptablePet> suggestPetsByGender(String species, String gender, boolean isBusy) {
        List<AdoptablePet> allPets = adoptablePetRepository.findBySpecies(species);
        List<AdoptablePet> suggestedPets = new ArrayList<>();
    
        for (AdoptablePet pet : allPets) {
            String petGender = pet.getGender();
            String behavior = pet.getBehavior();
    
            boolean isMatchingBehavior = (isBusy && (hasModerateBehavior(behavior))) ||
                (!isBusy && hasEnergeticBehavior(behavior));
    
            boolean isMatchingGender = (gender == null || gender.equalsIgnoreCase(petGender));
    
            if (isMatchingBehavior && isMatchingGender) {
                suggestedPets.add(pet);
            }
        }
    
        return suggestedPets;
    }

    public List<AdoptablePet> suggestPetsByBreed(String species, String gender, String breed, boolean isBusy) {
        List<AdoptablePet> allPets = adoptablePetRepository.findBySpecies(species);
        List<AdoptablePet> suggestedPets = new ArrayList<>();
    
        for (AdoptablePet pet : allPets) {
            String petGender = pet.getGender();
            String behavior = pet.getBehavior();
            String petBreed = pet.getBreed(); // Rename this variable to avoid conflicts
    
            boolean isMatchingBehavior = (isBusy && (hasModerateBehavior(behavior))) ||
                                         (!isBusy && hasEnergeticBehavior(behavior));
    
            boolean isMatchingGender = (gender == null || gender.equalsIgnoreCase(petGender));
            boolean isMatchingBreed = (breed == null || breed.equalsIgnoreCase(petBreed));
    
            if (isMatchingBehavior && isMatchingGender && isMatchingBreed) {
                suggestedPets.add(pet);
            }
        }
    
        return suggestedPets;
    }
    
    
    
}

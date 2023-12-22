package al.bytesquad.petstoreandclinic.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import al.bytesquad.petstoreandclinic.entity.AdoptablePet;

@Repository
public interface AdoptablePetRepository extends JpaRepository<AdoptablePet, Long> {

    AdoptablePet findBySpeciesAndBreed(String species, String breed);
    List<AdoptablePet> findBySpecies(String species);
}

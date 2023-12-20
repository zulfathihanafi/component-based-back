package al.bytesquad.petstoreandclinic.repository;

import al.bytesquad.petstoreandclinic.entity.PetServices;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends JpaRepository<PetServices, Long>, JpaSpecificationExecutor<PetServices> {
}
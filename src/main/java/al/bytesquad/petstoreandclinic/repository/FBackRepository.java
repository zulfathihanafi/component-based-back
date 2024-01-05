package al.bytesquad.petstoreandclinic.repository;

import al.bytesquad.petstoreandclinic.entity.*;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface FBackRepository extends JpaRepository<FBack, Long>, JpaSpecificationExecutor<FBack> {
    List<FBack> findByMonth(String month);
    // List<FBack> findByMonthAndShopId(String month, Long shopId);
}

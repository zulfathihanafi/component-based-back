package al.bytesquad.petstoreandclinic.repository;

import al.bytesquad.petstoreandclinic.entity.FeedbackEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackRepository extends JpaRepository<FeedbackEntity, Long>, JpaSpecificationExecutor<FeedbackEntity> {
    Page<FeedbackEntity> findAllByShop(Pageable pageable, long shopId);
}

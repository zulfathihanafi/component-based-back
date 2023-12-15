package al.bytesquad.petstoreandclinic.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.transaction.Transactional;
import al.bytesquad.petstoreandclinic.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long>{
    
    // @Transactional
    // void deleteByTutorialId(long tutorialId);
}

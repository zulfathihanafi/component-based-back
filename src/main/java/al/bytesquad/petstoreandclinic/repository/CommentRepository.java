package al.bytesquad.petstoreandclinic.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.transaction.Transactional;
import al.bytesquad.petstoreandclinic.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long>{
    
    List<Comment> findByForumId(Long forumID);
    
    @Transactional
    void deleteByForumId(long forumID);

    // @Transactional
    // void deleteByTutorialId(long tutorialId);
}

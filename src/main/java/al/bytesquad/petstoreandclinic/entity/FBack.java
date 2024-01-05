package al.bytesquad.petstoreandclinic.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Table(name = "feedback")
@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class FBack {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tutorial_generator")
    private long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    // @ManyToOne
    // @JoinColumn(name = "shop_id")
    // @LazyCollection(LazyCollectionOption.FALSE)
    // @JsonBackReference
    // private Shop shop;

    @Column(name = "month", nullable = false)
    private String month;    
    
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message")
    private String message;

    // @Column(name = "sentiment")
    // private String sentiment;


}  

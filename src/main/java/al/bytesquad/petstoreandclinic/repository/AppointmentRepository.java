package al.bytesquad.petstoreandclinic.repository;

import al.bytesquad.petstoreandclinic.entity.Appointment;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long>, JpaSpecificationExecutor<Appointment> {
    List<Appointment> findByPetId(Long petId);

    List<Appointment> findByDoctorId(Long doctorId);

    List<Appointment> findByClientId(Long clientId);

    List<Appointment> findByDoctorIdAndStartTimeBetweenAndFinishTimeBetween(
            Long doctorId, Date startTime, Date endTime, Date startTime2, Date endTime2);

    List<Appointment> findByDoctorIdAndStartTimeBetweenAndFinishTimeBetweenAndIdNot(
            Long doctorId, Date startTime1, Date finishTime1, Date startTime2, Date finishTime2, Long id);

}

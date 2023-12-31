package al.bytesquad.petstoreandclinic.service;

import al.bytesquad.petstoreandclinic.entity.*;
import al.bytesquad.petstoreandclinic.payload.JWTAuthResponse;
import al.bytesquad.petstoreandclinic.payload.LoginDTO;
import al.bytesquad.petstoreandclinic.payload.StatusResponse;
import al.bytesquad.petstoreandclinic.repository.*;
import al.bytesquad.petstoreandclinic.secuity.JWTProvider;
import al.bytesquad.petstoreandclinic.secuity.UserDetailsImpl;
import al.bytesquad.petstoreandclinic.service.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JWTProvider provider;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SessionRepository sessionRepository;
    private final ObjectMapper objectMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AdminRepository adminRepository;
    private final ManagerRepository managerRepository;
    private final DoctorRepository doctorRepository;
    private final ReceptionistRepository receptionistRepository;
    private final ClientRepository clientRepository;

    public AuthService(AuthenticationManager authenticationManager, JWTProvider provider,
                       UserRepository userRepository, RoleRepository roleRepository,
                       SessionRepository sessionRepository, ObjectMapper objectMapper,
                       AdminRepository adminRepository,
                       ManagerRepository managerRepository,
                       DoctorRepository doctorRepository,
                       ReceptionistRepository receptionistRepository,
                       ClientRepository clientRepository) {
        this.authenticationManager = authenticationManager;
        this.provider = provider;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.sessionRepository = sessionRepository;
        this.objectMapper = objectMapper;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.adminRepository = adminRepository;
        this.managerRepository = managerRepository;
        this.doctorRepository = doctorRepository;
        this.receptionistRepository = receptionistRepository;
        this.clientRepository = clientRepository;
    }

    public ResponseEntity<?> authenticate(String jsonString) throws Exception {
        LoginDTO loginDTO = objectMapper.readValue(jsonString, LoginDTO.class);


        String email = loginDTO.getEmail();
        String password = loginDTO.getPassword();
        
        Authentication authentication = authenticationManager
        .authenticate(new UsernamePasswordAuthenticationToken(email, password));
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
        .map(item -> item.getAuthority())
        .collect(Collectors.toList());

        ResponseCookie jwtCookie = provider.generateJwtCookie(userDetails);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
        .body(userDetails);
    }

    // public ResponseEntity<JWTAuthResponse> refreshToken() {
    //     Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    //     String accessToken = provider.generateToken(authentication);
    //     String refreshToken = provider.generateRefreshToken(authentication);
    //     return ResponseEntity.ok(new JWTAuthResponse(accessToken, refreshToken));
    // }

    public ResponseEntity<StatusResponse> auth(String sessionId) {
        Session session = sessionRepository.findSessionBySessionId(sessionId);
        if(session == null)
            return ResponseEntity.ok(new StatusResponse(null, 404, null, "Logout!", null, null));
        return ResponseEntity.ok(new StatusResponse(session.getSessionId(), 200, session.getUser().getSecondId(), "Success!", null, session.getRole()));
    }

    public ResponseEntity<?> signOut(){
        ResponseCookie cookie = provider.getCleanJwtCookie();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
            .body("You've been signed out!");
    }
}

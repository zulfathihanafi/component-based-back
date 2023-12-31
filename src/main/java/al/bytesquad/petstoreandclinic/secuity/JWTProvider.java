package al.bytesquad.petstoreandclinic.secuity;

import al.bytesquad.petstoreandclinic.service.exception.APIException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.security.Key;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import java.util.Date;

@Component
public class JWTProvider {

    @Value("${app.jwt-secret}")
    private String jwtSecret;
    @Value("${app.jwt-expiration-milliseconds}")
    private int jwtExpirationinMs;
    @Value("${app.jwt-refresh-expiration-millisedonds}")
    private int refreshExpirationMilliseconds;
    @Value("${app.jwt-cookie-name}")
    private String jwtCookie;

    private Key key() {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
  }
private static final Logger logger = LoggerFactory.getLogger(JWTProvider.class);
    public String generateToken(String email) {
        // String email = authentication.getName();
        System.out.println("In Email"+email);
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + jwtExpirationinMs);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(expireDate)
                .signWith(key(),SignatureAlgorithm.HS256)
                .compact();
    }

    public String getEmailFromJWT(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
               .parseClaimsJws(token).getBody().getSubject();
    }

    public String generateRefreshToken(Authentication authentication) {
        return Jwts.builder()
                .setSubject(authentication.getName())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpirationMilliseconds))
                .signWith(key(),SignatureAlgorithm.HS256)
                .compact();
    }
    public ResponseCookie generateJwtCookie(UserDetailsImpl userPrincipal) {
        String jwt = generateToken(userPrincipal.getUsername());
        ResponseCookie cookie = ResponseCookie.from(jwtCookie, jwt).path("/").maxAge(24 * 60 * 60).httpOnly(true).build();
        return cookie;
    }
    public boolean validateJwtToken(String authToken) {
        System.out.println(authToken);
        try {
          Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
          return true;
        } catch (MalformedJwtException e) {
          logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
          logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
          logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
          logger.error("JWT claims string is empty: {}", e.getMessage());
        }
    
        return false;
      }
    public String getJwtFromCookies(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, jwtCookie);
        if (cookie != null) {
        return cookie.getValue();
        } else {
        return null;
        }
    }
    public ResponseCookie getCleanJwtCookie() {
      ResponseCookie cookie = ResponseCookie.from(jwtCookie, null).path("/").build();
      return cookie;
    }
}

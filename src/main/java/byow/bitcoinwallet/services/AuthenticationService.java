package byow.bitcoinwallet.services;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

import static org.springframework.security.crypto.bcrypt.BCrypt.checkpw;

@Service
@Lazy
public class AuthenticationService {
    public String hashPassword(String password) {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(10, new SecureRandom());
        return bCryptPasswordEncoder.encode(password);
    }

    public boolean checkPassword(String password, String hashedPassword) {
        return checkpw(password, hashedPassword);
    }
}

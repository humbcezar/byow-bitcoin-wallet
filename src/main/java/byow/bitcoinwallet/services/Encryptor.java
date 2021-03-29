package byow.bitcoinwallet.services;

import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Component;

@Component
public class Encryptor {

    public String encrypt(String text, String password) {
        String salt = KeyGenerators.string().generateKey();
        TextEncryptor encryptor = Encryptors.delux(password, salt);
        return salt.concat(encryptor.encrypt(text));
    }

    public String decrypt(String text, String password) {
        String salt = text.substring(0, 16);
        TextEncryptor encryptor = Encryptors.delux(password, salt);
        return encryptor.decrypt(text.substring(16));
    }
}

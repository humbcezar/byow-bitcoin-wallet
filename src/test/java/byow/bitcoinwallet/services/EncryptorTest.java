package byow.bitcoinwallet.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles("test")
public class EncryptorTest {
    private Encryptor encryptor;

    @BeforeEach
    public void setup() {
        encryptor = new Encryptor();
    }

    @Test
    public void encryptAndDecrypt() {
        String textToEncrypt = "tqasqwerqwerqwskei3849302l";
        String password = "akeid838204kdkksajei299";
        String encryptedText = encryptor.encrypt(textToEncrypt, password);

        String decryptedText = encryptor.decrypt(encryptedText, password);
        assertEquals(textToEncrypt, decryptedText);
    }

    @Test
    public void encryptAndDecryptWithWrongPassword() {
        String textToEncrypt = "aswksi8889k";
        String password = "id889iiedkie8";
        String encryptedText = encryptor.encrypt(textToEncrypt, password);

        assertThrows(IllegalStateException.class, () -> encryptor.decrypt(encryptedText, "akie"));
    }

    @Test
    public void encryptAndDecryptWithEmptyPassword() {
        String textToEncrypt = "akeiidi889398893983";
        String password = "";
        String encryptedText = encryptor.encrypt(textToEncrypt, password);

        String decryptedText = encryptor.decrypt(encryptedText, password);
        assertEquals(textToEncrypt, decryptedText);
    }

    @Test
    public void encryptWithEmptyPasswordAndDecryptWithWrongPassword() {
        String textToEncrypt = "kdkkdidididicic99dd9d9";
        String password = "";
        String encryptedText = encryptor.encrypt(textToEncrypt, password);

        assertThrows(IllegalStateException.class, () -> encryptor.decrypt(encryptedText, "akie"));
    }

    @Test
    public void encryptAndDecryptWithWrongEmptyPassword() {
        String textToEncrypt = "kk";
        String password = "ajdjjeijd";
        String encryptedText = encryptor.encrypt(textToEncrypt, password);

        assertThrows(IllegalStateException.class, () -> encryptor.decrypt(encryptedText, ""));
    }
}

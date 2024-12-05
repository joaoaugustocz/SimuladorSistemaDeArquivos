import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtils {
    // Chave secreta (deve ter 16 bytes para AES-128)
    private static final byte[] chave = "1234567890123456".getBytes(); // Exemplo de chave

    public static SecretKey getSecretKey() {
        return new SecretKeySpec(chave, "AES");
    }
}
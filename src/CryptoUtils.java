import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtils {
    private static final String CHAVE = "1234567890123456"; // 16 bytes para AES-128

    public static SecretKey getSecretKey() {
        return new SecretKeySpec(CHAVE.getBytes(), "AES");
    }
}

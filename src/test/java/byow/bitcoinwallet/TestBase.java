package byow.bitcoinwallet;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testfx.framework.junit5.ApplicationExtension;

@SpringBootTest
@ExtendWith(ApplicationExtension.class)
@ActiveProfiles("test")
abstract public class TestBase {
}

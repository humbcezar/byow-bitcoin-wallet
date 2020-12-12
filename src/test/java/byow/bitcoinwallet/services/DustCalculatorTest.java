package byow.bitcoinwallet.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class DustCalculatorTest {
    @Autowired
    private DustCalculator dustCalculator;

    @Test
    public void isDust() {
        assertTrue(dustCalculator.isDust(200));
        assertTrue(dustCalculator.isDust(293));
    }

    @Test
    public void isNotDust() {
        assertFalse(dustCalculator.isDust(300));
        assertFalse(dustCalculator.isDust(294));
    }
}
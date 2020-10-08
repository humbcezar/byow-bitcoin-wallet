package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.MockitoAnnotations.initMocks;

@ActiveProfiles("test")
@SpringBootTest
public class AddressGeneratorTest {
    @Autowired
    private SegwitAddressGenerator addressGenerator;

    @Autowired
    private WalletCreator walletCreator;

    private Wallet wallet;

    @MockBean
    ApplicationEventPublisher applicationEventPublisher;

    @BeforeEach
    void setUp() {
        initMocks(this);
        walletCreator.setApplicationEventPublisher(this.applicationEventPublisher);
        wallet = walletCreator.create(
                "test",
                "gap print mobile track security horn polar female inhale liberty general benefit",
                ""
        );
    }

    @Test
    public void testGenerateSegwitAddress() {
        String address = addressGenerator.generate(wallet);
        String expectedAddress = "bc1qzvfaa0r54fnlfsdv745h2npe7uuwph0892323l";
        assertEquals(expectedAddress, address);
    }
}

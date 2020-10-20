package byow.bitcoinwallet.controllers;

import byow.bitcoinwallet.services.SeedGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CreateWalletDialogController extends GenerateWalletDialogController {

    @Autowired
    private SeedGenerator seedGenerator;

    public void generateMnemonicSeed() {
        mnemonicSeed.setText(seedGenerator.generateMnemonicSeed());
    }
}

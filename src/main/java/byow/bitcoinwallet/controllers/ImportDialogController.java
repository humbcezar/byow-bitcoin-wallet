package byow.bitcoinwallet.controllers;

import org.springframework.stereotype.Component;

@Component
public class ImportDialogController extends GenerateWalletDialogController {
    @Override
    public void createWallet() {
        walletCreator.create(walletName.getText(), mnemonicSeed.getText(), walletPassword.getText(), null);
    }
}

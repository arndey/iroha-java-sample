package jp.co.soramitsu.iroha2.sample;

import java.security.KeyPair;
import jp.co.soramitsu.iroha2.CryptoUtils;
import jp.co.soramitsu.iroha2.generated.datamodel.Name;
import jp.co.soramitsu.iroha2.generated.datamodel.domain.Id;

public class Constants {

    public static final KeyPair KEY_PAIR = CryptoUtils.keyPairFromHex(
        "7233bfc89dcbd68c19fde6ce6158225298ec1131b6a130d1aeb454c1ab5183c0",
        "9ac47abf59b356e0bd7dcbbbb4dec080e302156a48ca907e47cb6aea1d32719e"
    );

    public static final Id DEFAULT_DOMAIN_ID = new Id(new Name("wonderland"));
    public static final jp.co.soramitsu.iroha2.generated.datamodel.account.Id  ALICE_ACCOUNT_ID =
        new jp.co.soramitsu.iroha2.generated.datamodel.account.Id(
            new Name("alice"),
            DEFAULT_DOMAIN_ID
        );
}

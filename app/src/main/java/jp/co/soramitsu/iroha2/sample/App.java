package jp.co.soramitsu.iroha2.sample;

import static jp.co.soramitsu.iroha2.sample.Constants.ALICE_ACCOUNT_ID;
import static jp.co.soramitsu.iroha2.sample.Constants.KEY_PAIR;

import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import jp.co.soramitsu.iroha2.AdminIroha2Client;
import jp.co.soramitsu.iroha2.PeerStatus;
import jp.co.soramitsu.iroha2.generated.datamodel.Name;
import jp.co.soramitsu.iroha2.generated.datamodel.domain.Domain;
import jp.co.soramitsu.iroha2.generated.datamodel.domain.Id;
import jp.co.soramitsu.iroha2.generated.datamodel.transaction.VersionedTransaction;
import jp.co.soramitsu.iroha2.query.QueryAndExtractor;
import jp.co.soramitsu.iroha2.query.QueryBuilder;
import jp.co.soramitsu.iroha2.transaction.TransactionBuilder;

public class App {

    public static void main(String[] args) throws Exception {
        final Id newDomainId = new Id(new Name("new_domain_name"));
        final URL peerUrl = new URL("http://127.0.0.1:8080");
        final URL telemetryUrl = new URL("http://127.0.0.1:8180");

        try (AdminIroha2Client client = new AdminIroha2Client(peerUrl, telemetryUrl, true)) {
            final Integer health = client.healthAsync().get();
            System.out.println("Health: " + health);

            final PeerStatus status = client.statusAsync().get();
            System.out.printf(
                "Current blocks: %d, peers: %d, transactions: %d%n",
                status.getBlocks(), status.getPeers(), status.getTxs()
            );

            final VersionedTransaction transaction = TransactionBuilder.Companion
                .builder()
                .account(ALICE_ACCOUNT_ID)
                .registerDomain(newDomainId)
                .buildSigned(KEY_PAIR);
            client.sendTransaction(transaction).get(10, TimeUnit.SECONDS);

            final QueryAndExtractor<Domain> query = QueryBuilder
                .findDomainById(newDomainId)
                .account(ALICE_ACCOUNT_ID)
                .buildSigned(KEY_PAIR);
            final CompletableFuture<Domain> future = client.sendQueryAsync(query);
            final Domain domain = future.get(10, TimeUnit.SECONDS);

            System.out.println("Result domain: " + domain);
        }
    }
}

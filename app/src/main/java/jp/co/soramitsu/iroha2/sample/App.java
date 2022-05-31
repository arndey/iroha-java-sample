package jp.co.soramitsu.iroha2.sample;

import static jp.co.soramitsu.iroha2.ExtensionsKt.toIrohaPublicKey;
import static jp.co.soramitsu.iroha2.sample.Constants.ALICE_ACCOUNT_ID;
import static jp.co.soramitsu.iroha2.sample.Constants.KEY_PAIR;

import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import jp.co.soramitsu.iroha2.AdminIroha2Client;
import jp.co.soramitsu.iroha2.Iroha2Client;
import jp.co.soramitsu.iroha2.PeerStatus;
import jp.co.soramitsu.iroha2.generated.crypto.PublicKey;
import jp.co.soramitsu.iroha2.generated.datamodel.Name;
import jp.co.soramitsu.iroha2.generated.datamodel.Value;
import jp.co.soramitsu.iroha2.generated.datamodel.account.Account;
import jp.co.soramitsu.iroha2.generated.datamodel.asset.AssetValueType;
import jp.co.soramitsu.iroha2.generated.datamodel.asset.DefinitionId;
import jp.co.soramitsu.iroha2.generated.datamodel.domain.Domain;
import jp.co.soramitsu.iroha2.generated.datamodel.domain.Id;
import jp.co.soramitsu.iroha2.generated.datamodel.metadata.Metadata;
import jp.co.soramitsu.iroha2.generated.datamodel.transaction.VersionedTransaction;
import jp.co.soramitsu.iroha2.query.QueryAndExtractor;
import jp.co.soramitsu.iroha2.query.QueryBuilder;
import jp.co.soramitsu.iroha2.transaction.TransactionBuilder;
import org.junit.Assert;

public class App {

    public static void main(String[] args) throws Exception {
        final Id newDomainId = new Id(new Name("new_domain_name"));
        final DefinitionId definitionId = new DefinitionId(
            new Name("new_asset_definition"), newDomainId
        );
        final URL peerUrl = new URL("http://127.0.0.1:8080");
        final URL telemetryUrl = new URL("http://127.0.0.1:8180");

        final Value value = new Value.U128(BigInteger.TEN);
        final Name key = new Name("asset_metadata_key");

        try (AdminIroha2Client client = new AdminIroha2Client(peerUrl, telemetryUrl, true)) {
            final Integer health = client.healthAsync().get();
            System.out.println("Health: " + health);

            final PeerStatus status = client.statusAsync().get();
            System.out.printf(
                "Current blocks: %d, peers: %d, transactions: %d%n",
                status.getBlocks(), status.getPeers(), status.getTxs()
            );

            registerDomain(newDomainId, client);
            final Domain domain = queryDomain(newDomainId, client);
            System.out.println("Result domain: " + domain);

            registerAccount("some_account", newDomainId, client);
            final List<Account> accounts = queryAllAccounts(client);

            registerDomain(newDomainId, client);
            final List<Account> accountsAfter = queryAllAccounts(client);

            Assert.assertEquals(accounts.size(), accountsAfter.size());
        }
    }

    private static List<Account> queryAllAccounts(Iroha2Client client) throws Exception {
        final QueryAndExtractor<List<Account>> domainQuery = QueryBuilder
            .findAllAccounts()
            .account(ALICE_ACCOUNT_ID)
            .buildSigned(KEY_PAIR);
        final CompletableFuture<List<Account>> future = client.sendQueryAsync(domainQuery);
        return future.get(10, TimeUnit.SECONDS);
    }

    private static void registerAccount(
        String name,
        Id domainId,
        Iroha2Client client
    ) throws Exception {
        final List<PublicKey> signatories = new ArrayList<>();
        signatories.add(toIrohaPublicKey(KEY_PAIR.getPublic()));

        final VersionedTransaction assetTransaction = TransactionBuilder.Companion
            .builder()
            .account(ALICE_ACCOUNT_ID)
            .registerAccount(
                new jp.co.soramitsu.iroha2.generated.datamodel.account.Id(
                    new Name(name), domainId
                ),
                signatories
            ).buildSigned(KEY_PAIR);
        client.sendTransaction(assetTransaction).get(10, TimeUnit.SECONDS);
    }

    private static void registerAsset(
        DefinitionId definitionId,
        AssetValueType type,
        Iroha2Client client
    ) throws Exception {
        final VersionedTransaction assetTx = TransactionBuilder.Companion
            .builder()
            .account(ALICE_ACCOUNT_ID)
            .registerAsset(definitionId, type)
            .buildSigned(KEY_PAIR);
        client.sendTransaction(assetTx).get(10, TimeUnit.SECONDS);
    }

    private static void registerDomain(Id domainId, Iroha2Client client) throws Exception {
        final VersionedTransaction domainTx = TransactionBuilder.Companion
            .builder()
            .account(ALICE_ACCOUNT_ID)
            .registerDomain(domainId)
            .buildSigned(KEY_PAIR);
        client.sendTransaction(domainTx).get(10, TimeUnit.SECONDS);
    }

    private static Domain queryDomain(Id domainId, Iroha2Client client) throws Exception {
        final QueryAndExtractor<Domain> domainQuery = QueryBuilder
            .findDomainById(domainId)
            .account(ALICE_ACCOUNT_ID)
            .buildSigned(KEY_PAIR);
        final CompletableFuture<Domain> future = client.sendQueryAsync(domainQuery);
        return future.get(10, TimeUnit.SECONDS);
    }
}


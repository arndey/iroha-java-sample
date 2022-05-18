package jp.co.soramitsu.iroha2.sample;

import static jp.co.soramitsu.iroha2.sample.Constants.ALICE_ACCOUNT_ID;
import static jp.co.soramitsu.iroha2.sample.Constants.KEY_PAIR;

import java.math.BigInteger;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import jp.co.soramitsu.iroha2.AdminIroha2Client;
import jp.co.soramitsu.iroha2.Iroha2Client;
import jp.co.soramitsu.iroha2.PeerStatus;
import jp.co.soramitsu.iroha2.generated.datamodel.Name;
import jp.co.soramitsu.iroha2.generated.datamodel.Value;
import jp.co.soramitsu.iroha2.generated.datamodel.asset.AssetValueType;
import jp.co.soramitsu.iroha2.generated.datamodel.asset.DefinitionId;
import jp.co.soramitsu.iroha2.generated.datamodel.domain.Domain;
import jp.co.soramitsu.iroha2.generated.datamodel.domain.Id;
import jp.co.soramitsu.iroha2.generated.datamodel.metadata.Metadata;
import jp.co.soramitsu.iroha2.generated.datamodel.transaction.VersionedTransaction;
import jp.co.soramitsu.iroha2.query.QueryAndExtractor;
import jp.co.soramitsu.iroha2.query.QueryBuilder;
import jp.co.soramitsu.iroha2.transaction.TransactionBuilder;

public class App {

    public static void main(String[] args) throws Exception {
        final Id newDomainId = new Id(new Name("new_domain_name"));
        final DefinitionId definitionId = new DefinitionId(
            new Name("new_asset_definition"), newDomainId
        );
        final URL peerUrl = new URL("http://127.0.0.1:8080");
        final URL telemetryUrl = new URL("http://127.0.0.1:8180");

        final Value assetMetadataValue = new Value.U128(BigInteger.TEN);
        final Name assetMetadataKey = new Name("asset_metadata_key");
        final Metadata metadata = new Metadata(new HashMap<Name, Value>() {{
            put(assetMetadataKey, assetMetadataValue);
        }});

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

            registerAsset(definitionId, new AssetValueType.Quantity(), metadata, client);
            final Value value = queryAssetDefinitionValue(definitionId, assetMetadataKey, client);
            System.out.println("Asset metadata key: " + value);
        }
    }

    private static void registerAsset(
        DefinitionId definitionId,
        AssetValueType type,
        Metadata metadata,
        Iroha2Client client
    ) throws Exception {
        final VersionedTransaction assetTransaction = TransactionBuilder.Companion
            .builder()
            .account(ALICE_ACCOUNT_ID)
            .registerAsset(definitionId, type, metadata)
            .buildSigned(KEY_PAIR);
        client.sendTransaction(assetTransaction).get(10, TimeUnit.SECONDS);
    }

    private static void registerDomain(Id domainId, Iroha2Client client) throws Exception {
        final VersionedTransaction domainTransaction = TransactionBuilder.Companion
            .builder()
            .account(ALICE_ACCOUNT_ID)
            .registerDomain(domainId)
            .buildSigned(KEY_PAIR);
        client.sendTransaction(domainTransaction).get(10, TimeUnit.SECONDS);
    }

    private static Domain queryDomain(Id domainId, Iroha2Client client) throws Exception {
        final QueryAndExtractor<Domain> domainQuery = QueryBuilder
            .findDomainById(domainId)
            .account(ALICE_ACCOUNT_ID)
            .buildSigned(KEY_PAIR);
        final CompletableFuture<Domain> future = client.sendQueryAsync(domainQuery);
        return future.get(10, TimeUnit.SECONDS);
    }

    private static Value queryAssetDefinitionValue(
        DefinitionId definitionId,
        Name key,
        Iroha2Client client
    ) throws Exception {
        final QueryAndExtractor<Value> assetDefinitionValueQuery = QueryBuilder
            .findAssetDefinitionKeyValueByIdAndKey(definitionId, key)
            .account(ALICE_ACCOUNT_ID)
            .buildSigned(KEY_PAIR);
        final CompletableFuture<Value> future = client.sendQueryAsync(assetDefinitionValueQuery);

        return future.get(10, TimeUnit.SECONDS);
    }
}

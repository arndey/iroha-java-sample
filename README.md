# Iroha SDK Java 8 sample

## Build project
From project root you can run `./gradlew build`

## Setup Iroha
To lift one Iroha peer you can use command
`docker compose -f docker/docker_compose.yml up`

Take a look at the files [config.json](docker/config.json) and [genesis.json](docker/genesis.json) as well.
Here you can find some Iroha settings and initial data.

`API_URL` and `TELEMETRY_URL` specified in [config.json](docker/config.json) you can also find in [App](app/src/main/).main function as `peerUrl` and `telemetryUrl` variables.
Domain `wonderland` and `alice` key pair for example.

Then you can run [App.java](app/src/main/)

## Might be useful
To generate new key pair you can use CryptoUtils.generateKeyPair()
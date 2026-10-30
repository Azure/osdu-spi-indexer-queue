# OSDU Indexer Queue Service for Azure

[![Release](https://img.shields.io/github/v/release/Azure/osdu-spi-indexer-queue)](https://github.com/Azure/osdu-spi-indexer-queue/releases)
[![Validate](https://github.com/Azure/osdu-spi-indexer-queue/actions/workflows/validate.yml/badge.svg?branch=main)](https://github.com/Azure/osdu-spi-indexer-queue/actions/workflows/validate.yml)
[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

> [!NOTE]
> The service code comes from the [OSDU community upstream](https://community.opengroup.org/osdu/platform/system/indexer-queue); the [OSDU documentation](https://osdu.pages.opengroup.org/platform/system/indexer-queue/) covers the service.

Indexer Queue moves record and schema change events from Service Bus to the Indexer, so that what Storage and Schema write becomes searchable.

## At a glance

| | |
|---|---|
| API | None; the service consumes Service Bus messages and serves no endpoints |
| Health | `:8081/actuator/health` |
| Consumes | `recordstopic`, `reindextopic`, and `schemachangedtopic` in each partition's Service Bus namespace |
| Calls | Indexer `index-worker` and `schema-worker` task handlers |
| Depends on | Partition, Indexer |
| Azure resources | Service Bus (per partition), Key Vault |
| Deployed by | [OSDU SPI Stack](https://github.com/Azure/osdu-spi-stack) (`software/stacks/osdu/services/indexer-queue.yaml`) |

## Repository layout

[CONTRIBUTING.md](CONTRIBUTING.md) explains where each kind of change belongs. There is no `provider/` directory and no shared core module: the Azure modules sit at the top level, and this repository owns them.

| Path | Owner | Contents |
|---|---|---|
| `indexer-queue-azure-enqueue/` | This repository | The service: a Service Bus consumer that calls the Indexer |
| `indexer-queue-azure-requeue/` | This repository | Azure Function that puts re-enqueue requests on a Service Bus topic; built, but not packaged into an image or deployed |
| `testing/indexer-queue-azure-enqueue/` | This repository | Azure integration tests |
| `pom.xml`, `testing/pom.xml` | OSDU upstream | Root and test aggregator poms; the `azure` profile adds both Azure modules |
| `.spi/service.yaml` | This repository | How CI deploys and tests the service on SPI Stack |

## Build

Requires Java 25 and Maven 3.6.3+. OSDU dependencies resolve from the public community registry through the settings file in `.mvn`. The root pom lists no modules by default, so the `azure` profile is required:

```bash
mvn --settings .mvn/community-maven.settings.xml -P azure clean install
```

The runnable jar lands at `indexer-queue-azure-enqueue/target/EnqueueFunction-*-spring-boot.jar`. It is the only jar the service image packages, and the same module upstream publishes.

## Configuration

SPI Stack sets the service's environment from two places: the shared `osdu-config` ConfigMap and the service's own entry in [`services/indexer-queue.yaml`](https://github.com/Azure/osdu-spi-stack/blob/main/software/stacks/osdu/services/indexer-queue.yaml). Those files are the contract; the tables below list what Indexer Queue actually reads from them.

**Shared, from `osdu-config`:**

| Variable | Purpose |
|---|---|
| `KEYVAULT_URI` | Central Key Vault |
| `SERVER_PORT` | HTTP port (`8080`) |
| `AAD_CLIENT_ID` | Application ID of the Indexer token audience, passed on as `AZURE_APP_RESOURCE_ID` below |

**Specific to Indexer Queue**, from `services/indexer-queue.yaml`:

| Variable and value on SPI Stack | Purpose |
|---|---|
| `SPRING_APPLICATION_NAME`<br>`indexer-queue` | Application name, used in logs |
| `AZURE_APP_RESOURCE_ID`<br>`$(AAD_CLIENT_ID)` | Token audience for calls to the Indexer |
| `AZURE_PAAS_WORKLOADIDENTITY_ISENABLED`<br>`true` | Authenticate to Azure with workload identity |
| `AZURE_ISTIOAUTH_ENABLED`<br>`true` | Set as for other services; this service does not read it |
| `PARTITION_API`<br>`http://partition/api/partition/v1` | Partition list and per-partition resource lookup |
| `INDEXER_WORKER_URL`<br>`http://indexer/api/indexer/v2/_dps/task-handlers/index-worker` | Indexer endpoint for record changes |
| `schema_worker_url`<br>`http://indexer/api/indexer/v2/_dps/task-handlers/schema-worker` | Indexer endpoint for schema changes |
| `AZURE_SERVICEBUS_TOPIC_NAME`<br>`recordstopic` | Record change topic |
| `AZURE_SERVICEBUS_TOPIC_SUBSCRIPTION`<br>`recordstopicsubscription` | Subscription on the record change topic |
| `AZURE_REINDEX_TOPIC_NAME`<br>`reindextopic` | Reindex topic, also the target for retries |
| `AZURE_REINDEX_TOPIC_SUBSCRIPTION`<br>`reindextopicsubscription` | Subscription on the reindex topic |
| `AZURE_SCHEMACHANGED_TOPIC_NAME`<br>`schemachangedtopic` | Schema change topic |
| `AZURE_SCHEMACHANGED_TOPIC_SUBSCRIPTION`<br>`schemachangedtopiceg` | Subscription on the schema change topic |
| `MAX_CONCURRENT_CALLS`<br>`32` | Messages handled at once per subscription |
| `EXECUTOR_N_THREADS`<br>`32` | Worker threads shared by all subscriptions |
| `MAX_LOCK_RENEW_DURATION_SECONDS`<br>`600` | How long a message lock is renewed while the Indexer call runs |
| `MAX_DELIVERY_COUNT`<br>`5` | Retries before a message is dead-lettered |

The service authenticates to Azure with workload identity, which injects `AZURE_CLIENT_ID` and a federated token; there are no client secrets. It calls the Indexer with a token from that identity for the `AZURE_APP_RESOURCE_ID` audience.

## Test

| Suite | Where | Runs in CI | Run it yourself |
|---|---|---|---|
| Unit | `indexer-queue-azure-enqueue`, `indexer-queue-azure-requeue` | Pull requests (Java Build) | `mvn ... install` from [Build](#build) |
| Acceptance | `testing/indexer-queue-azure-enqueue` | Pull requests (Deploy and Test) | `spi test indexer-queue` |

CI runs these on pull requests from this repository that change code. Documentation-only changes skip the build, and pull requests from forks build without deploying.

There is no acceptance module upstream, so `.spi/service.yaml` declares the Azure integration suite carried in `testing/` as this service's acceptance suite. It writes records through Storage, changes a schema, and polls Search until the records show up. The schema test only passes when schema-changed events reach the Indexer through this service. Against an environment you are connected to:

```bash
spi test indexer-queue
```

The suite takes a bearer token from `INTEGRATION_TESTER_ACCESS_TOKEN`, which CI and `spi test` supply. Without one it falls back to signing in as a service principal (`INTEGRATION_TESTER`, `TESTER_SERVICEPRINCIPAL_SECRET`). The Indexer's suites also exercise this service end to end, since the records they write through Storage reach the index only through it.

## Deploy

For a pull request from this repository that changes code, CI publishes the service image, `osdu-spi-indexer-queue`, to GHCR. When `.spi/service.yaml` declares a test suite, CI also publishes the suite image, `osdu-spi-indexer-queue-acceptance`, and the Deploy and Test lane borrows an SPI Stack environment, runs the new image there, proves it with those suites, and restores the environment's own image. The Validation Summary on the pull request shows whether that lane ran. This repository does not own infrastructure; SPI Stack does.

To try a build by hand on an environment you are connected to, pin it by digest and release the pin when done:

```bash
spi service pin indexer-queue --image ghcr.io/azure/osdu-spi-indexer-queue@sha256:<digest>
spi service reset indexer-queue
```

## Service notes

**Partition discovery.** The service lists partitions through the Partition service at startup and again every 60 seconds, and subscribes to each new partition's topics as it appears. A partition added to the environment is picked up without a restart.

**Retries.** When an Indexer call for a record change fails, the service completes the original message and schedules a copy on `reindextopic` with a growing delay, capped at 12 hours. A message retried more than `MAX_DELIVERY_COUNT` times, or one the Indexer marks as not retryable, goes to the dead-letter queue of the subscription it was read from. When the Indexer answers 404 because Storage holds no valid record, the message is dropped without a retry.

## License

Copyright © Microsoft Corporation

Licensed under the [Apache License 2.0](LICENSE).

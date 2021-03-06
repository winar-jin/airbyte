/*
 * MIT License
 *
 * Copyright (c) 2020 Airbyte
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.airbyte.test.acceptance;

import static io.airbyte.api.client.model.ConnectionSchedule.TimeUnitEnum.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import io.airbyte.api.client.AirbyteApiClient;
import io.airbyte.api.client.invoker.ApiClient;
import io.airbyte.api.client.invoker.ApiException;
import io.airbyte.api.client.model.CheckConnectionRead;
import io.airbyte.api.client.model.ConnectionCreate;
import io.airbyte.api.client.model.ConnectionIdRequestBody;
import io.airbyte.api.client.model.ConnectionRead;
import io.airbyte.api.client.model.ConnectionSchedule;
import io.airbyte.api.client.model.ConnectionStatus;
import io.airbyte.api.client.model.ConnectionSyncRead;
import io.airbyte.api.client.model.ConnectionUpdate;
import io.airbyte.api.client.model.DestinationCreate;
import io.airbyte.api.client.model.DestinationIdRequestBody;
import io.airbyte.api.client.model.DestinationRead;
import io.airbyte.api.client.model.SourceCreate;
import io.airbyte.api.client.model.SourceIdRequestBody;
import io.airbyte.api.client.model.SourceRead;
import io.airbyte.api.client.model.SourceSchema;
import io.airbyte.commons.json.Jsons;
import io.airbyte.commons.resources.MoreResources;
import io.airbyte.config.persistence.PersistenceConstants;
import io.airbyte.db.Database;
import io.airbyte.db.Databases;
import io.airbyte.test.utils.PostgreSQLContainerHelper;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.jooq.Record;
import org.jooq.Result;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

@SuppressWarnings("rawtypes")
// We order tests such that earlier tests test more basic behavior that is relied upon in later
// tests.
// e.g. We test that we can create a destination before we test whether we can sync data to it.
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AcceptanceTests {

  private static final Path AIRBYTE_LOCAL_ROOT = Path.of("/tmp/airbyte_local");
  private static final Path RELATIVE_PATH = Path.of("destination_csv/test");

  private static PostgreSQLContainer sourcePsql;

  private final AirbyteApiClient apiClient = new AirbyteApiClient(
      new ApiClient().setScheme("http")
          .setHost("localhost")
          .setPort(8001)
          .setBasePath("/api"));

  private List<UUID> sourceIds;
  private List<UUID> connectionIds;
  private List<UUID> destinationIds;
  private Path outputDir;
  private Path relativeDir;

  @BeforeAll
  public static void init() {
    sourcePsql = new PostgreSQLContainer("postgres:13-alpine");
    sourcePsql.start();
  }

  @AfterAll
  public static void end() {
    sourcePsql.stop();
  }

  @BeforeEach
  public void setup() throws IOException {
    sourceIds = Lists.newArrayList();
    connectionIds = Lists.newArrayList();
    destinationIds = Lists.newArrayList();

    final Path outputRoot = Files.createTempDirectory(AIRBYTE_LOCAL_ROOT, "acceptance_test");
    outputDir = outputRoot.resolve(RELATIVE_PATH);
    // get the path that starts with acceptance_tests_<random string>/destination_csv/test.
    relativeDir = outputRoot.getParent().relativize(outputDir);

    // seed database.
    PostgreSQLContainerHelper.runSqlScript(MountableFile.forClasspathResource("simple_postgres_init.sql"), sourcePsql);
  }

  @AfterEach
  public void tearDown() throws ApiException, SQLException {
    clearDbData(sourcePsql);

    for (UUID sourceId : sourceIds) {
      deleteSource(sourceId);
    }

    for (UUID connectionId : connectionIds) {
      disableConnection(connectionId);
    }

    for (UUID destinationId : destinationIds) {
      deleteDestination(destinationId);
    }
  }

  @Test
  @Order(1)
  public void testCreateDestination() throws ApiException {
    final UUID destinationDefId = getDestinationDefId();
    final JsonNode destinationConfig = getDestinationConfig();
    final UUID workspaceId = PersistenceConstants.DEFAULT_WORKSPACE_ID;
    final String name = "AccTestDestinationDb-" + UUID.randomUUID().toString();

    final DestinationRead createdDestination = createDestination(
        name,
        workspaceId,
        destinationDefId,
        destinationConfig);

    assertEquals(name, createdDestination.getName());
    assertEquals(destinationDefId, createdDestination.getDestinationDefinitionId());
    assertEquals(workspaceId, createdDestination.getWorkspaceId());
    assertEquals(destinationConfig, createdDestination.getConnectionConfiguration());
  }

  @Test
  @Order(2)
  public void testDestinationCheckConnection() throws ApiException {
    UUID destinationId = createCsvDestination().getDestinationId();

    CheckConnectionRead.StatusEnum checkOperationStatus = apiClient.getDestinationApi()
        .checkConnectionToDestination(
            new DestinationIdRequestBody().destinationId(destinationId))
        .getStatus();

    assertEquals(CheckConnectionRead.StatusEnum.SUCCEEDED, checkOperationStatus);
  }

  @Test
  @Order(3)
  public void testCreateSource() throws ApiException {
    String dbName = "acc-test-db";
    UUID postgresSourceDefinitionId = getPostgresSourceDefinitionId();
    UUID defaultWorkspaceId = PersistenceConstants.DEFAULT_WORKSPACE_ID;
    Map<Object, Object> sourceDbConfig = getSourceDbConfig();

    SourceRead response = createSource(
        dbName,
        defaultWorkspaceId,
        postgresSourceDefinitionId,
        sourceDbConfig);

    assertEquals(dbName, response.getName());
    assertEquals(defaultWorkspaceId, response.getWorkspaceId());
    assertEquals(postgresSourceDefinitionId, response.getSourceDefinitionId());
    assertEquals(Jsons.jsonNode(sourceDbConfig), response.getConnectionConfiguration());
  }

  @Test
  @Order(4)
  public void testSourceCheckConnection() throws ApiException {
    UUID sourceId = createPostgresSource().getSourceId();

    CheckConnectionRead checkConnectionRead = apiClient.getSourceApi()
        .checkConnectionToSource(new SourceIdRequestBody().sourceId(sourceId));

    assertEquals(CheckConnectionRead.StatusEnum.SUCCEEDED, checkConnectionRead.getStatus());
  }

  @Test
  @Order(5)
  public void testDiscoverSourceSchema() throws ApiException, IOException {
    UUID sourceId = createPostgresSource().getSourceId();

    SourceSchema actualSchema = discoverSourceSchema(sourceId);

    final SourceSchema expectedSchema = Jsons.deserialize(MoreResources.readResource("simple_postgres_source_schema.json"), SourceSchema.class);
    assertEquals(expectedSchema, actualSchema);
  }

  @Test
  @Order(6)
  public void testCreateConnection() throws ApiException {
    UUID sourceId = createPostgresSource().getSourceId();
    SourceSchema schema = discoverSourceSchema(sourceId);
    UUID destinationId = createCsvDestination().getDestinationId();
    String name = "test-connection-" + UUID.randomUUID().toString();
    ConnectionSchedule schedule = new ConnectionSchedule().timeUnit(MINUTES).units(100L);
    ConnectionCreate.SyncModeEnum syncMode = ConnectionCreate.SyncModeEnum.FULL_REFRESH;

    ConnectionRead createdConnection = createConnection(name, sourceId, destinationId, schema, schedule, syncMode);

    assertEquals(sourceId, createdConnection.getSourceId());
    assertEquals(destinationId, createdConnection.getDestinationId());
    assertEquals(ConnectionRead.SyncModeEnum.FULL_REFRESH, createdConnection.getSyncMode());
    assertEquals(schema, createdConnection.getSyncSchema());
    assertEquals(schedule, createdConnection.getSchedule());
    assertEquals(name, createdConnection.getName());
  }

  @Test
  @Order(7)
  public void testManualSync() throws Exception {
    String connectionName = "test-connection";
    UUID sourceId = createPostgresSource().getSourceId();
    UUID destinationId = createCsvDestination().getDestinationId();
    SourceSchema schema = discoverSourceSchema(sourceId);
    schema.getStreams().forEach(table -> table.getFields().forEach(c -> c.setSelected(true))); // select all fields
    ConnectionSchedule connectionSchedule = new ConnectionSchedule().units(100L).timeUnit(MINUTES);
    ConnectionCreate.SyncModeEnum syncMode = ConnectionCreate.SyncModeEnum.FULL_REFRESH;

    ConnectionRead createdConnection = createConnection(connectionName, sourceId, destinationId, schema, connectionSchedule, syncMode);

    ConnectionSyncRead connectionSyncRead =
        apiClient.getConnectionApi().syncConnection(new ConnectionIdRequestBody().connectionId(createdConnection.getConnectionId()));
    assertEquals(ConnectionSyncRead.StatusEnum.SUCCEEDED, connectionSyncRead.getStatus());
    assertSourceAndTargetDbInSync(sourcePsql);
  }

  @Test
  @Order(8)
  public void testScheduledSync() throws Exception {
    String connectionName = "test-connection";
    UUID sourceId = createPostgresSource().getSourceId();
    UUID destinationId = createCsvDestination().getDestinationId();
    SourceSchema schema = discoverSourceSchema(sourceId);
    schema.getStreams().forEach(table -> table.getFields().forEach(c -> c.setSelected(true))); // select all fields
    ConnectionSchedule connectionSchedule = new ConnectionSchedule().units(1L).timeUnit(MINUTES);
    ConnectionCreate.SyncModeEnum syncMode = ConnectionCreate.SyncModeEnum.FULL_REFRESH;

    createConnection(connectionName, sourceId, destinationId, schema, connectionSchedule, syncMode);

    // When a new connection is created, Airbyte might sync it immediately (before the sync interval).
    // Then it will wait the sync interval.
    Thread.sleep(Duration.of(30, SECONDS).toMillis());
    assertSourceAndTargetDbInSync(sourcePsql);
  }

  private SourceSchema discoverSourceSchema(UUID sourceId) throws ApiException {
    return apiClient.getSourceApi().discoverSchemaForSource(
        new SourceIdRequestBody().sourceId(sourceId)).getSchema();
  }

  private void assertSourceAndTargetDbInSync(PostgreSQLContainer sourceDb) throws Exception {
    Database database = getDatabase(sourceDb);

    Set<String> sourceStreams = listStreams(database);
    Set<String> targetStreams = listCsvStreams();
    assertEquals(sourceStreams, targetStreams);

    for (String table : sourceStreams) {
      assertStreamsEquivalent(database, table);
    }
  }

  private Database getDatabase(PostgreSQLContainer db) {
    return Databases.createPostgresDatabase(
        db.getUsername(), db.getPassword(), db.getJdbcUrl());
  }

  private Set<String> listStreams(Database database) throws SQLException {
    return database.query(
        context -> {
          Result<Record> fetch =
              context.fetch(
                  "SELECT tablename FROM pg_catalog.pg_tables WHERE schemaname != 'pg_catalog' AND schemaname != 'information_schema'");
          return fetch.stream()
              .map(record -> (String) record.get("tablename"))
              .collect(Collectors.toSet());
        });
  }

  private Set<String> listCsvStreams() throws IOException {
    return Files.list(outputDir)
        .map(file -> file.getFileName().toString().replaceAll(".csv", ""))
        .collect(Collectors.toSet());
  }

  private void assertStreamsEquivalent(Database database, String table) throws Exception {
    final Set<JsonNode> destinationRecords = new HashSet<>(retrieveCsvRecords(table));

    long sourceStreamCount = getStreamCount(database, table);
    assertEquals(sourceStreamCount, destinationRecords.size());
    final List<JsonNode> allRecords = retrievePgRecords(database, table);

    for (JsonNode sourceStreamRecord : allRecords) {
      assertTrue(destinationRecords.contains(sourceStreamRecord));
    }
  }

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  private long getStreamCount(Database database, String tableName) throws SQLException {
    return database.query(
        context -> {
          Result<Record> record =
              context.fetch(String.format("SELECT COUNT(*) FROM %s;", tableName));
          return (long) record.stream().findFirst().get().get(0);
        });
  }

  private ConnectionRead createConnection(String name,
                                          UUID sourceId,
                                          UUID destinationId,
                                          SourceSchema schema,
                                          ConnectionSchedule schedule,
                                          ConnectionCreate.SyncModeEnum syncMode)
      throws ApiException {
    ConnectionRead connection = apiClient.getConnectionApi().createConnection(
        new ConnectionCreate()
            .status(ConnectionStatus.ACTIVE)
            .sourceId(sourceId)
            .destinationId(destinationId)
            .syncMode(syncMode)
            .syncSchema(schema)
            .schedule(schedule)
            .name(name));
    connectionIds.add(connection.getConnectionId());
    return connection;
  }

  private DestinationRead createCsvDestination() throws ApiException {
    return createDestination(
        "AccTestDestination-" + UUID.randomUUID().toString(),
        PersistenceConstants.DEFAULT_WORKSPACE_ID,
        getDestinationDefId(),
        getDestinationConfig());
  }

  private DestinationRead createDestination(String name, UUID workspaceId, UUID destinationId, JsonNode destinationConfig)
      throws ApiException {
    DestinationRead destination =
        apiClient.getDestinationApi().createDestination(new DestinationCreate()
            .name(name)
            .connectionConfiguration(Jsons.jsonNode(destinationConfig))
            .workspaceId(workspaceId)
            .destinationDefinitionId(destinationId));
    destinationIds.add(destination.getDestinationId());
    return destination;
  }

  private UUID getDestinationDefId() throws ApiException {
    return apiClient.getDestinationDefinitionApi().listDestinationDefinitions().getDestinationDefinitions()
        .stream()
        .filter(dr -> dr.getName().toLowerCase().contains("csv"))
        .findFirst()
        .orElseThrow()
        .getDestinationDefinitionId();
  }

  private JsonNode getDestinationConfig() {
    return Jsons.jsonNode(ImmutableMap.of("destination_path", Path.of("/local").resolve(relativeDir).toString()));
  }

  private List<JsonNode> retrievePgRecords(Database database, String table) throws SQLException {
    return database.query(context -> context.fetch(String.format("SELECT * FROM %s;", table)))
        .stream()
        .map(Record::intoMap)
        .map(Jsons::jsonNode)
        .collect(Collectors.toList());
  }

  private List<JsonNode> retrieveCsvRecords(String streamName) throws Exception {
    final Optional<Path> stream = Files.list(outputDir)
        .filter(path -> path.getFileName().toString().toLowerCase().contains(streamName))
        .findFirst();
    assertTrue(stream.isPresent());

    final FileReader in = new FileReader(stream.get().toFile());
    final Iterable<CSVRecord> records = CSVFormat.DEFAULT
        .withHeader("data")
        .withFirstRecordAsHeader()
        .parse(in);

    return StreamSupport.stream(records.spliterator(), false)
        .map(record -> Jsons.deserialize(record.toMap().get("data")))
        .collect(Collectors.toList());
  }

  private Map<Object, Object> getSourceDbConfig() {
    Map<Object, Object> dbConfig = new HashMap<>();
    dbConfig.put("host", sourcePsql.getHost());
    dbConfig.put("password", sourcePsql.getPassword());
    dbConfig.put("port", sourcePsql.getFirstMappedPort());
    dbConfig.put("dbname", sourcePsql.getDatabaseName());
    dbConfig.put("user", sourcePsql.getUsername());
    return dbConfig;
  }

  private SourceRead createPostgresSource() throws ApiException {
    return createSource(
        "acceptanceTestDb-" + UUID.randomUUID().toString(),
        PersistenceConstants.DEFAULT_WORKSPACE_ID,
        getPostgresSourceDefinitionId(),
        getSourceDbConfig());
  }

  private SourceRead createSource(String name, UUID workspaceId, UUID sourceDefId, Map<Object, Object> sourceConfig)
      throws ApiException {
    SourceRead source = apiClient.getSourceApi().createSource(new SourceCreate()
        .name(name)
        .sourceDefinitionId(sourceDefId)
        .workspaceId(workspaceId)
        .connectionConfiguration(Jsons.jsonNode(sourceConfig)));
    sourceIds.add(source.getSourceId());
    return source;
  }

  private UUID getPostgresSourceDefinitionId() throws ApiException {
    return apiClient.getSourceDefinitionApi().listSourceDefinitions().getSourceDefinitions()
        .stream()
        .filter(sourceRead -> sourceRead.getName().toLowerCase().equals("postgres"))
        .findFirst()
        .orElseThrow()
        .getSourceDefinitionId();
  }

  private void clearDbData(PostgreSQLContainer db) throws SQLException {
    final Database database = getDatabase(db);
    final Set<String> tableNames = listStreams(database);
    for (final String tableName : tableNames) {
      database.query(context -> context.execute(String.format("DELETE FROM %s", tableName)));
    }
  }

  private void deleteSource(UUID sourceId) throws ApiException {
    apiClient.getSourceApi().deleteSource(new SourceIdRequestBody().sourceId(sourceId));
  }

  private void disableConnection(UUID connectionId) throws ApiException {
    ConnectionRead connection = apiClient.getConnectionApi().getConnection(new ConnectionIdRequestBody().connectionId(connectionId));
    ConnectionUpdate connectionUpdate =
        new ConnectionUpdate()
            .connectionId(connectionId)
            .status(ConnectionStatus.DEPRECATED)
            .schedule(connection.getSchedule())
            .syncSchema(connection.getSyncSchema());
    apiClient.getConnectionApi().updateConnection(connectionUpdate);
  }

  private void deleteDestination(UUID destinationId) throws ApiException {
    apiClient.getDestinationApi()
        .deleteDestination(new DestinationIdRequestBody().destinationId(destinationId));
  }

}

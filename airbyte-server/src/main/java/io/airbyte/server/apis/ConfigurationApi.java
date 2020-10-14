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

package io.airbyte.server.apis;

import io.airbyte.api.model.CheckConnectionRead;
import io.airbyte.api.model.ConnectionCreate;
import io.airbyte.api.model.ConnectionIdRequestBody;
import io.airbyte.api.model.ConnectionRead;
import io.airbyte.api.model.ConnectionReadList;
import io.airbyte.api.model.ConnectionSyncRead;
import io.airbyte.api.model.ConnectionUpdate;
import io.airbyte.api.model.DebugRead;
import io.airbyte.api.model.DestinationDefinitionIdRequestBody;
import io.airbyte.api.model.DestinationDefinitionRead;
import io.airbyte.api.model.DestinationDefinitionReadList;
import io.airbyte.api.model.DestinationDefinitionSpecificationRead;
import io.airbyte.api.model.DestinationDefinitionUpdate;
import io.airbyte.api.model.DestinationImplementationCreate;
import io.airbyte.api.model.DestinationImplementationIdRequestBody;
import io.airbyte.api.model.DestinationImplementationRead;
import io.airbyte.api.model.DestinationImplementationReadList;
import io.airbyte.api.model.DestinationImplementationRecreate;
import io.airbyte.api.model.DestinationImplementationUpdate;
import io.airbyte.api.model.JobIdRequestBody;
import io.airbyte.api.model.JobInfoRead;
import io.airbyte.api.model.JobListRequestBody;
import io.airbyte.api.model.JobReadList;
import io.airbyte.api.model.SlugRequestBody;
import io.airbyte.api.model.SourceCreate;
import io.airbyte.api.model.SourceDefinitionCreate;
import io.airbyte.api.model.SourceDefinitionIdRequestBody;
import io.airbyte.api.model.SourceDefinitionRead;
import io.airbyte.api.model.SourceDefinitionReadList;
import io.airbyte.api.model.SourceDefinitionSpecificationRead;
import io.airbyte.api.model.SourceDefinitionUpdate;
import io.airbyte.api.model.SourceDiscoverSchemaRead;
import io.airbyte.api.model.SourceIdRequestBody;
import io.airbyte.api.model.SourceRead;
import io.airbyte.api.model.SourceReadList;
import io.airbyte.api.model.SourceRecreate;
import io.airbyte.api.model.SourceUpdate;
import io.airbyte.api.model.WbConnectionRead;
import io.airbyte.api.model.WbConnectionReadList;
import io.airbyte.api.model.WorkspaceIdRequestBody;
import io.airbyte.api.model.WorkspaceRead;
import io.airbyte.api.model.WorkspaceUpdate;
import io.airbyte.commons.json.JsonSchemaValidator;
import io.airbyte.commons.json.JsonValidationException;
import io.airbyte.config.persistence.ConfigNotFoundException;
import io.airbyte.config.persistence.ConfigRepository;
import io.airbyte.scheduler.persistence.SchedulerPersistence;
import io.airbyte.server.errors.KnownException;
import io.airbyte.server.handlers.ConnectionsHandler;
import io.airbyte.server.handlers.DebugInfoHandler;
import io.airbyte.server.handlers.DestinationDefinitionsHandler;
import io.airbyte.server.handlers.DestinationImplementationsHandler;
import io.airbyte.server.handlers.JobHistoryHandler;
import io.airbyte.server.handlers.SchedulerHandler;
import io.airbyte.server.handlers.SourceDefinitionsHandler;
import io.airbyte.server.handlers.SourceHandler;
import io.airbyte.server.handlers.WebBackendConnectionsHandler;
import io.airbyte.server.handlers.WebBackendDestinationImplementationHandler;
import io.airbyte.server.handlers.WebBackendSourceImplementationHandler;
import io.airbyte.server.handlers.WorkspacesHandler;
import io.airbyte.server.validators.DockerImageValidator;
import java.io.IOException;
import javax.validation.Valid;
import org.eclipse.jetty.http.HttpStatus;

@javax.ws.rs.Path("/v1")
public class ConfigurationApi implements io.airbyte.api.V1Api {

  private final WorkspacesHandler workspacesHandler;
  private final SourceDefinitionsHandler sourceDefinitionsHandler;
  private final SourceHandler sourceHandler;
  private final DestinationDefinitionsHandler destinationDefinitionsHandler;
  private final DestinationImplementationsHandler destinationImplementationsHandler;
  private final ConnectionsHandler connectionsHandler;
  private final DebugInfoHandler debugInfoHandler;
  private final SchedulerHandler schedulerHandler;
  private final JobHistoryHandler jobHistoryHandler;
  private final WebBackendConnectionsHandler webBackendConnectionsHandler;
  private final WebBackendSourceImplementationHandler webBackendSourceImplementationHandler;
  private final WebBackendDestinationImplementationHandler webBackendDestinationImplementationHandler;

  public ConfigurationApi(final ConfigRepository configRepository, final SchedulerPersistence schedulerPersistence) {
    final JsonSchemaValidator schemaValidator = new JsonSchemaValidator();
    schedulerHandler = new SchedulerHandler(configRepository, schedulerPersistence);
    workspacesHandler = new WorkspacesHandler(configRepository);
    DockerImageValidator dockerImageValidator = new DockerImageValidator(schedulerHandler);
    sourceDefinitionsHandler = new SourceDefinitionsHandler(configRepository, dockerImageValidator);
    connectionsHandler = new ConnectionsHandler(configRepository);
    destinationDefinitionsHandler = new DestinationDefinitionsHandler(configRepository, dockerImageValidator);
    destinationImplementationsHandler =
        new DestinationImplementationsHandler(configRepository, schemaValidator, schedulerHandler, connectionsHandler);
    sourceHandler = new SourceHandler(configRepository, schemaValidator, schedulerHandler, connectionsHandler);
    jobHistoryHandler = new JobHistoryHandler(schedulerPersistence);
    webBackendConnectionsHandler = new WebBackendConnectionsHandler(connectionsHandler, sourceHandler, jobHistoryHandler);
    webBackendSourceImplementationHandler = new WebBackendSourceImplementationHandler(sourceHandler, schedulerHandler);
    webBackendDestinationImplementationHandler = new WebBackendDestinationImplementationHandler(destinationImplementationsHandler, schedulerHandler);
    debugInfoHandler = new DebugInfoHandler(configRepository);
  }

  // WORKSPACE

  @Override
  public WorkspaceRead getWorkspace(@Valid WorkspaceIdRequestBody workspaceIdRequestBody) {
    return execute(() -> workspacesHandler.getWorkspace(workspaceIdRequestBody));
  }

  @Override
  public WorkspaceRead getWorkspaceBySlug(@Valid SlugRequestBody slugRequestBody) {
    return execute(() -> workspacesHandler.getWorkspaceBySlug(slugRequestBody));
  }

  @Override
  public WorkspaceRead updateWorkspace(@Valid WorkspaceUpdate workspaceUpdate) {
    return execute(() -> workspacesHandler.updateWorkspace(workspaceUpdate));
  }

  @Override
  public DestinationImplementationRead webBackendCreateDestinationImplementation(@Valid DestinationImplementationCreate destinationImplementationCreate) {
    return execute(
        () -> webBackendDestinationImplementationHandler.webBackendCreateDestinationImplementationAndCheck(destinationImplementationCreate));
  }

  @Override
  public SourceRead webBackendCreateSource(@Valid SourceCreate sourceCreate) {
    return execute(() -> webBackendSourceImplementationHandler.webBackendCreateSourceImplementationAndCheck(sourceCreate));
  }

  // SOURCE

  @Override
  public SourceDefinitionReadList listSourceDefinitions() {
    return execute(sourceDefinitionsHandler::listSourceDefinitions);
  }

  @Override
  public SourceDefinitionRead getSourceDefinition(@Valid SourceDefinitionIdRequestBody sourceDefinitionIdRequestBody) {
    return execute(() -> sourceDefinitionsHandler.getSourceDefinition(sourceDefinitionIdRequestBody));
  }

  @Override
  public SourceDefinitionRead createSourceDefinition(@Valid SourceDefinitionCreate sourceDefinitionCreate) {
    return execute(() -> sourceDefinitionsHandler.createSourceDefinition(sourceDefinitionCreate));
  }

  @Override
  public SourceDefinitionRead updateSourceDefinition(@Valid SourceDefinitionUpdate sourceDefinitionUpdate) {
    return execute(() -> sourceDefinitionsHandler.updateSourceDefinition(sourceDefinitionUpdate));
  }

  // SOURCE SPECIFICATION

  @Override
  public SourceDefinitionSpecificationRead getSourceDefinitionSpecification(@Valid SourceDefinitionIdRequestBody sourceDefinitionIdRequestBody) {
    return execute(() -> schedulerHandler.getSourceSpecification(sourceDefinitionIdRequestBody));
  }
  // SOURCE IMPLEMENTATION

  @Override
  public SourceRead createSource(@Valid SourceCreate sourceCreate) {
    return execute(() -> sourceHandler.createSource(sourceCreate));
  }

  @Override
  public SourceRead updateSource(@Valid SourceUpdate sourceUpdate) {
    return execute(() -> sourceHandler.updateSource(sourceUpdate));
  }

  @Override
  public SourceReadList listSourcesForWorkspace(@Valid WorkspaceIdRequestBody workspaceIdRequestBody) {
    return execute(() -> sourceHandler.listSourcesForWorkspace(workspaceIdRequestBody));
  }

  @Override
  public SourceRead getSource(@Valid SourceIdRequestBody sourceIdRequestBody) {
    return execute(() -> sourceHandler.getSource(sourceIdRequestBody));
  }

  @Override
  public void deleteSource(@Valid SourceIdRequestBody sourceIdRequestBody) {
    execute(() -> {
      sourceHandler.deleteSource(sourceIdRequestBody);
      return null;
    });
  }

  @Override
  public CheckConnectionRead checkConnectionToSource(@Valid SourceIdRequestBody sourceIdRequestBody) {
    return execute(() -> schedulerHandler.checkSourceImplementationConnection(sourceIdRequestBody));
  }

  @Override
  public SourceDiscoverSchemaRead discoverSchemaForSource(@Valid SourceIdRequestBody sourceIdRequestBody) {
    return execute(() -> schedulerHandler.discoverSchemaForSourceImplementation(sourceIdRequestBody));
  }
  // DESTINATION

  @Override
  public DestinationDefinitionReadList listDestinationDefinitions() {
    return execute(destinationDefinitionsHandler::listDestinationDefinitions);
  }

  @Override
  public DestinationDefinitionRead getDestinationDefinition(@Valid DestinationDefinitionIdRequestBody destinationDefinitionIdRequestBody) {
    return execute(() -> destinationDefinitionsHandler.getDestinationDefinition(destinationDefinitionIdRequestBody));
  }

  @Override
  public DestinationDefinitionRead updateDestinationDefinition(@Valid DestinationDefinitionUpdate destinationDefinitionUpdate) {
    return execute(() -> destinationDefinitionsHandler.updateDestinationDefinition(destinationDefinitionUpdate));
  }

  // DESTINATION SPECIFICATION

  @Override
  public DestinationDefinitionSpecificationRead getDestinationDefinitionSpecification(@Valid DestinationDefinitionIdRequestBody destinationDefinitionIdRequestBody) {
    return execute(() -> schedulerHandler.getDestinationSpecification(destinationDefinitionIdRequestBody));
  }

  // DESTINATION IMPLEMENTATION

  @Override
  public DestinationImplementationRead createDestinationImplementation(@Valid DestinationImplementationCreate destinationImplementationCreate) {
    return execute(() -> destinationImplementationsHandler.createDestinationImplementation(destinationImplementationCreate));
  }

  @Override
  public void deleteDestinationImplementation(@Valid DestinationImplementationIdRequestBody destinationImplementationIdRequestBody) {
    execute(() -> {
      destinationImplementationsHandler.deleteDestinationImplementation(destinationImplementationIdRequestBody);
      return null;
    });
  }

  @Override
  public DestinationImplementationRead updateDestinationImplementation(@Valid DestinationImplementationUpdate destinationImplementationUpdate) {
    return execute(() -> destinationImplementationsHandler.updateDestinationImplementation(destinationImplementationUpdate));
  }

  @Override
  public DestinationImplementationReadList listDestinationImplementationsForWorkspace(@Valid WorkspaceIdRequestBody workspaceIdRequestBody) {
    return execute(() -> destinationImplementationsHandler.listDestinationImplementationsForWorkspace(workspaceIdRequestBody));
  }

  @Override
  public DestinationImplementationRead getDestinationImplementation(@Valid DestinationImplementationIdRequestBody destinationImplementationIdRequestBody) {
    return execute(() -> destinationImplementationsHandler.getDestinationImplementation(destinationImplementationIdRequestBody));
  }

  @Override
  public CheckConnectionRead checkConnectionToDestinationImplementation(@Valid DestinationImplementationIdRequestBody destinationImplementationIdRequestBody) {
    return execute(() -> schedulerHandler.checkDestinationImplementationConnection(destinationImplementationIdRequestBody));
  }

  // CONNECTION

  @Override
  public ConnectionRead createConnection(@Valid ConnectionCreate connectionCreate) {
    return execute(() -> connectionsHandler.createConnection(connectionCreate));
  }

  @Override
  public ConnectionRead updateConnection(@Valid ConnectionUpdate connectionUpdate) {
    return execute(() -> connectionsHandler.updateConnection(connectionUpdate));
  }

  @Override
  public ConnectionReadList listConnectionsForWorkspace(@Valid WorkspaceIdRequestBody workspaceIdRequestBody) {
    return execute(() -> connectionsHandler.listConnectionsForWorkspace(workspaceIdRequestBody));
  }

  @Override
  public ConnectionRead getConnection(@Valid ConnectionIdRequestBody connectionIdRequestBody) {
    return execute(() -> connectionsHandler.getConnection(connectionIdRequestBody));
  }

  @Override
  public DebugRead getDebuggingInfo() {
    return execute(debugInfoHandler::getInfo);
  }

  @Override
  public ConnectionSyncRead syncConnection(@Valid ConnectionIdRequestBody connectionIdRequestBody) {
    return execute(() -> schedulerHandler.syncConnection(connectionIdRequestBody));
  }

  // JOB HISTORY

  @Override
  public JobReadList listJobsFor(@Valid JobListRequestBody jobListRequestBody) {
    return execute(() -> jobHistoryHandler.listJobsFor(jobListRequestBody));
  }

  @Override
  public JobInfoRead getJobInfo(@Valid JobIdRequestBody jobIdRequestBody) {
    return execute(() -> jobHistoryHandler.getJobInfo(jobIdRequestBody));
  }

  // WEB BACKEND

  @Override
  public WbConnectionReadList webBackendListConnectionsForWorkspace(@Valid WorkspaceIdRequestBody workspaceIdRequestBody) {
    return execute(() -> webBackendConnectionsHandler.webBackendListConnectionsForWorkspace(workspaceIdRequestBody));
  }

  @Override
  public DestinationImplementationRead webBackendRecreateDestinationImplementation(@Valid DestinationImplementationRecreate destinationImplementationRecreate) {
    return execute(
        () -> webBackendDestinationImplementationHandler.webBackendRecreateDestinationImplementationAndCheck(destinationImplementationRecreate));
  }

  @Override
  public SourceRead webBackendRecreateSource(@Valid SourceRecreate sourceRecreate) {
    return execute(() -> webBackendSourceImplementationHandler.webBackendRecreateSourceImplementationAndCheck(sourceRecreate));
  }

  @Override
  public WbConnectionRead webBackendGetConnection(@Valid ConnectionIdRequestBody connectionIdRequestBody) {
    return execute(() -> webBackendConnectionsHandler.webBackendGetConnection(connectionIdRequestBody));
  }

  private <T> T execute(HandlerCall<T> call) {
    try {
      return call.call();
    } catch (ConfigNotFoundException e) {
      throw new KnownException(
          HttpStatus.UNPROCESSABLE_ENTITY_422,
          String.format("Could not find configuration for %s: %s.", e.getType().toString(), e.getConfigId()), e);
    } catch (JsonValidationException e) {
      throw new KnownException(
          HttpStatus.UNPROCESSABLE_ENTITY_422,
          String.format("The provided configuration does not fulfill the specification. Errors: %s", e.getMessage()), e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private interface HandlerCall<T> {

    T call() throws ConfigNotFoundException, IOException, JsonValidationException;

  }

}

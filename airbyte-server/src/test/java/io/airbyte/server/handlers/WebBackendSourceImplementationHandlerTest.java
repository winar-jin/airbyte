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

package io.airbyte.server.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.airbyte.api.model.CheckConnectionRead;
import io.airbyte.api.model.CheckConnectionRead.StatusEnum;
import io.airbyte.api.model.SourceCreate;
import io.airbyte.api.model.SourceIdRequestBody;
import io.airbyte.api.model.SourceRead;
import io.airbyte.api.model.SourceRecreate;
import io.airbyte.commons.json.JsonValidationException;
import io.airbyte.config.SourceConnectionImplementation;
import io.airbyte.config.StandardSource;
import io.airbyte.config.persistence.ConfigNotFoundException;
import io.airbyte.server.errors.KnownException;
import io.airbyte.server.helpers.SourceHelpers;
import io.airbyte.server.helpers.SourceImplementationHelpers;
import java.io.IOException;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class WebBackendSourceImplementationHandlerTest {

  private WebBackendSourceImplementationHandler wbSourceImplementationHandler;

  private SourceHandler sourceHandler;
  private SchedulerHandler schedulerHandler;

  private SourceRead sourceRead;

  @BeforeEach
  public void setup() throws IOException {
    sourceHandler = mock(SourceHandler.class);
    schedulerHandler = mock(SchedulerHandler.class);
    wbSourceImplementationHandler = new WebBackendSourceImplementationHandler(sourceHandler, schedulerHandler);

    final StandardSource standardSource = SourceHelpers.generateSource();
    SourceConnectionImplementation sourceImplementation = SourceImplementationHelpers.generateSourceImplementation(UUID.randomUUID());
    sourceRead = SourceImplementationHelpers.getSourceImplementationRead(sourceImplementation, standardSource);
  }

  @Test
  public void testCreatesSourceWhenCheckConnectionSucceeds() throws JsonValidationException, IOException, ConfigNotFoundException {
    SourceCreate sourceCreate = new SourceCreate();
    sourceCreate.setName(sourceRead.getName());
    sourceCreate.setConnectionConfiguration(sourceRead.getConnectionConfiguration());
    sourceCreate.setSourceDefinitionId(sourceRead.getSourceDefinitionId());
    sourceCreate.setWorkspaceId(sourceRead.getWorkspaceId());

    when(sourceHandler.createSource(sourceCreate)).thenReturn(sourceRead);

    SourceIdRequestBody sourceIdRequestBody = new SourceIdRequestBody();
    sourceIdRequestBody.setSourceId(sourceRead.getSourceId());

    CheckConnectionRead checkConnectionRead = new CheckConnectionRead();
    checkConnectionRead.setStatus(StatusEnum.SUCCESS);

    when(schedulerHandler.checkSourceImplementationConnection(sourceIdRequestBody)).thenReturn(checkConnectionRead);

    SourceRead returnedSource = wbSourceImplementationHandler.webBackendCreateSourceImplementationAndCheck(sourceCreate);

    verify(sourceHandler, times(0)).deleteSource(Mockito.any());
    assertEquals(sourceRead, returnedSource);
  }

  @Test
  public void testDeletesSourceWhenCheckConnectionFails() throws JsonValidationException, IOException, ConfigNotFoundException {
    SourceCreate sourceCreate = new SourceCreate();
    sourceCreate.setName(sourceRead.getName());
    sourceCreate.setConnectionConfiguration(sourceRead.getConnectionConfiguration());
    sourceCreate.setWorkspaceId(sourceRead.getWorkspaceId());
    when(sourceHandler.createSource(sourceCreate)).thenReturn(sourceRead);

    CheckConnectionRead checkConnectionRead = new CheckConnectionRead();
    checkConnectionRead.setStatus(StatusEnum.FAILURE);

    SourceIdRequestBody sourceIdRequestBody = new SourceIdRequestBody();
    sourceIdRequestBody.setSourceId(sourceRead.getSourceId());
    when(schedulerHandler.checkSourceImplementationConnection(sourceIdRequestBody)).thenReturn(checkConnectionRead);

    Assertions.assertThrows(KnownException.class,
        () -> wbSourceImplementationHandler.webBackendCreateSourceImplementationAndCheck(sourceCreate));

    verify(sourceHandler).deleteSource(sourceIdRequestBody);
  }

  @Test
  public void testReCreatesSourceWhenCheckConnectionSucceeds() throws JsonValidationException, IOException, ConfigNotFoundException {
    SourceCreate sourceCreate = new SourceCreate();
    sourceCreate.setName(sourceRead.getName());
    sourceCreate.setConnectionConfiguration(sourceRead.getConnectionConfiguration());
    sourceCreate.setWorkspaceId(sourceRead.getWorkspaceId());

    SourceRead newSourceImplementation = SourceImplementationHelpers
        .getSourceImplementationRead(SourceImplementationHelpers.generateSourceImplementation(UUID.randomUUID()), SourceHelpers.generateSource());

    when(sourceHandler.createSource(sourceCreate)).thenReturn(newSourceImplementation);

    SourceIdRequestBody newSourceId = new SourceIdRequestBody();
    newSourceId.setSourceId(newSourceImplementation.getSourceId());

    CheckConnectionRead checkConnectionRead = new CheckConnectionRead();
    checkConnectionRead.setStatus(StatusEnum.SUCCESS);

    when(schedulerHandler.checkSourceImplementationConnection(newSourceId)).thenReturn(checkConnectionRead);

    SourceRecreate sourceRecreate = new SourceRecreate();
    sourceRecreate.setName(sourceRead.getName());
    sourceRecreate.setConnectionConfiguration(sourceRead.getConnectionConfiguration());
    sourceRecreate.setWorkspaceId(sourceRead.getWorkspaceId());
    sourceRecreate.setSourceId(sourceRead.getSourceId());

    SourceIdRequestBody oldSourceIdBody = new SourceIdRequestBody();
    oldSourceIdBody.setSourceId(sourceRead.getSourceId());

    SourceRead returnedSource =
        wbSourceImplementationHandler.webBackendRecreateSourceImplementationAndCheck(sourceRecreate);

    verify(sourceHandler, times(1)).deleteSource(Mockito.eq(oldSourceIdBody));
    assertEquals(newSourceImplementation, returnedSource);
  }

  @Test
  public void testRecreateDeletesNewCreatedSourceWhenFails() throws JsonValidationException, IOException, ConfigNotFoundException {
    SourceCreate sourceCreate = new SourceCreate();
    sourceCreate.setName(sourceRead.getName());
    sourceCreate.setConnectionConfiguration(sourceRead.getConnectionConfiguration());
    sourceCreate.setWorkspaceId(sourceRead.getWorkspaceId());

    SourceRead newSourceImplementation = SourceImplementationHelpers
        .getSourceImplementationRead(SourceImplementationHelpers.generateSourceImplementation(UUID.randomUUID()), SourceHelpers.generateSource());

    when(sourceHandler.createSource(sourceCreate)).thenReturn(newSourceImplementation);

    SourceIdRequestBody newSourceId = new SourceIdRequestBody();
    newSourceId.setSourceId(newSourceImplementation.getSourceId());

    CheckConnectionRead checkConnectionRead = new CheckConnectionRead();
    checkConnectionRead.setStatus(StatusEnum.FAILURE);

    when(schedulerHandler.checkSourceImplementationConnection(newSourceId)).thenReturn(checkConnectionRead);

    SourceRecreate sourceRecreate = new SourceRecreate();
    sourceRecreate.setName(sourceRead.getName());
    sourceRecreate.setConnectionConfiguration(sourceRead.getConnectionConfiguration());
    sourceRecreate.setWorkspaceId(sourceRead.getWorkspaceId());
    sourceRecreate.setSourceId(sourceRead.getSourceId());

    Assertions.assertThrows(KnownException.class,
        () -> wbSourceImplementationHandler.webBackendRecreateSourceImplementationAndCheck(sourceRecreate));
    verify(sourceHandler, times(1)).deleteSource(Mockito.eq(newSourceId));
  }

}

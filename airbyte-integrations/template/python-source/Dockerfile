FROM airbyte/integration-base-python:dev

ENV AIRBYTE_IMPL_MODULE="template_python_source"
ENV AIRBYTE_IMPL_PATH="TemplatePythonSource"

LABEL io.airbyte.version=0.1.0
LABEL io.airbyte.name=airbyte/source-template-python

WORKDIR /airbyte/integration_code
COPY src ./src
COPY setup.py ./
RUN pip install .

WORKDIR /airbyte

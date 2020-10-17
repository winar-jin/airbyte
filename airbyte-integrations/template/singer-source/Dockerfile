FROM airbyte/integration-base-singer:dev

RUN apt-get update && apt-get install -y \
    jq \
    && rm -rf /var/lib/apt/lists/*

ENV AIRBYTE_IMPL_MODULE="template_singer_source"
ENV AIRBYTE_IMPL_PATH="TemplateSingerSource"

LABEL io.airbyte.version=0.1.0
LABEL io.airbyte.name=airbyte/source-template-singer

WORKDIR /airbyte/integration_code
COPY src ./src
COPY setup.py ./
RUN pip install .

WORKDIR /airbyte

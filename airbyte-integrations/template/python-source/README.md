# Python Airbyte Source Development

Prepare development environment:
```
cd airbyte-integrations/template/python-source
pipenv install
```

Test locally:
```
pipenv run python main_dev.py spec
pipenv run python main_dev.py check --config sample_files/test_config.json
pipenv run python main_dev.py discover --config sample_files/test_config.json
pipenv run python main_dev.py read --config sample_files/test_config.json --catalog sample_files/test_catalog.json
```

Test image:
```
# in airbyte root directory
./gradlew :airbyte-integrations:template:python-source:buildImage
docker run --rm -v $(pwd)/airbyte-integrations/template/python-source/sample_files:/sample_files airbyte/source-template-python:dev spec
docker run --rm -v $(pwd)/airbyte-integrations/template/python-source/sample_files:/sample_files airbyte/source-template-python:dev check --config /sample_files/test_config.json
docker run --rm -v $(pwd)/airbyte-integrations/template/python-source/sample_files:/sample_files airbyte/source-template-python:dev discover --config /sample_files/test_config.json
docker run --rm -v $(pwd)/airbyte-integrations/template/python-source/sample_files:/sample_files airbyte/source-template-python:dev read --config /sample_files/test_config.json --catalog /sample_files/test_catalog.json
```

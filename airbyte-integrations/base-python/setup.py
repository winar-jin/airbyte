from setuptools import setup, find_packages

print(find_packages(where='src/main/python'))

setup(
    name='airbyte-protocol',
    description='Contains classes representing the schema of the Airbyte protocol.',
    author='Airbyte',
    author_email='contact@airbyte.io',
    url='https://github.com/airbytehq/airbyte',

    packages=find_packages(where='src/main/python'),
    package_dir={'airbyte_protocol': 'src/main/python/airbyte_protocol'},

    install_requires=[
        'PyYAML==5.3.1',
        'pydantic==1.6.1'
    ],

    entry_points={
        'console_scripts': [
            'base-python=airbyte_protocol.entrypoint:main'
        ],
    }
)

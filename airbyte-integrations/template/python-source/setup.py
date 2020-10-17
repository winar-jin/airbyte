from setuptools import setup, find_packages

print(find_packages())

setup(
    name='template-python-source',
    description='Source implementation template',
    author='Airbyte',
    author_email='contact@airbyte.io',

    packages=find_packages(where='src/main/python'),
    package_data={
        '': ['*.json']
    },

    install_requires=[]
)

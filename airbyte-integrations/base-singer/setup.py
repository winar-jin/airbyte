from setuptools import setup, find_packages

setup(
    name='base-singer',
    description='Contains helpers for handling Singer sources and destinations.',
    author='Airbyte',
    author_email='contact@airbyte.io',

    packages=setuptools.find_packages(where='src/main/python'),

    install_requires=[]
)

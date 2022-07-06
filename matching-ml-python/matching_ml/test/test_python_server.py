# This unit test checks the python_server.py
# Run `pytest` in the root directory of the jRDF2vec project (where the pom.xml resides).

from pathlib import Path

import pytest
import requests

from kbert.constants import RESOURCES_DIR, URI_PREFIX
from kbert.test.ServerThread import ServerThread
from python_server_melt import app as my_app


@pytest.fixture
def server_thread():
    server_thread = ServerThread()
    # wait_time_seconds = 10
    server_thread.start()
    # print(f"Waiting {wait_time_seconds} seconds for the server to start.")
    # time.sleep(wait_time_seconds)
    yield
    print("Shutting down...")
    server_thread.stop()


def test_get_vector(server_thread):
    test_model_vectors = "../../test/resources/test_model_vectors.kv"
    vector_test_path = Path(test_model_vectors)
    assert vector_test_path.is_file()
    result = requests.get(
        URI_PREFIX + "get-vector",
        headers={"concept": "Europe", "vector_path": test_model_vectors},
    )
    assert len(result.content.decode("utf-8").split(" ")) == 100


def test_is_in_vocabulary(server_thread):
    test_model = "../../test/resources/test_model"
    test_vectors = "../../test/resources/test_model_vectors.kv"
    model_test_path = Path(test_model)
    vector_test_path = Path(test_vectors)
    assert model_test_path.is_file()
    assert vector_test_path.is_file()
    result = requests.get(
        URI_PREFIX + "is-in-vocabulary",
        headers={"concept": "Europe", "model_path": test_model},
    )
    assert result.content.decode("utf-8") == "True"
    result = requests.get(
        URI_PREFIX + "is-in-vocabulary",
        headers={"concept": "Europe", "vector_path": test_vectors},
    )
    assert result.content.decode("utf-8") == "True"


def test_get_similarity(server_thread):
    test_model = "../../test/resources/test_model"
    model_test_path = Path(test_model)
    assert model_test_path.is_file()
    result = requests.get(
        URI_PREFIX + "get-similarity",
        headers={
            "concept_1": "Europe",
            "concept_2": "united",
            "model_path": test_model,
        },
    )
    result_str = result.content.decode("utf-8")
    assert float(result_str) > 0


def teardown_module(module):
    print("Shutting down...")
    server_thread.stop()

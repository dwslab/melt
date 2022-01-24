# This unit test checks the python_server.py
# Run `pytest` in the root directory of the jRDF2vec project (where the pom.xml resides).

import threading
import python_server_melt as server
import time
import requests
from pathlib import Path


uri_prefix = "http://localhost:41193/"


class ServerThread(threading.Thread):
    def __init__(self, *args, **kwargs):
        super(ServerThread, self).__init__(*args, **kwargs)
        self._stop_event = threading.Event()

    def run(self):
        server.main()

    def stop(self):
        self._stop_event.set()
        requests.get(uri_prefix + "shutdown")

    def stopped(self):
        return self._stop_event.is_set()


server_thread = ServerThread()


def setup_module(module):
    """Let's start the server."""
    wait_time_seconds = 10
    server_thread.start()
    print(f"Waiting {wait_time_seconds} seconds for the server to start.")
    time.sleep(wait_time_seconds)


def test_get_vector():
    test_model_vectors = "../../test/resources/test_model_vectors.kv"
    vector_test_path = Path(test_model_vectors)
    assert vector_test_path.is_file()
    result = requests.get(
        uri_prefix + "get-vector",
        headers={"concept": "Europe", "vector_path": test_model_vectors},
    )
    assert len(result.content.decode("utf-8").split(" ")) == 100


def test_is_in_vocabulary():
    test_model = "../../test/resources/test_model"
    test_vectors = "../../test/resources/test_model_vectors.kv"
    model_test_path = Path(test_model)
    vector_test_path = Path(test_vectors)
    assert model_test_path.is_file()
    assert vector_test_path.is_file()
    result = requests.get(
        uri_prefix + "is-in-vocabulary",
        headers={"concept": "Europe", "model_path": test_model},
    )
    assert result.content.decode("utf-8") == "True"
    result = requests.get(
        uri_prefix + "is-in-vocabulary",
        headers={"concept": "Europe", "vector_path": test_vectors},
    )
    assert result.content.decode("utf-8") == "True"


def test_get_similarity():
    test_model = "../../test/resources/test_model"
    model_test_path = Path(test_model)
    assert model_test_path.is_file()
    result = requests.get(
        uri_prefix + "get-similarity",
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

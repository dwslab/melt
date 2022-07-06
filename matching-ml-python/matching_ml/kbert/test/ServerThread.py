import threading

import requests

import matching_ml.python_server_melt as server
from kbert.constants import URI_PREFIX


class ServerThread(threading.Thread):
    def __init__(self, *args, **kwargs):
        super(ServerThread, self).__init__(*args, **kwargs)
        self._stop_event = threading.Event()
        self.daemon = True

    def run(self):
        server.main()

    def stop(self):
        self._stop_event.set()
        requests.get(URI_PREFIX + "shutdown")

    def stopped(self):
        return self._stop_event.is_set()

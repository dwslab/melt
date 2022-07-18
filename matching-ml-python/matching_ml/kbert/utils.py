import logging
import sys
import time
from contextlib import contextmanager

@contextmanager
def print_time():
    start_time = time.time()
    yield
    print(f'took {time.time() - start_time} seconds')

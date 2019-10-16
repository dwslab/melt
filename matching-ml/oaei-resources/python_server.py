from flask import Flask
from flask import request

# default boilerplate code
app = Flask(__name__)


@app.route('/melt_ml.html')
def display_server_status():
    return "MELT ML Server running. Ready to accept requests."


@app.route('/hello', methods=['GET'])
def hello_demo():
    """A demo program that will return Hello <name> when called.

    Returns
    -------
    greeting : str
        A simple greeting.
    """
    name_to_greet = request.headers.get('name')
    print(name_to_greet)
    return "Hello " + str(name_to_greet) + "!"


if __name__ == "__main__":
    app.run(debug=False, port=41193)

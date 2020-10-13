import logging
import sys
from rdflib import Graph, URIRef, RDFS
from AlignmentFormat import serialize_mapping_to_tmp_file
from collections import defaultdict


def match_rdflib(source_graph, target_graph, input_alignment):
    # a very simple label matcher:
    alignment = []

    label_to_uri = defaultdict(list)
    for s, p, o in source_graph.triples((None, RDFS.label, None)):
        if isinstance(s, URIRef):
            label_to_uri[str(o)].append(str(s))

    for s, p, o in target_graph.triples((None, RDFS.label, None)):
        if isinstance(s, URIRef) and str(o) in label_to_uri:
            for one_uri in label_to_uri[str(o)]:
                alignment.append((one_uri, str(s), "=", 1.0))
    return alignment
    # return [('http://one.de', 'http://two.de', '=', 1.0)]


def get_file_from_url(location):
    from urllib.parse import unquote, urlparse
    from urllib.request import url2pathname, urlopen

    if location.startswith("file:"):
        return open(url2pathname(unquote(urlparse(location).path)))
    else:
        return urlopen(location)


def match(source_url, target_url, input_alignment_url):
    logging.info("Python matcher info: Match " + source_url + " to " + target_url)

    # in case you want the file object use
    # source_file = get_file_from_url(source_url)
    # target_file = get_file_from_url(target_url)

    source_graph = Graph()
    source_graph.parse(source_url)
    logging.info("Read source with %s triples.", len(source_graph))

    target_graph = Graph()
    target_graph.parse(target_url)
    logging.info("Read target with %s triples.", len(target_graph))

    input_alignment = None
    # if input_alignment_url is not None:

    resulting_alignment = match_rdflib(source_graph, target_graph, input_alignment)

    # in case you have the results in a pandas dataframe, make sure you have the columns
    # source (uri), target (uri), relation (usually the string '='), confidence (floating point number)
    # in case relation or confidence is missing use: df["relation"] = '='  and  df["confidence"] = 1.0
    # then select the columns in the right order (like df[['source', 'target', 'relation', 'confidence']])
    # because serialize_mapping_to_tmp_file expects an iterbale of source, target, relation, confidence
    # and then call .itertuples(index=False)
    # example: alignment_file_url = serialize_mapping_to_tmp_file(df[['source', 'target', 'relation', 'confidence']].itertuples(index=False))

    alignment_file_url = serialize_mapping_to_tmp_file(resulting_alignment)
    return alignment_file_url


def main(argv):
    if len(argv) == 2:
        print(match(argv[0], argv[1], None))
    elif len(argv) >= 3:
        if len(argv) > 3:
            logging.error("Too many parameters but we will ignore them.")
        print(match(argv[0], argv[1], argv[2]))
    else:
        logging.error(
            "Too few parameters. Need at least two (source and target URL of ontologies"
        )


if __name__ == "__main__":
    logging.basicConfig(
        format="%(asctime)s %(levelname)s:%(message)s", level=logging.INFO
    )
    main(sys.argv[1:])

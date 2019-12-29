
import sys
from gatenlp import interact, GateNlpPr, Document

@GateNlpPr
class MyProcessor:

  def __call__(self, doc, **kwargs):
    print("!!!DEBUG PYTHON LENGTH=", len(doc))
    set2 = doc.get_annotations("Copy")
    set2.clear()
    set_def = list(doc.get_annotations())
    doc_ann = set_def[0]
    print("!!!DEBUG ANN FROM/TO/TYPE = {}/{}/{}".format(doc_ann.start, doc_ann.end, doc_ann.type))
    annid = set2.add(doc_ann.start, doc_ann.end, doc_ann.type, doc_ann.features)
    ann = set2.get(annid)
    ann.set_feature("python_start", ann.start)
    ann.set_feature("python_end", ann.end)

interact()
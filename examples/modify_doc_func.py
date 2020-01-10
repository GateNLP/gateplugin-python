"""Simple example PR to print the document text"""

import sys
from gatenlp import interact, GateNlpPr, Document
from mypackage import helpermodule

@GateNlpPr
def run(doc, **kwargs):
    print("We are running on a doc! kwargs={}".format(kwargs), file=sys.stderr)
    helpermodule.helperfunc()
    set1 = doc.get_annotations("PythonModifyFunc")
    set1.clear()
    set1.add(1,4,"Type1",{"f1":12, "f2": "val2"})
    doc.set_feature("FEAT", "VAL")
    doc.clear_features()
    doc.set_feature("feat1", 12)
    doc.set_feature("feat2", "asdf")
    doc.set_feature("feat1", 13)
    print("changelog", doc.changelog)
    
interact()

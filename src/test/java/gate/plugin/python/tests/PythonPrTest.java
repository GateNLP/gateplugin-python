/*
 * Copyright (c) 2019 The University of Sheffield.
 *
 * This file is part of gateplugin-Python 
 * (see https://github.com/GateNLP/gateplugin-Python).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package gate.plugin.python.tests;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Utils;
import gate.ProcessingResource;
import gate.creole.SerialAnalyserController;
import gate.plugin.python.PythonPr;
import gate.plugin.python.PythonPr.LoggingLevel;
import gate.test.GATEPluginTestCase;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;

/**
 * Class for testing.
 * @author Johann Petrak
 */
public class PythonPrTest extends GATEPluginTestCase {

  /**
   * A test.
   * @throws IOException   exception
   */
  public void testPython() throws IOException {
    Executor executor = new DefaultExecutor();
    //System.err.println("Python path:");
    CommandLine cmdLine1;
    if(PythonPr.isOsWindows()) {
      cmdLine1 = CommandLine.parse("cmd.exe /C where python");  
    } else {
      cmdLine1 = CommandLine.parse("/bin/bash which python");
    }
    int retval = executor.execute(cmdLine1);
    assertEquals(0, retval);
    
    cmdLine1 = CommandLine.parse("python -m py_compile src/test/python/debug.py");
    retval = executor.execute(cmdLine1);
    assertEquals(0, retval);

    cmdLine1 = CommandLine.parse("python src/test/python/debug.py");
    retval = executor.execute(cmdLine1);
    assertEquals(0, retval);    
  }
  /**
   * Very basic test.
   * @throws Exception  exception
   */
  public void testPythonPr00() throws Exception {
    FeatureMap params = Factory.newFeatureMap();
    params.put("encoding", "UTF-8");
    params.put("markupAware", true);
    params.put("sourceUrl",  new File("./src/test/docs/tiny1.xml").toURI().toURL());
    Document doc1 = (Document)Factory.createResource("gate.corpora.DocumentImpl", params);
    //System.err.println("!!!DEBUG: document text="+doc1.getContent().toString());
    // System.err.println("!!!DEBUG: document len="+doc1.getContent().size());
    AnnotationSet defset = doc1.getAnnotations();
    defset.clear();
    defset.add(0L, doc1.getContent().size(), "Document", Factory.newFeatureMap());
    ProcessingResource pr;
    params = Factory.newFeatureMap();
    params.put("pythonBinary", "python");
    params.put("pythonProgram", new File("./src/test/python/tiny1.py").toURI().toURL());
    pr = (ProcessingResource)Factory.createResource("gate.plugin.python.PythonPr",params);
    Corpus corpus = Factory.newCorpus("test");
    corpus.add(doc1);
    SerialAnalyserController controller = (SerialAnalyserController) Factory.createResource(
            "gate.creole.SerialAnalyserController");
    controller.add(pr);
    controller.setCorpus(corpus);
    controller.execute();
    
    AnnotationSet anns = doc1.getAnnotations("Copy");
    System.err.println("Got anns: "+anns);
    assertEquals(1, anns.size());
    Annotation ann = anns.iterator().next();
    //System.err.println("Got ann: "+ann);
    assertEquals("Document", ann.getType());
    assertEquals(0L, (long)ann.getStartNode().getOffset());
    assertEquals((long)doc1.getContent().size(), (long)ann.getEndNode().getOffset());
  }

  
  /**
   * Another test.
   * @throws Exception  exception
   */
  public void testPythonPr01() throws Exception {
    Document doc1 = Factory.newDocument("This is a small document");
    // add a document a null document feature and a null annotation feature
    // to check if this works correctly. The python code copies those 
    // values back into new document featrues
    //System.err.println("######## TEST Pr01");
    doc1.getFeatures().put("docfeature_null", null);
    doc1.getFeatures().put("docfeature_str", "asdf");
    doc1.getAnnotations().add(0L, 2L, "DEBUG", 
            gate.Utils.featureMap("annfeature_null", null, "annfeature_str", "asdf"));
    ProcessingResource pr;
    FeatureMap params = Factory.newFeatureMap();
    params.put("pythonBinary", "python");
    params.put("pythonProgram", new File("./src/test/python/test1.py").toURI().toURL());
    pr = (ProcessingResource)Factory.createResource("gate.plugin.python.PythonPr",params);
    Corpus corpus = Factory.newCorpus("test");
    corpus.add(doc1);
    SerialAnalyserController controller = (SerialAnalyserController) Factory.createResource(
            "gate.creole.SerialAnalyserController");
    controller.add(pr);
    controller.setCorpus(corpus);
    controller.execute();
    FeatureMap docfm = doc1.getFeatures();
    assertEquals(null, docfm.get("copy_docfeature_null"));
    assertEquals(null, docfm.get("copy_annfeature_null"));
    assertEquals("asdf", docfm.get("copy_docfeature_str"));
    assertEquals("asdf", docfm.get("copy_annfeature_str"));
    AnnotationSet anns = doc1.getAnnotations("Set1");
    //System.err.println("Got anns: "+anns);
    assertEquals(1, anns.size());
    Annotation ann = anns.iterator().next();
    // System.err.println("Got ann: "+ann);
    assertEquals("Type1", ann.getType());
    assertEquals(1L, (long)ann.getStartNode().getOffset());
    assertEquals(4L, (long)ann.getEndNode().getOffset());
    FeatureMap fm = ann.getFeatures();
    Object f1 = fm.get("f1");
    Object f2 = fm.get("f2");
    assertNotNull(f1);
    assertNotNull(f2);
    assertTrue(f1 instanceof Integer);
    assertTrue(f2 instanceof String);
    assertEquals(12, (int)f1);
    assertEquals("val2", (String)f2);
    fm = doc1.getFeatures();
    f1 = fm.get("feat1");
    f2 = fm.get("feat2");
    assertNotNull(f1);
    assertNotNull(f2);
    assertTrue(f1 instanceof Integer);
    assertTrue(f2 instanceof String);
    assertEquals(13, (int)f1);
    assertEquals("asdf", (String)f2);
  }

  /**
   * Another test.
   * Same as 01, but with a space in the python script path
   * @throws Exception  exception
   */
  public void testPythonPr01b() throws Exception {
    //System.err.println("######## TEST Pr01b");
    Document doc1 = Factory.newDocument("This is a small document");
    // add a document a null document feature and a null annotation feature
    // to check if this works correctly. The python code copies those 
    // values back into new document featrues
    doc1.getFeatures().put("docfeature_null", null);
    doc1.getFeatures().put("docfeature_str", "asdf");
    doc1.getAnnotations().add(0L, 2L, "DEBUG", 
            gate.Utils.featureMap("annfeature_null", null, "annfeature_str", "asdf"));
    ProcessingResource pr;
    FeatureMap params = Factory.newFeatureMap();
    params.put("pythonBinary", "python");
    params.put("pythonProgram", new File("./src/test/python/Sub Dir/test1.py").toURI().toURL());
    params.put("loggingLevel", LoggingLevel.DEBUG);
    pr = (ProcessingResource)Factory.createResource("gate.plugin.python.PythonPr",params);
    Corpus corpus = Factory.newCorpus("test");
    corpus.add(doc1);
    SerialAnalyserController controller = (SerialAnalyserController) Factory.createResource(
            "gate.creole.SerialAnalyserController");
    controller.add(pr);
    controller.setCorpus(corpus);
    controller.execute();
    FeatureMap docfm = doc1.getFeatures();
    assertEquals(null, docfm.get("copy_docfeature_null"));
    assertEquals(null, docfm.get("copy_annfeature_null"));
    assertEquals("asdf", docfm.get("copy_docfeature_str"));
    assertEquals("asdf", docfm.get("copy_annfeature_str"));
    AnnotationSet anns = doc1.getAnnotations("Set1");
    //System.err.println("Got anns: "+anns);
    assertEquals(1, anns.size());
    Annotation ann = anns.iterator().next();
    // System.err.println("Got ann: "+ann);
    assertEquals("Type1", ann.getType());
    assertEquals(1L, (long)ann.getStartNode().getOffset());
    assertEquals(4L, (long)ann.getEndNode().getOffset());
    FeatureMap fm = ann.getFeatures();
    Object f1 = fm.get("f1");
    Object f2 = fm.get("f2");
    assertNotNull(f1);
    assertNotNull(f2);
    assertTrue(f1 instanceof Integer);
    assertTrue(f2 instanceof String);
    assertEquals(12, (int)f1);
    assertEquals("val2", (String)f2);
    fm = doc1.getFeatures();
    f1 = fm.get("feat1");
    f2 = fm.get("feat2");
    assertNotNull(f1);
    assertNotNull(f2);
    assertTrue(f1 instanceof Integer);
    assertTrue(f2 instanceof String);
    assertEquals(13, (int)f1);
    assertEquals("asdf", (String)f2);
  }
  
  /**
   * Another test.
   * @throws Exception  exception
   */  
  public void testPythonPr02() throws Exception {
    //System.err.println("######## TEST Pr02");
    Document doc1 = Factory.newDocument("This is a small document");
    ProcessingResource pr;
    FeatureMap params = Factory.newFeatureMap();
    params.put("pythonBinary", "python");
    params.put("pythonProgram", new File("./src/test/python/test2.py").toURI().toURL());
    pr = (ProcessingResource)Factory.createResource("gate.plugin.python.PythonPr",params);
    Corpus corpus = Factory.newCorpus("test");
    corpus.add(doc1);
    SerialAnalyserController controller = (SerialAnalyserController) Factory.createResource(
            "gate.creole.SerialAnalyserController");
    controller.add(pr);
    controller.setCorpus(corpus);
    controller.execute();
    AnnotationSet anns = doc1.getAnnotations("Set1");
    //System.err.println("!!!!!!!!!!!!!1Got anns: "+anns);
    assertEquals(1, anns.size());
    Annotation ann = anns.iterator().next();
    //   System.err.println("Got ann: "+ann);
    assertEquals("Type1", ann.getType());
    assertEquals(1L, (long)ann.getStartNode().getOffset());
    assertEquals(4L, (long)ann.getEndNode().getOffset());
    FeatureMap fm = ann.getFeatures();
    Object f1 = fm.get("f1");
    Object f2 = fm.get("f2");
    assertNotNull(f1);
    assertNotNull(f2);
    assertTrue(f1 instanceof Integer);
    assertTrue(f2 instanceof String);
    assertEquals(12, (int)f1);
    assertEquals("val2", (String)f2);
    fm = doc1.getFeatures();
    f1 = fm.get("feat1");
    f2 = fm.get("feat2");
    assertNotNull(f1);
    assertNotNull(f2);
    assertTrue(f1 instanceof Integer);
    assertTrue(f2 instanceof String);
    assertEquals(13, (int)f1);
    assertEquals("asdf", (String)f2);
  }

  /**
   * Test sending over a selection of annotation sets.
   *
   * @throws Exception  exception
   */
  public void testPythonPr03() throws Exception {
    Document doc1 = Factory.newDocument("This is a small document");
    // Create two sets: Set1 and Set2 and add one annotation to each set
    AnnotationSet aset = doc1.getAnnotations("Set1");
    Utils.addAnn(aset, 0L,1L, "Type1", Utils.featureMap());
    aset = doc1.getAnnotations("Set2");
    Utils.addAnn(aset, 0L,1L, "Type2", Utils.featureMap());
    ProcessingResource pr;
    FeatureMap params = Factory.newFeatureMap();
    params.put("pythonBinary", "python");
    params.put("pythonProgram", new File("./src/test/python/test3.py").toURI().toURL());
    Set<String> ss =new HashSet<>();
    ss.add("Set1");
    params.put("setsToUse", ss);
    pr = (ProcessingResource)Factory.createResource("gate.plugin.python.PythonPr",params);
    Corpus corpus = Factory.newCorpus("test");
    corpus.add(doc1);
    SerialAnalyserController controller = (SerialAnalyserController) Factory.createResource(
            "gate.creole.SerialAnalyserController");
    controller.add(pr);
    controller.setCorpus(corpus);
    controller.execute();

    aset = doc1.getAnnotations("Set1");
    // Set1 was sent and should have the original with id 0 and a new one with id 1
    assertEquals(2, aset.size());
    Set<Integer> idset = new HashSet<>();
    for(Annotation a : aset) {
      idset.add(a.getId());
    }
    assert idset.contains(0);
    assert idset.contains(1);
    Annotation a0 = aset.get(0);
    assert a0.getType().equals("Type1");
    Annotation a1 = aset.get(1);
    assert a1.getType().equals("Type3");

    aset = doc1.getAnnotations("Set2");
    // Set2 should have the original with id=1, plus the new merged in with id=2
    assertEquals(2, aset.size());
    idset.clear();
    for(Annotation a : aset) {
      idset.add(a.getId());
    }
    assert idset.contains(1);
    assert idset.contains(2);
    a1 = aset.get(1);
    assert a1.getType().equals("Type2");
    Annotation a3 = aset.get(2);
    assert a3.getType().equals("Type4");

    aset = doc1.getAnnotations("Set3");
    assertEquals(1, aset.size());
    a1 = aset.get(0);
    assert a1.getType().equals("Type5");
  }

}

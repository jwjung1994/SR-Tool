/*
 * This file is part of the ONT API.
 * The contents of this file are subject to the LGPL License, Version 3.0.
 * Copyright (c) 2017, Avicomp Services, AO
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 * Alternatively, the contents of this file may be used under the terms of the Apache License, Version 2.0 in which case, the provisions of the Apache License Version 2.0 are applicable instead of those above.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package ru.avicomp.ontapi.tests;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import org.apache.log4j.Logger;
import org.hamcrest.core.IsEqual;
import org.junit.Assert;
import org.junit.Test;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.model.parameters.OntologyCopy;
import ru.avicomp.ontapi.*;
import ru.avicomp.ontapi.config.OntLoaderConfiguration;
import ru.avicomp.ontapi.internal.InternalObject;
import ru.avicomp.ontapi.internal.ReadHelper;
import ru.avicomp.ontapi.jena.ConcurrentGraph;
import ru.avicomp.ontapi.jena.OntModelFactory;
import ru.avicomp.ontapi.jena.impl.configuration.OntModelConfig;
import ru.avicomp.ontapi.jena.model.OntClass;
import ru.avicomp.ontapi.jena.model.OntEntity;
import ru.avicomp.ontapi.jena.model.OntGraphModel;
import ru.avicomp.ontapi.transforms.GraphTransformers;
import ru.avicomp.ontapi.utils.FileMap;
import ru.avicomp.ontapi.utils.OntIRI;
import ru.avicomp.ontapi.utils.ReadWriteUtils;
import ru.avicomp.ontapi.utils.SpinModels;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * to test core ({@link OntManagers})
 * (+ testing serialization_
 * <p>
 * Created by szuev on 22.12.2016.
 */
public class CommonManagerTest {

    private static final Logger LOGGER = Logger.getLogger(CommonManagerTest.class);

    @Test
    public void testBasics() throws OWLOntologyCreationException {
        final IRI fileIRI = IRI.create(ReadWriteUtils.getResourceURI("test1.ttl"));
        final OWLOntologyID id = OntIRI.create("http://dummy").toOwlOntologyID();

        Assert.assertNotSame("The same manager", OntManagers.createONT(), OntManagers.createONT());
        Assert.assertNotSame("The same concurrent manager", OntManagers.createConcurrentONT(), OntManagers.createConcurrentONT());

        OntologyManagerImpl m1 = (OntologyManagerImpl) OntManagers.createONT();
        Assert.assertFalse("Concurrent", m1.isConcurrent());

        OntologyModel ont1 = m1.loadOntology(fileIRI);
        OntologyModel ont2 = m1.createOntology(id);
        Assert.assertEquals("Incorrect num of ontologies", 2, m1.ontologies().count());
        Stream.of(ont1, ont2).forEach(o -> {
            Assert.assertEquals("Incorrect impl", OntologyModelImpl.class, ont1.getClass());
            Assert.assertNotEquals("Incorrect impl", OntologyModelImpl.Concurrent.class, ont1.getClass());
        });

        OntologyManagerImpl m2 = (OntologyManagerImpl) OntManagers.createConcurrentONT();
        Assert.assertTrue("Not Concurrent", m2.isConcurrent());
        OntologyModel ont3 = m2.loadOntology(fileIRI);
        OntologyModel ont4 = m2.createOntology(id);
        Assert.assertEquals("Incorrect num of ontologies", 2, m2.ontologies().count());
        Stream.of(ont3, ont4).forEach(o -> {
            Assert.assertNotEquals("Incorrect impl", OntologyModelImpl.class, ont3.getClass());
            Assert.assertEquals("Incorrect impl", OntologyModelImpl.Concurrent.class, ont3.getClass());
        });
    }

    @Test
    public void testConfigs() {
        OntologyManager m1 = OntManagers.createONT();
        OntologyManager m2 = OntManagers.createONT();
        OntLoaderConfiguration conf1 = m1.getOntologyLoaderConfiguration();
        conf1.setPersonality(OntModelConfig.ONT_PERSONALITY_LAX);
        OntLoaderConfiguration conf2 = m2.getOntologyLoaderConfiguration();
        conf2.setPersonality(OntModelConfig.ONT_PERSONALITY_STRICT);
        Assert.assertEquals("Not the same loader configs", conf1, conf2);
        Assert.assertEquals("Not the same personalities", conf1.getPersonality(), conf2.getPersonality());
        m1.setOntologyLoaderConfiguration(conf1.setPersonality(OntModelConfig.ONT_PERSONALITY_LAX));
        m2.setOntologyLoaderConfiguration(conf1.setPersonality(OntModelConfig.ONT_PERSONALITY_STRICT));
        Assert.assertNotEquals("The same personalities", m1.getOntologyLoaderConfiguration().getPersonality(),
                m2.getOntologyLoaderConfiguration().getPersonality());

        boolean doTransformation = !conf1.isPerformTransformation();
        m1.getOntologyLoaderConfiguration().setPerformTransformation(doTransformation);
        Assert.assertNotEquals("The 'perform transformation' flag is changed", doTransformation, m1.getOntologyLoaderConfiguration().isPerformTransformation());
        m1.setOntologyLoaderConfiguration(conf2.setPerformTransformation(doTransformation));
        Assert.assertEquals("The same 'perform transformation' flag", doTransformation, m1.getOntologyLoaderConfiguration().isPerformTransformation());

        GraphTransformers.Store store = new GraphTransformers.Store().add((GraphTransformers.Maker) graph -> null);
        OntLoaderConfiguration conf3 = m1.getOntologyLoaderConfiguration().setGraphTransformers(store);
        Assert.assertNotEquals("Graph transform action store is changed", store, m1.getOntologyLoaderConfiguration().getGraphTransformers());
        m1.setOntologyLoaderConfiguration(conf3);
        Assert.assertEquals("Can't set transform action store.", store, m1.getOntologyLoaderConfiguration().getGraphTransformers());
    }

    @Test
    public void testConcurrentManager() throws Exception {
        OWLOntologyManager m = OntManagers.createConcurrentONT();
        OWLOntology o1 = m.createOntology();
        OWLOntology o2 = m.loadOntology(IRI.create(ReadWriteUtils.getResourceFile("test1.ttl")));
        Assert.assertEquals("Expected 2 ontologies.", 2, m.ontologies().count());
        Assert.assertTrue("(1)Not concurrent model!", o1 instanceof OntologyModelImpl.Concurrent);
        Assert.assertTrue("(2)Not concurrent model!", o2 instanceof OntologyModelImpl.Concurrent);
        ReadWriteLock managerLock = ((OntologyManagerImpl) m).getLock();
        Assert.assertNotNull(managerLock);
        Assert.assertEquals(managerLock, ((OntologyModelImpl.Concurrent) o1).getLock());
        Assert.assertEquals(managerLock, ((OntologyModelImpl.Concurrent) o2).getLock());
        Assert.assertTrue("(1)Not concurrent graph!", ((OntologyModel) o1).asGraphModel().getBaseGraph() instanceof ConcurrentGraph);
        Assert.assertTrue("(2)Not concurrent graph!", ((OntologyModel) o2).asGraphModel().getBaseGraph() instanceof ConcurrentGraph);
        Assert.assertEquals(managerLock, ((ConcurrentGraph) ((OntologyModel) o1).asGraphModel().getBaseGraph()).lock());
        Assert.assertEquals(managerLock, ((ConcurrentGraph) ((OntologyModel) o2).asGraphModel().getBaseGraph()).lock());

        o1.axioms().forEach(LOGGER::debug);
        ((OntologyModel) o1).asGraphModel().createOntEntity(OntClass.class, "urn:c1").createIndividual("urn:i");
        OWLDataFactory df = m.getOWLDataFactory();
        o1.add(df.getOWLAnnotationAssertionAxiom(df.getOWLAnnotationProperty(IRI.create("urn:ap")), IRI.create("urn:c1"), df.getOWLLiteral("test", "e")));
        Assert.assertEquals(4, o1.getAxiomCount());
        ((OntologyModel) o1).clearCache();
        Assert.assertEquals("+ 1 declaration", 5, o1.getAxiomCount());

        o2.axioms().forEach(LOGGER::debug);
        Assert.assertEquals(12, o2.getAxiomCount());
        ((OntologyModel) o2).clearCache();
        Assert.assertEquals(12, o2.getAxiomCount());
    }

    @Test
    public void testSerialization() throws Exception {
        serializationTest(OntManagers.createONT());
    }

    @Test
    public void testSerializationWithConcurrency() throws Exception {
        serializationTest(OntManagers.createConcurrentONT());
    }

    @Test
    public void testCoping() throws Exception {
        copyTest(OntManagers.createONT(), OntManagers.createOWL(), OntologyCopy.SHALLOW);
        copyTest(OntManagers.createONT(), OntManagers.createOWL(), OntologyCopy.DEEP);
        copyTest(OntManagers.createOWL(), OntManagers.createONT(), OntologyCopy.SHALLOW);
        copyTest(OntManagers.createOWL(), OntManagers.createONT(), OntologyCopy.DEEP);
        copyTest(OntManagers.createONT(), OntManagers.createONT(), OntologyCopy.SHALLOW);
        copyTest(OntManagers.createONT(), OntManagers.createONT(), OntologyCopy.DEEP);
    }

    @Test
    public void testMoving() throws Exception {
        LOGGER.info("1) Move OWL -> ONT");
        OWLDataFactory df = OntManagers.getDataFactory();
        OWLOntologyManager m1 = OntManagers.createOWL();
        OWLOntologyManager m2 = OntManagers.createONT();
        OWLOntologyManager m3 = OntManagers.createONT();
        IRI iri1 = IRI.create("http://test/1");
        OWLOntology o1 = m1.createOntology(iri1);
        o1.add(df.getOWLSubClassOfAxiom(df.getOWLClass("a"), df.getOWLClass("b")));
        try {
            m2.copyOntology(o1, OntologyCopy.MOVE);
            Assert.fail("Moving from OWL to ONT should be disabled");
        } catch (OWLOntologyCreationException e) {
            LOGGER.info(e);
        }
        Assert.assertEquals("Incorrect ont-count in source", 1, m1.ontologies().count());
        Assert.assertEquals("Incorrect ont-count in destinaction", 0, m2.ontologies().count());

        LOGGER.info("2) Move ONT -> OWL");
        IRI iri2 = IRI.create("http://test/2");
        IRI docIRI = IRI.create("file://nothing");
        OWLDocumentFormat format = OntFormat.JSON_LD.createOwlFormat();
        OWLOntology o2 = m2.createOntology(iri2);
        m2.setOntologyFormat(o2, format);
        m2.setOntologyDocumentIRI(o2, docIRI);
        o2.add(df.getOWLEquivalentClassesAxiom(df.getOWLClass("a"), df.getOWLClass("b")));

        try {
            m1.copyOntology(o2, OntologyCopy.MOVE);
            Assert.fail("Expected exception while moving from ONT -> OWL");
        } catch (OntApiException a) {
            LOGGER.info(a);
        }
        // check ONT manager
        // And don't care about OWL manager, we can't help him anymore.
        Assert.assertTrue("Can't find " + iri2, m2.contains(iri2));
        Assert.assertTrue("Can't find " + o2, m2.contains(o2));
        Assert.assertSame("Incorrect manager!", m2, o2.getOWLOntologyManager());
        Assert.assertEquals("Incorrect document IRI", docIRI, m2.getOntologyDocumentIRI(o2));
        Assert.assertEquals("Incorrect format", format, m2.getOntologyFormat(o2));

        LOGGER.info("3) Move ONT -> ONT");
        Assert.assertSame("Not same ontology!", o2, m3.copyOntology(o2, OntologyCopy.MOVE));
        Assert.assertTrue("Can't find " + iri2, m3.contains(iri2));
        Assert.assertFalse("There is still " + iri2, m2.contains(iri2));
        Assert.assertTrue("Can't find " + o2, m3.contains(o2));
        Assert.assertFalse("There is still " + o2, m2.contains(o2));
        Assert.assertSame("Not the same ontology", o2, m3.getOntology(iri2));
        Assert.assertSame("Incorrect manager!", m3, o2.getOWLOntologyManager());
        Assert.assertEquals("Incorrect document IRI", docIRI, m3.getOntologyDocumentIRI(o2));
        Assert.assertEquals("Incorrect format", format, m3.getOntologyFormat(o2));
        Assert.assertNull("Still have ont-format", m2.getOntologyFormat(o2));
        try {
            Assert.fail("Expected exception, but found some doc iri " + m2.getOntologyDocumentIRI(o2));
        } catch (UnknownOWLOntologyException u) {
            LOGGER.info(u);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testCopyWholeManager() throws Exception {
        IRI iri1 = IRI.create("http://spinrdf.org/sp");
        IRI doc1 = IRI.create(ReadWriteUtils.getResourcePath("etc", "sp.ttl").toUri());
        IRI iri2 = IRI.create("http://spinrdf.org/spin");
        IRI doc2 = IRI.create(ReadWriteUtils.getResourcePath("etc", "spin.ttl").toUri());

        OntologyManager orig = OntManagers.createONT();
        orig.getIRIMappers().add(FileMap.create(iri1, doc1));
        orig.getIRIMappers().add(FileMap.create(iri2, doc2));
        orig.loadOntologyFromOntologyDocument(iri2);
        Assert.assertEquals(2, orig.ontologies().count());

        LOGGER.info("Copy manager");
        OntologyManager copy = copyManager(orig);
        Assert.assertEquals(2, copy.ontologies().count());

        // validate doc iris:
        Assert.assertEquals(doc1, copy.getOntologyDocumentIRI(copy.getOntology(iri1)));
        // Note: the same behaviour as OWL-API (tested: 5.1.4): the primary ontology has ontology-iri as document-iri.
        Assert.assertEquals(iri2, copy.getOntologyDocumentIRI(copy.getOntology(iri2)));
    }

    @Test
    public void testLoadAnnotationsOption() {
        OntologyManager m = OntManagers.createONT();
        Assert.assertEquals("Incorrect default settings", true, m.getOntologyLoaderConfiguration().isLoadAnnotationAxioms());
        OWLDataFactory df = m.getOWLDataFactory();

        OntologyModel o1 = m.createOntology();
        OWLClass cl = df.getOWLClass(IRI.create("C"));
        OWLAnnotationProperty ap = df.getOWLAnnotationProperty(IRI.create("http://a"));
        OWLAnnotation a1 = df.getOWLAnnotation(ap, df.getOWLLiteral("assertion1"));
        OWLAnnotation a2 = df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral("assertion2"));

        o1.add(df.getOWLDeclarationAxiom(cl));
        o1.add(df.getOWLDeclarationAxiom(ap));
        o1.add(df.getOWLAnnotationPropertyDomainAxiom(ap, IRI.create("domain")));
        o1.add(df.getOWLAnnotationPropertyRangeAxiom(ap, IRI.create("range")));
        o1.add(df.getOWLAnnotationAssertionAxiom(cl.getIRI(), a1));
        o1.add(df.getOWLAnnotationAssertionAxiom(cl.getIRI(), a2));
        List<OWLAxiom> axioms = o1.axioms().collect(Collectors.toList());
        axioms.forEach(LOGGER::debug);
        ReadWriteUtils.print(o1);

        LOGGER.info("Change Load Annotation settings");
        m.setOntologyLoaderConfiguration(m.getOntologyLoaderConfiguration().setLoadAnnotationAxioms(false));
        Assert.assertEquals("Incorrect settings", false, m.getOntologyLoaderConfiguration().isLoadAnnotationAxioms());
        // check the axioms changed.
        List<OWLAxiom> axioms1 = o1.axioms().collect(Collectors.toList());
        axioms1.forEach(LOGGER::debug);
        Assert.assertEquals("Should be 2 axioms only", 2, axioms1.size());
        Assert.assertTrue("Can't find declaration for " + ap, axioms1.contains(df.getOWLDeclarationAxiom(ap)));
        Assert.assertTrue("The declaration for " + cl + " should be with annotations now", axioms1.contains(df.getOWLDeclarationAxiom(cl, Arrays.asList(a1, a2))));

        LOGGER.info("Create new ontology ");
        OntologyModel o2 = m.createOntology();
        axioms.forEach(o2::add);
        ReadWriteUtils.print(o2);
        List<OWLAxiom> axioms2 = o2.axioms().collect(Collectors.toList());
        axioms2.forEach(LOGGER::debug);
        Assert.assertEquals("Should be 2 axioms only", 2, axioms2.size());
        Assert.assertTrue("Can't find declaration for " + ap, axioms2.contains(df.getOWLDeclarationAxiom(ap)));
        Assert.assertTrue("The declaration for " + cl + " should not be unannotated now", axioms2.contains(df.getOWLDeclarationAxiom(cl)));
    }

    @Test
    public void testBulkAnnotationsSetting() throws Exception {
        OntologyManager m = OntManagers.createONT();
        Assert.assertEquals("Incorrect default settings", true, m.getOntologyLoaderConfiguration().isAllowBulkAnnotationAssertions());
        OWLDataFactory df = m.getOWLDataFactory();

        OntologyModel o1 = m.createOntology();
        OWLClass cl = df.getOWLClass(IRI.create("http://class"));
        OWLAnnotation a1 = df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral("plain assertion"));
        OWLAnnotation a2 = df.getOWLAnnotation(df.getRDFSLabel(), df.getOWLLiteral("bulk assertion"),
                df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral("the child")));
        Set<OWLAxiom> axioms1 = Stream.of(
                df.getOWLDeclarationAxiom(cl),
                df.getOWLAnnotationAssertionAxiom(cl.getIRI(), a1),
                df.getOWLAnnotationAssertionAxiom(cl.getIRI(), a2)
        ).collect(Collectors.toSet());
        LOGGER.debug("Axioms to be added: ");
        axioms1.forEach(LOGGER::debug);
        axioms1.forEach(o1::add);

        LOGGER.debug("Create second ontology with the same content.");
        String txt = ReadWriteUtils.toString(o1, OntFormat.TURTLE);
        LOGGER.debug("\n" + txt);
        OWLOntology o2 = m.loadOntologyFromOntologyDocument(ReadWriteUtils.toInputStream(txt));
        Assert.assertEquals("Incorrect axioms collection in the copied ontology", axioms1, o2.axioms().collect(Collectors.toSet()));

        LOGGER.info("Change Allow Bulk Annotation Assertion setting");
        m.setOntologyLoaderConfiguration(m.getOntologyLoaderConfiguration().setAllowBulkAnnotationAssertions(false));
        Assert.assertEquals("Incorrect settings", false, m.getOntologyLoaderConfiguration().isAllowBulkAnnotationAssertions());
        o1.axioms().forEach(LOGGER::debug);
        Set<OWLAxiom> axioms2 = Stream.of(
                df.getOWLAnnotationAssertionAxiom(cl.getIRI(), a1),
                df.getOWLDeclarationAxiom(cl, Stream.of(a2).collect(Collectors.toSet()))
        ).collect(Collectors.toSet());

        Assert.assertEquals("Incorrect axioms count", axioms2.size(), o1.getAxiomCount());
        Assert.assertEquals("Incorrect axioms collection in the first ontology", axioms2, o1.axioms().collect(Collectors.toSet()));
        Assert.assertEquals("Incorrect axioms collection in the second ontology", axioms2, o2.axioms().collect(Collectors.toSet()));
        LOGGER.debug("Create third ontology with the same content.");
        OWLOntology o3 = m.loadOntologyFromOntologyDocument(ReadWriteUtils.toInputStream(txt));
        Assert.assertEquals("Incorrect axioms collection in the third ontology", axioms2, o3.axioms().collect(Collectors.toSet()));
    }


    @Test
    public void testPassingGraph() throws Exception {
        LOGGER.info("Build MultiUnion graph using jena OntModel");
        OntModelSpec spec = OntModelSpec.OWL_DL_MEM;
        FileManager jenaFileManager = spec.getDocumentManager().getFileManager();
        SpinModels.addMappings(jenaFileManager);
        OntModel ontologyModel = ModelFactory.createOntologyModel(spec);
        ontologyModel.read(SpinModels.SPINMAPL.getIRI().getIRIString(), "ttl");

        LOGGER.info("Load spin-rdf ontology family using file-iri-mappings");
        OntologyManager m1 = OntManagers.createONT();
        SpinModels.addMappings(m1);
        m1.loadOntologyFromOntologyDocument(SpinModels.SPINMAPL.getIRI());
        long expected = m1.ontologies().count();

        LOGGER.info("Pass ready composite graph to the manager as-is");
        OntologyManager m2 = OntManagers.createONT();
        m2.addOntology(ontologyModel.getGraph());
        long actual = m2.ontologies().count();

        Assert.assertEquals("Counts of ontologies does not match", expected, actual);

        LOGGER.info("Add several additional ontologies");
        m2.addOntology(OntModelFactory.createDefaultGraph());
        OntGraphModel o2 = OntModelFactory.createModel();
        o2.setID("http://example.org/test");
        m2.addOntology(o2.getGraph());
        Assert.assertEquals("Counts of ontologies does not match", expected + 2, m2.ontologies().count());

    }

    private static void copyTest(OWLOntologyManager from, OWLOntologyManager to, OntologyCopy mode) throws Exception {
        LOGGER.info("Copy (" + mode + ") " + from.getClass().getInterfaces()[0].getSimpleName() + " -> " + to.getClass().getInterfaces()[0].getSimpleName());
        long fromCount = from.ontologies().count();
        long toCount = to.ontologies().count();

        OWLDataFactory df = from.getOWLDataFactory();
        IRI iri = IRI.create("test" + System.currentTimeMillis());
        LOGGER.debug("Create ontology " + iri);
        OWLClass clazz = df.getOWLClass("x");
        OWLOntology o1 = from.createOntology(iri);
        o1.add(df.getOWLDeclarationAxiom(clazz));

        to.copyOntology(o1, OntologyCopy.DEEP);
        Assert.assertEquals("Incorrect ontologies count inside OWL-manager", fromCount + 1, from.ontologies().count());
        Assert.assertEquals("Incorrect ontologies count inside ONT-manager", toCount + 1, to.ontologies().count());
        Assert.assertTrue("Can't find " + iri, to.contains(iri));
        OWLOntology o2 = to.getOntology(iri);
        Assert.assertNotNull("Can't find " + to, o2);
        Assert.assertNotSame("Should not be same", o1, o2);
        Set<OWLClass> classes = o2.classesInSignature().collect(Collectors.toSet());
        Assert.assertEquals("Should be single class inside", 1, classes.size());
        Assert.assertTrue("Can't find " + clazz, classes.contains(clazz));
    }

    /**
     * Copies managers content to new instance
     *
     * @param from {@link OntologyManager}
     * @return new instance of {@link OntologyManager}
     */
    public static OntologyManager copyManager(OntologyManager from) {
        OntologyManager res = OntManagers.createONT();
        copyManager(from, res, false);
        return res;
    }

    /**
     * Copies managers content.
     *
     * @param from     source
     * @param to       destination
     * @param silently if true ignore {@link OWLOntologyCreationException} while coping
     * @throws OntApiException if there are some exceptions and {@code silently = true}
     */
    public static void copyManager(OntologyManager from, OntologyManager to, boolean silently) throws OntApiException {
        OntApiException ex = new OntApiException("Can't copy manager:");
        from.ontologies()
                .sorted(Comparator.comparingInt(o -> (int) o.imports().count()))
                .forEach(o -> {
                    try {
                        to.copyOntology(o, OntologyCopy.DEEP);
                    } catch (OWLOntologyCreationException e) {
                        ex.addSuppressed(e);
                    }
                });
        if (!silently && ex.getSuppressed().length != 0) {
            throw ex;
        }
    }

    private static void serializationTest(OWLOntologyManager origin) throws Exception {
        setUpManager(origin);
        debugManager(origin);

        LOGGER.info("|====================|");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream stream = new ObjectOutputStream(out);
        stream.writeObject(origin);
        stream.flush();

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        ObjectInputStream inStream = new ObjectInputStream(in);
        OWLOntologyManager copy = (OWLOntologyManager) inStream.readObject();
        if (OntologyManager.class.isInstance(origin)) {
            Assert.assertEquals("Incorrect concurrency", ((OntologyManagerImpl) origin).isConcurrent(), ((OntologyManagerImpl) copy).isConcurrent());
        }

        fixAfterSerialization(origin, copy);
        debugManager(copy);
        compareManagersTest(origin, copy);

        if (OntologyManager.class.isInstance(origin)) {
            editManagerTest((OntologyManager) origin, (OntologyManager) copy);
        }
    }

    private static void fixAfterSerialization(OWLOntologyManager origin, OWLOntologyManager copy) {
        if (OntologyManager.class.isInstance(copy)) {
            return;
        }
        // OWL-API 5.0.5:
        copy.setOntologyWriterConfiguration(origin.getOntologyWriterConfiguration());
    }

    private static void debugManager(OWLOntologyManager m) {
        m.ontologies().forEach(o -> {
            LOGGER.debug("<" + o.getOntologyID() + ">:");
            ReadWriteUtils.print(o);
        });
    }

    @SuppressWarnings({"OptionalGetWithoutIsPresent", "ConstantConditions"})
    private static OWLOntologyManager setUpManager(OWLOntologyManager m) throws OWLOntologyCreationException {
        OWLDataFactory f = m.getOWLDataFactory();

        OWLOntology a1 = m.createOntology();
        a1.add(f.getOWLDeclarationAxiom(f.getOWLClass(IRI.create("urn:iri.com#Class1"))));
        OWLOntology a2 = m.createOntology();
        a2.add(f.getOWLDeclarationAxiom(f.getOWLClass(IRI.create("urn:iri.com#Class2"))));

        OWLOntology i1 = m.createOntology(IRI.create("urn:iri.com#1"));
        i1.add(f.getOWLDeclarationAxiom(f.getOWLClass(IRI.create("urn:iri.com#Class3"))));
        OWLOntology i2 = m.createOntology(IRI.create("urn:iri.com#2"));
        i2.add(f.getOWLDeclarationAxiom(f.getOWLClass(IRI.create("urn:iri.com#Class4"))));
        OWLOntology i3 = m.createOntology(IRI.create("urn:iri.com#3"));
        i3.add(f.getOWLDeclarationAxiom(f.getOWLClass(IRI.create("urn:iri.com#Class5"))));

        i1.applyChange(new AddImport(i1, f.getOWLImportsDeclaration(i2.getOntologyID().getOntologyIRI().get())));
        i2.applyChange(new AddImport(i2, f.getOWLImportsDeclaration(i3.getOntologyID().getOntologyIRI().get())));
        i1.applyChange(new AddImport(i1, f.getOWLImportsDeclaration(IRI.create("urn:some.import"))));
        a1.applyChange(new AddImport(a1, f.getOWLImportsDeclaration(i1.getOntologyID().getOntologyIRI().get())));

        i2.getFormat().asPrefixOWLDocumentFormat().setPrefix("test", "urn:iri.com");
        return m;
    }

    private static void editManagerTest(OntologyManager origin, OntologyManager copy) {
        String uri = "urn:iri.com#1";
        OntGraphModel o1 = origin.getGraphModel(uri);
        OntGraphModel o2 = copy.getGraphModel(uri);

        List<OntClass> classes1 = o1.listClasses().collect(Collectors.toList());
        // create two new classes inside original manager (in two models).
        o1.createOntEntity(OntClass.class, "http://some/new#Class1");
        origin.getGraphModel("urn:iri.com#3").createOntEntity(OntClass.class, "http://some/new#Class2");
        List<OntClass> classes2 = o2.listClasses().collect(Collectors.toList());
        // check that in the second (copied) manager there is no changes:
        Assert.assertEquals("incorrect classes", classes1, classes2);

        // create two new classes inside copied manager.
        Set<OntClass> classes3 = o2.listClasses().collect(Collectors.toSet());
        OntClass cl3 = o2.createOntEntity(OntClass.class, "http://some/new#Class3");
        OntClass cl4 = copy.getGraphModel("urn:iri.com#3").createOntEntity(OntClass.class, "http://some/new#Class4");
        List<OntClass> newClasses = Arrays.asList(cl3, cl4);
        classes3.addAll(newClasses);
        Set<OntClass> classes4 = o2.listClasses().collect(Collectors.toSet());
        Assert.assertEquals("incorrect classes", classes3, classes4);
        newClasses.forEach(c ->
                Assert.assertFalse("Found " + c + " inside original ontology", o1.containsResource(c))
        );
        OntologyModel ont = copy.getOntology(IRI.create(uri));
        OWLDataFactory df = copy.getOWLDataFactory();
        Assert.assertNotNull(ont);
        List<OWLClass> newOWLClasses = newClasses.stream()
                .map(ce -> ReadHelper.fetchClassExpression(ce, df))
                .map(InternalObject::getObject)
                .map(AsOWLClass::asOWLClass).collect(Collectors.toList());
        LOGGER.debug("OWL-Classes: " + newOWLClasses);
        newOWLClasses.forEach(c ->
                Assert.assertTrue("Can't find " + c + " inside copied ontology", ont.containsReference(c, Imports.INCLUDED))
        );
    }

    public static void compareManagersTest(OWLOntologyManager expected, OWLOntologyManager actual) {
        Assert.assertEquals("Incorrect number of ontologies.", expected.ontologies().count(), actual.ontologies().count());
        actual.ontologies().forEach(test -> {
            OWLOntologyID id = test.getOntologyID();
            LOGGER.debug("Test <" + id + ">");
            OWLOntology origin = expected.getOntology(id);
            Assert.assertNotNull("Can't find init ontology with id " + id, origin);
            AxiomType.AXIOM_TYPES.forEach(t -> {
                Set<OWLAxiom> expectedAxiom = origin.axioms(t).collect(Collectors.toSet());
                Set<OWLAxiom> actualAxiom = test.axioms(t).collect(Collectors.toSet());
                Assert.assertThat(String.format("Incorrect axioms for type <%s> and %s (expected=%d, actual=%d)", t, id, expectedAxiom.size(), actualAxiom.size()),
                        actualAxiom, IsEqual.equalTo(expectedAxiom));
            });

            Set<OWLImportsDeclaration> expectedImports = origin.importsDeclarations().collect(Collectors.toSet());
            Set<OWLImportsDeclaration> actualImports = test.importsDeclarations().collect(Collectors.toSet());
            Assert.assertEquals("Incorrect imports for " + id, expectedImports, actualImports);
            OWLDocumentFormat expectedFormat = origin.getFormat();
            OWLDocumentFormat actualFormat = test.getFormat();
            Assert.assertEquals("Incorrect formats for " + id, expectedFormat, actualFormat);
            Map<String, String> expectedPrefixes = expectedFormat != null && expectedFormat.isPrefixOWLDocumentFormat() ?
                    expectedFormat.asPrefixOWLDocumentFormat().getPrefixName2PrefixMap() : null;
            Map<String, String> actualPrefixes = actualFormat != null && actualFormat.isPrefixOWLDocumentFormat() ?
                    actualFormat.asPrefixOWLDocumentFormat().getPrefixName2PrefixMap() : null;
            Assert.assertEquals("Incorrect prefixes for " + id, expectedPrefixes, actualPrefixes);
            compareEntitiesTest(origin, test);
        });
    }

    @SuppressWarnings("ConstantConditions")
    public static void compareEntitiesTest(OWLOntology expectedOnt, OWLOntology actualOnt) {
        for (Imports i : Imports.values()) {
            Set<OWLEntity> actualEntities = actualOnt.signature(i).collect(Collectors.toSet());
            Set<OWLEntity> expectedEntities = expectedOnt.signature(i).collect(Collectors.toSet());
            LOGGER.debug("OWL entities: " + actualEntities);
            Assert.assertEquals("Incorrect owl entities", expectedEntities, actualEntities);
        }
        if (OntologyModel.class.isInstance(actualOnt) && OntologyModel.class.isInstance(expectedOnt)) {  // ont
            List<OntEntity> actualEntities = ((OntologyModel) actualOnt).asGraphModel().ontEntities().collect(Collectors.toList());
            List<OntEntity> expectedEntities = ((OntologyModel) expectedOnt).asGraphModel().ontEntities().collect(Collectors.toList());
            LOGGER.debug("ONT entities: " + actualEntities);
            Assert.assertEquals("Incorrect ont entities", expectedEntities, actualEntities);
        }
    }

}
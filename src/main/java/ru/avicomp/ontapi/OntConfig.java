package ru.avicomp.ontapi;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.semanticweb.owlapi.model.*;

import ru.avicomp.ontapi.jena.impl.configuration.OntModelConfig;
import ru.avicomp.ontapi.jena.impl.configuration.OntPersonality;
import ru.avicomp.ontapi.transforms.GraphTransformers;

/**
 * The config builder.
 * see base class {@link org.semanticweb.owlapi.model.OntologyConfigurator}
 * Created by szuev on 27.02.2017.
 */
public class OntConfig extends OntologyConfigurator {

    @Override
    public LoaderConfiguration buildLoaderConfiguration() {
        return new LoaderConfiguration(super.buildLoaderConfiguration());
    }

    public static OntConfig copy(OntologyConfigurator from) {
        OntConfig res = new OntConfig();
        if (from == null) return res;
        try {
            Field ignoredImports = from.getClass().getDeclaredField("ignoredImports");
            ignoredImports.setAccessible(true);
            ignoredImports.set(res, ignoredImports.get(from));
            Field overrides = from.getClass().getDeclaredField("overrides");
            overrides.setAccessible(true);
            overrides.set(res, overrides.get(from));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new OntApiException("Can't copy configuration.", e);
        }
        return res;
    }

    /**
     * Extended {@link OWLOntologyLoaderConfiguration} with ONT-API specific settings.
     * It is a wrapper since all members of original base class are private.
     * TODO: new (ONT-API) options should be configured in global ({@link OntConfig}) config also.
     */
    @SuppressWarnings({"NullableProblems", "WeakerAccess"})
    public static class LoaderConfiguration extends OWLOntologyLoaderConfiguration {
        protected final OWLOntologyLoaderConfiguration inner;
        // WARNING: OntPersonality is not serializable:
        protected transient OntPersonality personality;
        protected GraphTransformers.Store transformers;
        protected boolean performTransformation = true;
        protected HashSet<Scheme> supportedSchemes;
        protected boolean allowBulkAnnotationAssertions = true;

        public LoaderConfiguration(OWLOntologyLoaderConfiguration owl) {
            this.inner = owl == null ? new OWLOntologyLoaderConfiguration() :
                    owl instanceof LoaderConfiguration ? ((LoaderConfiguration) owl).inner : owl;
        }

        /**
         * the analogue of{@link super#copyConfiguration()}, since the original method is private.
         *
         * @param owl to copy from.
         * @return new instance of {@link LoaderConfiguration}
         */
        protected LoaderConfiguration copy(OWLOntologyLoaderConfiguration owl) {
            LoaderConfiguration res = new LoaderConfiguration(owl);
            res.personality = this.personality;
            res.transformers = this.transformers;
            res.performTransformation = this.performTransformation;
            res.supportedSchemes = this.supportedSchemes;
            res.allowBulkAnnotationAssertions = this.allowBulkAnnotationAssertions;
            return res;
        }

        /**
         * ONT-API config method.
         *
         * @return if true some graph transformations will be performed after loading graph.
         * @see #getGraphTransformers()
         * @see #setGraphTransformers(GraphTransformers.Store)
         */
        public boolean isPerformTransformation() {
            return performTransformation;
        }

        /**
         * ONT-API config setter.
         *
         * @param b if false all graph transformations will be disabled.
         * @return {@link LoaderConfiguration}
         * @see #getGraphTransformers()
         * @see #setGraphTransformers(GraphTransformers.Store)
         */
        public LoaderConfiguration setPerformTransformation(boolean b) {
            if (b == performTransformation) return this;
            LoaderConfiguration res = copy(inner);
            res.performTransformation = b;
            return res;
        }

        /**
         * ONT-API config method.
         * Note: after deserialization it is always default.
         *
         * @return the {@link org.apache.jena.enhanced.Personality}, if null then default ({@link OntModelConfig#getPersonality()})
         */
        public OntPersonality getPersonality() {
            return personality == null ? personality = OntModelConfig.getPersonality() : personality;
        }

        /**
         * ONT-API config setter.
         *
         * @param p {@link OntPersonality} new personality. Null means default ({@link OntModelConfig#getPersonality()})
         * @return {@link LoaderConfiguration}
         */
        public LoaderConfiguration setPersonality(OntPersonality p) {
            if (Objects.equals(personality, p)) return this;
            LoaderConfiguration res = copy(inner);
            res.personality = p;
            return res;
        }

        /**
         * ONT-API config method.
         *
         * @return {@link GraphTransformers.Store} a collection with {@link GraphTransformers.Maker}s.
         * @see #isPerformTransformation()
         * @see #setPerformTransformation(boolean)
         */
        public GraphTransformers.Store getGraphTransformers() {
            return transformers == null ? transformers = GraphTransformers.getTransformers() : transformers;
        }

        /**
         * ONT-API config setter.
         *
         * @param t {@link GraphTransformers.Store} new graph transformers store. null means default
         * @return {@link LoaderConfiguration}
         * @see #isPerformTransformation()
         * @see #setPerformTransformation(boolean)
         */
        public LoaderConfiguration setGraphTransformers(GraphTransformers.Store t) {
            if (Objects.equals(transformers, t)) return this;
            LoaderConfiguration res = copy(inner);
            res.transformers = t;
            return res;
        }

        /**
         * ONT-API config method.
         *
         * @return Set of {@link Scheme}
         */
        public Set<Scheme> getSupportedSchemes() {
            return supportedSchemes == null ? supportedSchemes = DefaultScheme.all().collect(Collectors.toCollection(HashSet::new)) : supportedSchemes;
        }

        /**
         * ONT-API config setter.
         *
         * @param schemes the collection of {@link Scheme}
         * @return {@link LoaderConfiguration}
         */
        public LoaderConfiguration setSupportedSchemes(Collection<Scheme> schemes) {
            if (Objects.equals(supportedSchemes, schemes)) return this;
            LoaderConfiguration res = copy(inner);
            res.supportedSchemes = new HashSet<>(schemes);
            return res;
        }

        /**
         * ONT-API config method.
         * The additional to the {@link #isLoadAnnotationAxioms()} optional setting to manage behaviour of annotation axioms.
         * By default annotated annotation assertions are allowed.
         * See the example in the description of {@link #setAllowBulkAnnotationAssertions(boolean)}
         *
         * @return true if annotation assertions could be annotated.
         * @see #setAllowBulkAnnotationAssertions(boolean)
         * @see #setLoadAnnotationAxioms(boolean)
         * @see #isLoadAnnotationAxioms()
         */
        public boolean isAllowBulkAnnotationAssertions() {
            return allowBulkAnnotationAssertions;
        }

        /**
         * ONT-API config setter.
         * This option manages annotation assertion axioms in conjunction with declaration axioms.
         * In depends of parameter specified bulk annotations fall either into declaration or annotation assertion.
         * Consider the following example:
         * <pre>
         * <http://class>   a                       owl:Class ;
         *                  rdfs:comment            "plain assertion" ;
         *                  rdfs:label              "bulk assertion" .
         * [                a                       owl:Axiom ;
         *                  rdfs:comment            "the child" ;
         *                  owl:annotatedProperty   rdfs:label ;
         *                  owl:annotatedSource     <http://class> ;
         *                  owl:annotatedTarget     "bulk assertion"
         * ] .
         * </pre>
         * In case {@link #isAllowBulkAnnotationAssertions()} equals {@code true} this slice of graph corresponds to the following list of axioms:
         * * AnnotationAssertion(rdfs:comment <http://class> "plain assertion"^^xsd:string)
         * * AnnotationAssertion(Annotation(rdfs:comment "the child"^^xsd:string) rdfs:label <http://class> "bulk assertion"^^xsd:string)
         * * Declaration(Class(<http://class>))
         * In case {@link #isAllowBulkAnnotationAssertions()} equals {@code false} there would be following axioms:
         * * Declaration(Annotation(Annotation(rdfs:comment "the child"^^xsd:string) rdfs:label "bulk assertion"^^xsd:string) Class(<http://class>))
         * * AnnotationAssertion(rdfs:comment <http://class> "plain assertion"^^xsd:string)
         * Note: the {@link org.semanticweb.owlapi.formats.ManchesterSyntaxDocumentFormat} does NOT work correctly
         * in the second case (to test try to reload ontology in manchester syntax. The loss of annotations is expected).
         *
         * @param b if false only plain annotation assertions axioms expected.
         * @return {@link LoaderConfiguration}
         * @see #isAllowBulkAnnotationAssertions()
         * @see #setLoadAnnotationAxioms(boolean)
         * @see #isLoadAnnotationAxioms()
         * @see OntFormat#MANCHESTER_SYNTAX
         */
        public LoaderConfiguration setAllowBulkAnnotationAssertions(boolean b) {
            if (b == allowBulkAnnotationAssertions) return this;
            LoaderConfiguration res = copy(inner);
            res.allowBulkAnnotationAssertions = b;
            return res;
        }

        @Override
        public LoaderConfiguration addIgnoredImport(IRI iri) {
            return copy(inner.addIgnoredImport(iri));
        }

        @Override
        public LoaderConfiguration clearIgnoredImports() {
            return copy(inner.clearIgnoredImports());
        }

        @Override
        public boolean isIgnoredImport(IRI iri) {
            return inner.isIgnoredImport(iri);
        }

        @Override
        public LoaderConfiguration removeIgnoredImport(IRI ontologyDocumentIRI) {
            return copy(inner.removeIgnoredImport(ontologyDocumentIRI));
        }

        @Override
        public PriorityCollectionSorting getPriorityCollectionSorting() {
            return inner.getPriorityCollectionSorting();
        }

        @Override
        public LoaderConfiguration setPriorityCollectionSorting(PriorityCollectionSorting sorting) {
            return copy(inner.setPriorityCollectionSorting(sorting));
        }

        @Override
        public int getConnectionTimeout() {
            return inner.getConnectionTimeout();
        }

        @Override
        public LoaderConfiguration setConnectionTimeout(int l) {
            return copy(inner.setConnectionTimeout(l));
        }

        @Override
        public MissingImportHandlingStrategy getMissingImportHandlingStrategy() {
            return inner.getMissingImportHandlingStrategy();
        }

        @Override
        public LoaderConfiguration setMissingImportHandlingStrategy(MissingImportHandlingStrategy missingImportHandlingStrategy) {
            return copy(inner.setMissingImportHandlingStrategy(missingImportHandlingStrategy));
        }

        @Override
        public MissingOntologyHeaderStrategy getMissingOntologyHeaderStrategy() {
            return inner.getMissingOntologyHeaderStrategy();
        }

        @Override
        public LoaderConfiguration setMissingOntologyHeaderStrategy(MissingOntologyHeaderStrategy missingOntologyHeaderStrategy) {
            return copy(inner.setMissingOntologyHeaderStrategy(missingOntologyHeaderStrategy));
        }

        @Override
        public int getRetriesToAttempt() {
            return inner.getRetriesToAttempt();
        }

        @Override
        public LoaderConfiguration setRetriesToAttempt(int retries) {
            return copy(inner.setRetriesToAttempt(retries));
        }

        @Override
        public boolean isAcceptingHTTPCompression() {
            return inner.isAcceptingHTTPCompression();
        }

        @Override
        public LoaderConfiguration setAcceptingHTTPCompression(boolean b) {
            OWLOntologyLoaderConfiguration copy = inner.setAcceptingHTTPCompression(b);
            return new LoaderConfiguration(copy);
        }

        @Override
        public boolean isFollowRedirects() {
            return inner.isFollowRedirects();
        }

        @Override
        public LoaderConfiguration setFollowRedirects(boolean value) {
            return copy(inner.setFollowRedirects(value));
        }

        /**
         * Determines whether or not annotation axioms (instances of {@code OWLAnnotationAxiom}) should be loaded.
         * By default the loading of annotation axioms is enabled.
         *
         * @return if {@code false} all annotation axioms (assertion, range and domain) will be discarded on loading.
         * @see OWLOntologyLoaderConfiguration#isLoadAnnotationAxioms()
         */
        @Override
        public boolean isLoadAnnotationAxioms() {
            return inner.isLoadAnnotationAxioms();
        }

        @Override
        public LoaderConfiguration setLoadAnnotationAxioms(boolean b) {
            return copy(inner.setLoadAnnotationAxioms(b));
        }

        @Override
        public boolean isReportStackTrace() {
            return inner.isReportStackTrace();
        }

        @Override
        public boolean isStrict() {
            return inner.isStrict();
        }

        @Override
        public LoaderConfiguration setStrict(boolean strict) {
            return copy(inner.setStrict(strict));
        }

        @Override
        public boolean isTreatDublinCoreAsBuiltIn() {
            return inner.isTreatDublinCoreAsBuiltIn();
        }

        @Override
        public LoaderConfiguration setTreatDublinCoreAsBuiltIn(boolean value) {
            return copy(inner.setTreatDublinCoreAsBuiltIn(value));
        }

        @Override
        public String getBannedParsers() {
            return inner.getBannedParsers();
        }

        @Override
        public LoaderConfiguration setBannedParsers(String ban) {
            return copy(inner.setBannedParsers(ban));
        }

        @Override
        public String getEntityExpansionLimit() {
            return inner.getEntityExpansionLimit();
        }

        @Override
        public LoaderConfiguration setEntityExpansionLimit(String limit) {
            return copy(inner.setEntityExpansionLimit(limit));
        }

        @Override
        public LoaderConfiguration setReportStackTraces(boolean b) {
            return copy(inner.setReportStackTraces(b));
        }

    }

    public enum DefaultScheme implements Scheme {
        HTTP,
        HTTPS,
        FTP,
        FILE,;

        @Override
        public String key() {
            return name().toLowerCase();
        }

        @Override
        public boolean same(IRI iri) {
            return Objects.equals(key(), iri.getScheme());
        }

        public static Stream<DefaultScheme> all() {
            return Stream.of(values());
        }
    }

    public interface Scheme extends Serializable {
        String key();

        boolean same(IRI iri);
    }
}

/* This file is part of the OWL API.
 * The contents of this file are subject to the LGPL License, Version 3.0.
 * Copyright 2014, The University of Manchester
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 * Alternatively, the contents of this file may be used under the terms of the Apache License, Version 2.0 in which case, the provisions of the Apache License Version 2.0 are applicable instead of those above.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License. */
package uk.ac.manchester.cs.owl.owlapi;

import java.util.Collection;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.util.CollectionFactory;

/**
 * @author Matthew Horridge, The University Of Manchester, Bio-Health
 *         Informatics Group
 * @since 2.0.0
 */
public class OWLDataPropertyAssertionAxiomImpl
        extends
        OWLIndividualRelationshipAxiomImpl<OWLDataPropertyExpression, OWLLiteral>
        implements OWLDataPropertyAssertionAxiom {

    private static final long serialVersionUID = 40000L;

    /**
     * @param subject
     *        subject
     * @param property
     *        property
     * @param value
     *        value
     * @param annotations
     *        annotations
     */
    public OWLDataPropertyAssertionAxiomImpl(@Nonnull OWLIndividual subject,
            @Nonnull OWLDataPropertyExpression property,
            @Nonnull OWLLiteral value,
            @Nonnull Collection<? extends OWLAnnotation> annotations) {
        super(subject, property, value, annotations);
    }

    @Nonnull
    @Override
    public OWLSubClassOfAxiom asOWLSubClassOfAxiom() {
        return new OWLSubClassOfAxiomImpl(new OWLObjectOneOfImpl(
                CollectionFactory.createSet(getSubject())),
                new OWLDataHasValueImpl(getProperty(), getObject()),
                NO_ANNOTATIONS);
    }

    @Override
    public OWLDataPropertyAssertionAxiom getAxiomWithoutAnnotations() {
        if (!isAnnotated()) {
            return this;
        }
        return new OWLDataPropertyAssertionAxiomImpl(getSubject(),
                getProperty(), getObject(), NO_ANNOTATIONS);
    }

    @Override
    public OWLDataPropertyAssertionAxiom getAnnotatedAxiom(
            Collection<OWLAnnotation> annotations) {
        return new OWLDataPropertyAssertionAxiomImpl(getSubject(),
                getProperty(), getObject(), mergeAnnos(annotations));
    }

    @Override
    public OWLDataPropertyAssertionAxiom getAnnotatedAxiom(
            Stream<OWLAnnotation> annotations) {
        return new OWLDataPropertyAssertionAxiomImpl(getSubject(),
                getProperty(), getObject(), mergeAnnos(annotations));
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj)
                && obj instanceof OWLDataPropertyAssertionAxiom;
    }

    @Override
    public AxiomType<?> getAxiomType() {
        return AxiomType.DATA_PROPERTY_ASSERTION;
    }
}

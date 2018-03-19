/*
 * This file is part of the ONT API.
 * The contents of this file are subject to the LGPL License, Version 3.0.
 * Copyright (c) 2018, Avicomp Services, AO
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 * Alternatively, the contents of this file may be used under the terms of the Apache License, Version 2.0 in which case, the provisions of the Apache License Version 2.0 are applicable instead of those above.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package ru.avicomp.owlapi.objects.swrl;

import org.semanticweb.owlapi.model.SWRLArgument;
import org.semanticweb.owlapi.model.SWRLBinaryAtom;
import org.semanticweb.owlapi.model.SWRLPredicate;

import java.util.stream.Stream;

import static org.semanticweb.owlapi.util.OWLAPIPreconditions.checkNotNull;

/**
 * @param <A> atom type
 * @param <B> atom type
 * @author Matthew Horridge, The University Of Manchester, Bio-Health Informatics Group
 * @since 2.0.0
 */
public abstract class SWRLBinaryAtomImpl<A extends SWRLArgument, B extends SWRLArgument> extends
    SWRLAtomImpl implements
    SWRLBinaryAtom<A, B> {

    private final A arg0;
    private final B arg1;

    protected SWRLBinaryAtomImpl(SWRLPredicate predicate, A arg0, B arg1) {
        super(predicate);
        this.arg0 = checkNotNull(arg0, "arg0 cannot be null");
        this.arg1 = checkNotNull(arg1, "arg1 cannot be null");
    }

    @Override
    public Stream<SWRLArgument> allArguments() {
        return Stream.of(arg0, arg1);
    }

    @Override
    public A getFirstArgument() {
        return arg0;
    }

    @Override
    public B getSecondArgument() {
        return arg1;
    }
}

/*
 * Copyright (c) 2023. Andrea Giulianelli
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.webbasedwodt.ontology;

import io.github.webbasedwodt.model.ontology.DTOntology;
import io.github.webbasedwodt.model.ontology.Individual;
import io.github.webbasedwodt.model.ontology.Node;
import io.github.webbasedwodt.model.ontology.Property;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.Optional;

/**
 * Ontology for the Intersection Digital Twin.
 */
public final class IntersectionOntology implements DTOntology {
    private static final Map<String, Pair<String, String>> RELATIONSHIP_MAP = Map.of(
            "contains-traffic-light", Pair.of(
                    "https://smartcityontology.com/ontology#containsTrafficLight",
                    "https://smartcityontology.com/ontology#TrafficLight"
            )
    );

    @Override
    public String getDigitalTwinType() {
        return "https://smartcityontology.com/ontology#Intersection";
    }

    @Override
    public Optional<Property> obtainProperty(final String rawProperty) {
        if (RELATIONSHIP_MAP.containsKey(rawProperty)) {
            return Optional.of(new Property(RELATIONSHIP_MAP.get(rawProperty).getLeft()));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> obtainPropertyValueType(final String rawProperty) {
        if (RELATIONSHIP_MAP.containsKey(rawProperty)) {
            return Optional.of(RELATIONSHIP_MAP.get(rawProperty).getRight());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public <T> Optional<Pair<Property, Node>> convertPropertyValue(final String rawProperty, final T propertyValue) {
        return Optional.empty();
    }

    @Override
    public Optional<Pair<Property, Individual>> convertRelationship(
            final String rawRelationship,
            final String targetUri
    ) {
        if (RELATIONSHIP_MAP.containsKey(rawRelationship)) {
            return Optional.of(
                    Pair.of(new Property(RELATIONSHIP_MAP.get(rawRelationship).getLeft()), new Individual(targetUri))
            );
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> obtainActionType(final String rawAction) {
        return Optional.empty();
    }
}

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

package io.github.webbasedwodt;

import io.github.webbasedwodt.ontology.IntersectionOntology;
import io.github.webbasedwodt.physicaladapter.IntersectionPhysicalAdapter;
import io.github.webbasedwodt.shadowing.MirrorShadowingFunction;
import io.github.webbasedwodt.adapter.WoDTDigitalAdapter;
import io.github.webbasedwodt.adapter.WoDTDigitalAdapterConfiguration;
import it.wldt.core.engine.DigitalTwin;
import it.wldt.core.engine.DigitalTwinEngine;
import it.wldt.exception.EventBusException;
import it.wldt.exception.ModelException;
import it.wldt.exception.WldtConfigurationException;
import it.wldt.exception.WldtDigitalTwinStateException;
import it.wldt.exception.WldtEngineException;
import it.wldt.exception.WldtRuntimeException;
import it.wldt.exception.WldtWorkerException;

import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Java template project.
 */
public final class Launcher {
    private static final String TRAFFIC_LIGHT_A_URI_VARIABLE = "TRAFFIC_LIGHT_A_URI";
    private static final String TRAFFIC_LIGHT_B_URI_VARIABLE = "TRAFFIC_LIGHT_B_URI";
    private static final String EXPOSED_PORT_VARIABLE = "EXPOSED_PORT";
    private static final String PLATFORM_URL_VARIABLE = "PLATFORM_URL";

    static {
        // Checks on existence of environmental variables
        Objects.requireNonNull(System.getenv(TRAFFIC_LIGHT_A_URI_VARIABLE), "Please provide the traffic light A uri");
        Objects.requireNonNull(System.getenv(TRAFFIC_LIGHT_B_URI_VARIABLE), "Please provide the traffic light B uri");
        Objects.requireNonNull(System.getenv(EXPOSED_PORT_VARIABLE), "Please provide the exposed port");
        Objects.requireNonNull(System.getenv(PLATFORM_URL_VARIABLE), "Please provide the platform url");
    }

    private Launcher() { }

    /**
     * Main function.
     * @param args
     */
    public static void main(final String[] args) {
        try {
            final int portNumber = Integer.parseInt(System.getenv(EXPOSED_PORT_VARIABLE));
            final String intersectionDTId = "intersection-dt";
            final DigitalTwin intersectionDT = new DigitalTwin(intersectionDTId, new MirrorShadowingFunction());
            intersectionDT.addPhysicalAdapter(new IntersectionPhysicalAdapter(
                    System.getenv(TRAFFIC_LIGHT_A_URI_VARIABLE),
                    System.getenv(TRAFFIC_LIGHT_B_URI_VARIABLE)
            ));
            intersectionDT.addDigitalAdapter(new WoDTDigitalAdapter(
                    "wodt-dt-adapter",
                    new WoDTDigitalAdapterConfiguration(
                            "http://localhost:" + portNumber + "/",
                            new IntersectionOntology(),
                            portNumber,
                            "intersectionPA",
                            Set.of(System.getenv(PLATFORM_URL_VARIABLE)))
            ));

            final DigitalTwinEngine digitalTwinEngine = new DigitalTwinEngine();
            digitalTwinEngine.addDigitalTwin(intersectionDT);
            digitalTwinEngine.startDigitalTwin(intersectionDTId);
        } catch (ModelException
                 | WldtDigitalTwinStateException
                 | WldtWorkerException
                 | WldtRuntimeException
                 | EventBusException
                 | WldtConfigurationException
                 | WldtEngineException e) {
            Logger.getLogger(Launcher.class.getName()).info(e.getMessage());
        }
    }
}

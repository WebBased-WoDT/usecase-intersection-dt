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

package io.github.webbasedwodt.physicaladapter;

import it.wldt.adapter.physical.PhysicalAdapter;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.PhysicalAssetRelationship;
import it.wldt.adapter.physical.event.PhysicalAssetActionWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceCreatedWldtEvent;
import it.wldt.exception.EventBusException;
import it.wldt.exception.PhysicalAdapterException;

import java.util.logging.Logger;

/**
 * The Physical Adapter of the Intersection Digital Twin.
 */
public final class IntersectionPhysicalAdapter extends PhysicalAdapter {
    private static final int STARTUP_TIME = 2000;
    private static final String CONTAINS_TRAFFIC_LIGHT_RELATIONSHIP_KEY = "contains-traffic-light";
    private final PhysicalAssetRelationship<String> containsTrafficLightRelationship =
            new PhysicalAssetRelationship<>(CONTAINS_TRAFFIC_LIGHT_RELATIONSHIP_KEY);
    private final String trafficLightAUri;
    private final String trafficLightBUri;

    /**
     * Default constructor.
     * @param trafficLightAUri the uri of the traffic light A
     * @param trafficLightBUri the uri of the traffic light B
     */
    public IntersectionPhysicalAdapter(final String trafficLightAUri, final String trafficLightBUri) {
        super("intersection-physical-adapter");
        this.trafficLightAUri = trafficLightAUri;
        this.trafficLightBUri = trafficLightBUri;
    }

    @Override
    public void onIncomingPhysicalAction(final PhysicalAssetActionWldtEvent<?> physicalAssetActionWldtEvent) { }

    @Override
    public void onAdapterStart() {
        final PhysicalAssetDescription pad = new PhysicalAssetDescription();
        pad.getRelationships().add(containsTrafficLightRelationship);

        try {
            this.notifyPhysicalAdapterBound(pad);
            Thread.sleep(STARTUP_TIME);
            publishPhysicalAssetRelationshipCreatedWldtEvent(
                    new PhysicalAssetRelationshipInstanceCreatedWldtEvent<>(
                            containsTrafficLightRelationship.createRelationshipInstance(trafficLightAUri)
                    )
            );
            publishPhysicalAssetRelationshipCreatedWldtEvent(
                    new PhysicalAssetRelationshipInstanceCreatedWldtEvent<>(
                            containsTrafficLightRelationship.createRelationshipInstance(trafficLightBUri)
                    )
            );
        } catch (PhysicalAdapterException | EventBusException | InterruptedException e) {
            Logger.getLogger(IntersectionPhysicalAdapter.class.getName()).info(e.getMessage());
        }
    }

    @Override
    public void onAdapterStop() {

    }
}

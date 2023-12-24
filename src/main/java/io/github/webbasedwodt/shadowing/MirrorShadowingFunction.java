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

package io.github.webbasedwodt.shadowing;

import it.wldt.adapter.digital.event.DigitalActionWldtEvent;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.PhysicalAssetRelationshipInstance;
import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceCreatedWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceDeletedWldtEvent;
import it.wldt.core.model.ShadowingModelFunction;
import it.wldt.core.state.DigitalTwinStateAction;
import it.wldt.core.state.DigitalTwinStateProperty;
import it.wldt.core.state.DigitalTwinStateRelationship;
import it.wldt.core.state.DigitalTwinStateRelationshipInstance;
import it.wldt.exception.EventBusException;
import it.wldt.exception.ModelException;
import it.wldt.exception.WldtDigitalTwinStateActionConflictException;
import it.wldt.exception.WldtDigitalTwinStateActionException;
import it.wldt.exception.WldtDigitalTwinStateException;
import it.wldt.exception.WldtDigitalTwinStatePropertyBadRequestException;
import it.wldt.exception.WldtDigitalTwinStatePropertyConflictException;
import it.wldt.exception.WldtDigitalTwinStatePropertyException;
import it.wldt.exception.WldtDigitalTwinStatePropertyNotFoundException;
import it.wldt.exception.WldtDigitalTwinStateRelationshipException;

import java.util.Map;
import java.util.logging.Logger;

/**
 * Mirror shadowing function.
 */
public final class MirrorShadowingFunction extends ShadowingModelFunction {

    /**
     * Default constructor.
     */
    public MirrorShadowingFunction() {
        super("mirror-shadowing-function");
    }

    @Override
    protected void onCreate() {

    }

    @Override
    protected void onStart() {

    }

    @Override
    protected void onStop() {

    }

    @Override
    protected void onDigitalTwinBound(final Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap) {
        try {
            //Iterate over all the received PAD from connected Physical Adapters
            adaptersPhysicalAssetDescriptionMap.values().forEach(pad -> {
                pad.getProperties().forEach(property -> {
                    try {
                        this.digitalTwinState.createProperty(new DigitalTwinStateProperty<>(
                                property.getKey(),
                                property.getInitialValue())
                        );
                        this.observePhysicalAssetProperty(property);
                    } catch (EventBusException
                             | ModelException
                             | WldtDigitalTwinStateException
                             | WldtDigitalTwinStatePropertyException
                             | WldtDigitalTwinStatePropertyConflictException
                             | WldtDigitalTwinStatePropertyBadRequestException e) {
                        Logger.getLogger(MirrorShadowingFunction.class.getName()).info(e.getMessage());
                    }
                });

                //Iterate over available declared Physical Actions for the target Physical Adapter's PAD
                pad.getActions().forEach(action -> {
                    try {
                        this.digitalTwinState.enableAction(
                                new DigitalTwinStateAction(
                                        action.getKey(),
                                        action.getType(),
                                        action.getContentType()
                                )
                        );
                    } catch (WldtDigitalTwinStateActionException
                             | WldtDigitalTwinStateException
                             | WldtDigitalTwinStateActionConflictException e) {
                        Logger.getLogger(MirrorShadowingFunction.class.getName()).info(e.getMessage());
                    }
                });

                pad.getRelationships().forEach(relationship -> {
                    try {
                        if (relationship != null) {
                            this.digitalTwinState.createRelationship(
                                    new DigitalTwinStateRelationship<>(
                                            relationship.getName(),
                                            relationship.getName()
                                    )
                            );
                            observePhysicalAssetRelationship(relationship);
                        }
                    } catch (WldtDigitalTwinStateRelationshipException
                             | EventBusException
                             | ModelException e) {
                        Logger.getLogger(MirrorShadowingFunction.class.getName()).info(e.getMessage());
                    }
                });

            });
            observeDigitalActionEvents();
            notifyShadowingSync();
        } catch (EventBusException e) {
            Logger.getLogger(MirrorShadowingFunction.class.getName()).info(e.getMessage());
        }
    }

    @Override
    protected void onDigitalTwinUnBound(final Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap,
                                        final String errorMessage) {

    }

    @Override
    protected void onPhysicalAdapterBidingUpdate(final String adapterId,
                                                 final PhysicalAssetDescription adapterPhysicalAssetDescription) {

    }

    @Override
    protected void onPhysicalAssetPropertyVariation(final PhysicalAssetPropertyWldtEvent<?> physicalAssetPropertyWldtEvent) {
        try {
            this.digitalTwinState.updateProperty(new DigitalTwinStateProperty<>(
                    physicalAssetPropertyWldtEvent.getPhysicalPropertyId(),
                    physicalAssetPropertyWldtEvent.getBody()));
        } catch (WldtDigitalTwinStatePropertyException
                 | WldtDigitalTwinStatePropertyBadRequestException
                 | WldtDigitalTwinStateException
                 | WldtDigitalTwinStatePropertyNotFoundException e) {
            Logger.getLogger(MirrorShadowingFunction.class.getName()).info(e.getMessage());
        }
    }

    @Override
    protected void onPhysicalAssetEventNotification(final PhysicalAssetEventWldtEvent<?> physicalAssetEventWldtEvent) {

    }

    @Override
    protected void onPhysicalAssetRelationshipEstablished(
            final PhysicalAssetRelationshipInstanceCreatedWldtEvent<?> physicalAssetRelationshipInstanceCreatedWldtEvent
    ) {
        try {
            if (physicalAssetRelationshipInstanceCreatedWldtEvent != null
                    && physicalAssetRelationshipInstanceCreatedWldtEvent.getBody() != null) {
                final PhysicalAssetRelationshipInstance<?> paRelInstance =
                        physicalAssetRelationshipInstanceCreatedWldtEvent.getBody();

                if (paRelInstance.getTargetId() instanceof String) {
                    final String relName = paRelInstance.getRelationship().getName();
                    final String relKey = paRelInstance.getKey();
                    final String relTargetId = (String) paRelInstance.getTargetId();

                    final DigitalTwinStateRelationshipInstance<String> instance =
                            new DigitalTwinStateRelationshipInstance<>(relName, relTargetId, relKey);

                    this.digitalTwinState.addRelationshipInstance(relName, instance);
                }
            }
        } catch (WldtDigitalTwinStateRelationshipException e) {
            Logger.getLogger(MirrorShadowingFunction.class.getName()).info(e.getMessage());
        }
    }

    @Override
    protected void onPhysicalAssetRelationshipDeleted(
            final PhysicalAssetRelationshipInstanceDeletedWldtEvent<?> physicalAssetRelationshipWldtEvent) {
        try {
            if (physicalAssetRelationshipWldtEvent != null && physicalAssetRelationshipWldtEvent.getBody() != null) {
                final PhysicalAssetRelationshipInstance<?> paRelInstance = physicalAssetRelationshipWldtEvent.getBody();
                if (paRelInstance.getTargetId() instanceof String) {
                    final String relName = paRelInstance.getRelationship().getName();
                    final String relKey = paRelInstance.getKey();

                    this.digitalTwinState.deleteRelationshipInstance(relName, relKey);
                }
            }
        } catch (WldtDigitalTwinStateRelationshipException e) {
            Logger.getLogger(MirrorShadowingFunction.class.getName()).info(e.getMessage());
        }
    }

    @Override
    protected void onDigitalActionEvent(final DigitalActionWldtEvent<?> digitalActionWldtEvent) {
        try {
            this.publishPhysicalAssetActionWldtEvent(digitalActionWldtEvent.getActionKey(), digitalActionWldtEvent.getBody());
        } catch (EventBusException e) {
            Logger.getLogger(MirrorShadowingFunction.class.getName()).info(e.getMessage());
        }
    }
}

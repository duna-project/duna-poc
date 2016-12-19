/*
 * Copyright (c) 2016 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.inject;

import com.google.inject.spi.ProvisionListener;

/**
 * Created by carlos on 19/12/16.
 */
public class ProxyProvisionListener implements ProvisionListener {

    @Override
    public <T> void onProvision(ProvisionInvocation<T> provision) {
        // provision.
    }
}

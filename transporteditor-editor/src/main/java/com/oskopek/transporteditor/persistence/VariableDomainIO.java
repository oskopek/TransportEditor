/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.persistence;

import com.oskopek.transporteditor.planning.domain.VariableDomain;

public class VariableDomainIO implements DataReader<VariableDomain>, DataWriter<VariableDomain> {

    private final VariableDomain domain;

    public VariableDomainIO(VariableDomain domain) {
        this.domain = domain;
    }

    @Override
    public String serialize(VariableDomain object) throws IllegalArgumentException {
        return null;
    }

    @Override
    public VariableDomain parse(String contents) throws IllegalArgumentException {
        return null;
    }
}

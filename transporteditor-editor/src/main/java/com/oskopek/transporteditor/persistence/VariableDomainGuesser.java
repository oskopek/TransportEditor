/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.persistence;

import com.oskopek.transporteditor.planning.domain.VariableDomain;

public class VariableDomainGuesser implements DataReader<VariableDomain>, DataWriter<VariableDomain> {

    @Override
    public VariableDomain parse(String contents) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public String serialize(VariableDomain object) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}

/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.persistence;

import com.thoughtworks.xstream.XStream;

public abstract class XStreamGenericIO<T> implements DataReader<T>, DataWriter<T> {

    private final XStream xStream;

    public XStreamGenericIO() {
        xStream = new XStream();
        xStream.setMode(XStream.ID_REFERENCES);
    }

    @Override
    public String serialize(T object) throws IllegalArgumentException {
        return xStream.toXML(object);
    }

    @Override
    public T parse(String contents) throws IllegalArgumentException {
        return (T) xStream.fromXML(contents);
    }

}

package com.oskopek.transporteditor.persistence;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;

public abstract class XStreamGenericIO<T> implements DataIO<T> {

    private final XStream xStream;

    public XStreamGenericIO() {
        xStream = new XStream();
        xStream.registerConverter(new JavaBeanConverter(xStream.getMapper()), -10);
        xStream.setMode(XStream.ID_REFERENCES);
    }

    @Override
    public <T1 extends T> String serialize(T1 object) throws IllegalArgumentException {
        return xStream.toXML(object);
    }

    @Override
    public T parse(String contents) throws IllegalArgumentException {
        try {
            return (T) xStream.fromXML(contents);
        } catch (XStreamException e) {
            throw new IllegalArgumentException(
                    "Could not parse XML. " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }

}

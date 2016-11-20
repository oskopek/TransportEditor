package com.oskopek.transporteditor.persistence;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;

public abstract class XStreamGenericIO<T> implements DataReader<T>, DataWriter<T> {

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
        return (T) xStream.fromXML(contents);
    }

}

package com.oskopek.transporteditor.persistence;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.converters.basic.BooleanConverter;
import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;

public abstract class XStreamGenericIO<Persistable_> implements DataIO<Persistable_> {

    private final XStream xStream;

    public XStreamGenericIO() {
        xStream = new XStream();
        xStream.registerConverter(new JavaBeanConverter(xStream.getMapper()), XStream.PRIORITY_VERY_LOW);
        xStream.registerConverter(new BooleanConverter() {
            @Override
            public boolean canConvert(Class type) {
                return Boolean.class.equals(type);
            }

            @Override
            public Object fromString(String str) {
                if (str == null || "null".equals(str)) {
                    return null;
                } else {
                    return Boolean.parseBoolean(str);
                }
            }
        }, XStream.PRIORITY_VERY_HIGH);
        xStream.setMode(XStream.ID_REFERENCES);
    }

    @Override
    public <T extends Persistable_> String serialize(T object) throws IllegalArgumentException {
        return xStream.toXML(object);
    }

    @Override
    public Persistable_ parse(String contents, Class<? extends Persistable_> clazz) throws IllegalArgumentException {
        try {
            return clazz.cast(xStream.fromXML(contents));
        } catch (ClassCastException | XStreamException e) {
            throw new IllegalArgumentException(
                    "Could not parse XML. " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }

}

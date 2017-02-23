package com.oskopek.transporteditor.model.problem;

/**
 * A placeholder action object implementation, used for methods where extra arguments are needed syntactically
 * but not semantically.
 */
public class PlaceholderActionObject implements ActionObject {

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not supported on placeholder!");
    }

    @Override
    public ActionObject updateName(String newName) {
        throw new UnsupportedOperationException("Not supported on placeholder!");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof PlaceholderActionObject;
    }

    @Override
    public int hashCode() {
        return 31;
    }
}

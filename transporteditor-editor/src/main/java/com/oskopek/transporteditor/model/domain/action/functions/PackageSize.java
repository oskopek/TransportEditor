/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.domain.action.functions;

import com.oskopek.transporteditor.model.domain.action.ActionCost;
import com.oskopek.transporteditor.model.problem.ActionObject;
import com.oskopek.transporteditor.model.problem.Package;

public class PackageSize extends DefaultFunction {

    @Override
    public ActionCost apply(ActionObject... actionObjects) {
        if (actionObjects == null || actionObjects.length != 1 || !Package.class.isInstance(actionObjects[0])) {
            throw new IllegalArgumentException("PackageSize can only be applied to one argument of type Package");
        }
        return apply((Package) actionObjects[0]);
    }

    public ActionCost apply(Package aPackage) {
        if (aPackage == null) {
            throw new IllegalArgumentException("Package cannot be null.");
        }
        return aPackage.getSize();
    }

}

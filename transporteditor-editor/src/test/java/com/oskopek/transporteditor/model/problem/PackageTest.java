package com.oskopek.transporteditor.model.problem;

import com.oskopek.transporteditor.model.domain.action.ActionCost;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class PackageTest {

    @Test
    public void equalsTest() throws Exception {
        Location src = null;
        Location dst = new Location("locdest");
        assertThat(new Package("p1", src, dst, ActionCost.valueOf(1)))
                .isEqualTo(new Package("p1", src, dst, ActionCost.valueOf(1)))
                .isNotEqualTo(new Package("p1", dst, dst, ActionCost.valueOf(1)))
                .isNotEqualTo(new Package("p1", src, dst, ActionCost.valueOf(0)))
                .isNotEqualTo(new Package("p1", src, null, ActionCost.valueOf(1)))
                .isNotEqualTo(new Package("p2", src, dst, ActionCost.valueOf(1)));
    }
}

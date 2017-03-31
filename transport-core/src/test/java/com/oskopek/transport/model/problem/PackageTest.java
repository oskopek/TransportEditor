package com.oskopek.transport.model.problem;

import com.oskopek.transport.model.domain.action.ActionCost;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class PackageTest {

    @Test
    public void equalsTest() throws Exception {
        Location src = null;
        Location dst = new Location("locdest");
        assertThat(new Package("p1", src, dst, ActionCost.ONE))
                .isEqualTo(new Package("p1", src, dst, ActionCost.ONE))
                .isNotEqualTo(new Package("p1", dst, dst, ActionCost.ONE))
                .isNotEqualTo(new Package("p1", src, dst, ActionCost.ZERO))
                .isNotEqualTo(new Package("p1", src, null, ActionCost.ONE))
                .isNotEqualTo(new Package("p2", src, dst, ActionCost.ONE));
    }
}

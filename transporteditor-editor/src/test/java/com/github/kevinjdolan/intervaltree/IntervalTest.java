package com.github.kevinjdolan.intervaltree;

import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

public class IntervalTest {
    @Test
    public void contains() throws Exception {
        assertThat(new Interval<Void>(0, 2).contains(1)).isTrue();
        assertThat(new Interval<Void>(0, 2).contains(0)).isTrue();
        assertThat(new Interval<Void>(0, 2).contains(2)).isFalse();
        assertThat(new Interval<Void>(0, 2).contains(3)).isFalse();
        assertThat(new Interval<Void>(0, 2).contains(-1)).isFalse();
    }

    @Test
    public void intersects() throws Exception {
        assertThat(new Interval<Void>(0, 2).intersects(new Interval<>(2, 3))).isFalse();
        assertThat(new Interval<Void>(0, 2).intersects(new Interval<>(1, 3))).isTrue();
        assertThat(new Interval<Void>(0, 2).intersects(new Interval<>(-1, 0))).isFalse();
        assertThat(new Interval<Void>(0, 2).intersects(new Interval<>(-1, 1))).isTrue();
    }

    @Test
    public void compareTo() throws Exception {
        assertThat(new Interval<Void>(0, 2).compareTo(new Interval<>(2, 3))).isLessThan(0);
        assertThat(new Interval<Void>(0, 2).compareTo(new Interval<>(1, 3))).isLessThan(0);
        assertThat(new Interval<Void>(0, 2).compareTo(new Interval<>(0, 3))).isLessThan(0);
        assertThat(new Interval<Void>(0, 2).compareTo(new Interval<>(0, 2))).isEqualTo(0);
        assertThat(new Interval<Void>(0, 2).compareTo(new Interval<>(0, 1))).isGreaterThan(0);
        assertThat(new Interval<Void>(0, 2).compareTo(new Interval<>(-1, 0))).isGreaterThan(0);
        assertThat(new Interval<Void>(0, 2).compareTo(new Interval<>(-1, 1))).isGreaterThan(0);
    }

}

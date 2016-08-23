package com.mapr.stats;

import org.junit.Test;

import static org.junit.Assert.*;

public class TagMapTest {
    @Test
    public void testEmpty() {
        TagMap t = TagMap.create();
        assertEquals("TagMap{h=0000000000000000}", t.toString());
    }

    @Test
    public void testOrdering() {
        TagMap t = TagMap.create().add("a", "1").add("b", "2");
        TagMap u = TagMap.create().add("b", "2").add("a", "1");

        assertEquals(t.getHashValue(), u.getHashValue());
        assertNotEquals(t.toString(), u.toString());
    }

    @Test
    public void testSharing() throws Exception {
        TagMap t = TagMap.create().add("a", "1").add("b", "2");
        String s0 = t.toString();
        long h0 = t.getHashValue();

        TagMap u = t.add("c", "3");
        assertNotEquals(u.getHashValue(), t.getHashValue());
        assertEquals(s0, t.toString());
        assertEquals(h0, t.getHashValue());
    }
}
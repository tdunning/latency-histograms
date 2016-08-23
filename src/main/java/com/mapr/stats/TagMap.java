package com.mapr.stats;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

import java.util.Formatter;

/**
 * A tag map is a key value store where the most important thing is not to search for
 * values for specific keys, but rather to generate unique hashes for the entire set
 * of keys and values in the TagMap. In addition, it seems like a good idea to have
 * TagMaps be immutable. This allows an idiom whereby you can pass a TagMap without
 * worrying about how it might be used.
 */
public class TagMap {
    private final TagMap parent;
    private final String key;
    private final String value;
    private final long hashValue;

    private TagMap(TagMap parent, String key, String value) {
        assert parent != null;
        this.parent = parent;

        this.key = key;
        this.value = value;

        Hasher hash = Hashing.murmur3_128().newHasher();
        hash.putUnencodedChars(key);
        hash.putUnencodedChars(value);
        this.hashValue = hash.hash().asLong() ^ parent.hashValue;
    }

    private TagMap() {
        this.parent = null;
        this.key = null;
        this.value = null;
        this.hashValue = 0;
    }

    public static TagMap create() {
        return new TagMap();
    }

    public TagMap add(String key, String value) {
        return new TagMap(this, key, value);
    }

    public long getHashValue() {
        return hashValue;
    }

    @Override
    public String toString() {
        Formatter out = new Formatter();
        out.format("TagMap{h=%016x", hashValue);
        TagMap t = this;
        while (t.key != null) {
            out.format(", %s=%s", t.key, t.value);
            t = t.parent;
        }
        out.format("}");
        return out.toString();
    }
}

package com.github.xiaolyuh.redis.command;

import io.lettuce.core.ScanCursor;

import java.util.ArrayList;
import java.util.List;

public class KeyScanCursor<K> extends ScanCursor {

    private final List<K> keys = new ArrayList<>();

    public List<K> getKeys() {
        return keys;
    }
}
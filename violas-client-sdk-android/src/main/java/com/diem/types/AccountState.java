package com.diem.types;

import com.novi.serde.Bytes;
import com.novi.serde.Tuple2;

import java.util.Map;
import java.util.TreeMap;

public class AccountState {
    private Map<byte[], byte[]> mTree;

    AccountState(Map<byte[], byte[]> tree) {
        mTree = tree;
    }

    public static AccountState deserialize(com.novi.serde.Deserializer deserializer) throws com.novi.serde.DeserializationError {
        deserializer.increase_container_depth();
        AccountState.Builder builder = new AccountState.Builder();

        TreeMap<Bytes, Bytes> treeMap = new TreeMap<>();

        long len = deserializer.deserialize_len();
        for (int i = 0; i < len; i++) {
            Tuple2<Bytes, Bytes> pair;

            treeMap.put(deserializer.deserialize_bytes(), deserializer.deserialize_bytes());
        }
        //builder.value

        deserializer.decrease_container_depth();
        return builder.build();
    }

    public static AccountState bcsDeserialize(byte[] input) throws com.novi.serde.DeserializationError {
        if (input == null) {
            throw new com.novi.serde.DeserializationError("Cannot deserialize null array");
        }
        com.novi.serde.Deserializer deserializer = new com.novi.bcs.BcsDeserializer(input);
        AccountState value = deserialize(deserializer);
        if (deserializer.get_buffer_offset() < input.length) {
            throw new com.novi.serde.DeserializationError("Some input bytes were not read");
        }
        return value;
    }

    public static final class Builder {
        public Map<Bytes, Byte> value;

        public AccountState build() {
            return new AccountState(
                    value
            );
        }
    }

//    public <T extends Object> T getResource(byte[] key) {
//        T t = new T;
//
//        return t;
//    }
}

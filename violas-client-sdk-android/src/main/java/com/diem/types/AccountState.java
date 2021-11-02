package com.diem.types;

import com.google.common.reflect.TypeToken;
import com.novi.bcs.BcsDeserializer;
import com.novi.serde.Bytes;
import com.novi.serde.Deserializer;
import com.novi.serde.Tuple2;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
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

        TreeMap<byte[], byte[]> treeMap = new TreeMap<>((bytes1, bytes2) ->  {
            if(bytes1 == bytes2)
                return 0;

            int length = bytes1.length;
            int cmp;
            if ((cmp = Integer.compare(length, bytes2.length)) != 0) {
                return cmp;
            }

            int len = Math.min(bytes1.length, bytes2.length);

            for (int i = 0; i < len; i++) {
                if(bytes1[i] != bytes2[i])
                    return bytes1[i] - bytes2[i];
            }

            return  0;
        });

        Bytes bytes = deserializer.deserialize_bytes();
        Deserializer des = new BcsDeserializer(bytes.content());

        long len = des.deserialize_len();

        for (int i = 0; i < len; i++) {
            Tuple2<Bytes, Bytes> pair;

            treeMap.put(des.deserialize_bytes().content(), des.deserialize_bytes().content());
        }

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
        public Map<byte[], byte[]> value;

        public AccountState build() {
            return new AccountState(
                    value
            );
        }
    }

    public <T extends Object> T getResource(byte[] key, Class<T> object) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException v{

        return object.getDeclaredConstructor().newInstance();
    }
}

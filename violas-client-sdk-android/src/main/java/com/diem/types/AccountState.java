package com.diem.types;

import com.google.common.reflect.TypeToken;
import com.novi.bcs.BcsDeserializer;
import com.novi.serde.Bytes;
import com.novi.serde.DeserializationError;
import com.novi.serde.Deserializer;
import com.novi.serde.SerializationError;
import com.novi.serde.Tuple2;

import org.graalvm.compiler.nodes.memory.Access;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
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

        TreeMap<byte[], byte[]> treeMap = new TreeMap<>((bytes1, bytes2) -> {
            if (bytes1 == bytes2)
                return 0;

            int length = bytes1.length;
            int cmp;
            if ((cmp = Integer.compare(length, bytes2.length)) != 0) {
                return cmp;
            }

            int len = Math.min(bytes1.length, bytes2.length);

            for (int i = 0; i < len; i++) {
                if (bytes1[i] != bytes2[i])
                    return bytes1[i] - bytes2[i];
            }

            return 0;
        });

        Bytes bytes = deserializer.deserialize_bytes();
        Deserializer des = new BcsDeserializer(bytes.content());

        long len = des.deserialize_len();

        for (int i = 0; i < len; i++) {
            Tuple2<Bytes, Bytes> pair;

            treeMap.put(des.deserialize_bytes().content(), des.deserialize_bytes().content());
        }

        deserializer.decrease_container_depth();

        builder.value = treeMap;
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

    public interface BcsObject {
        public void bcsDeserialize(byte[] input) throws com.novi.serde.DeserializationError;
    }

    public <T extends BcsObject> T getResource(StructTag path, Class<T> object)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException,
            InstantiationException, DeserializationError, SerializationError {

        byte[] value = mTree.get(AccessPath.resourceAccessVec(path));
        if (value == null)
            throw new InvalidParameterException("The parameter path cannot be found.");

        //Method bcsDeserialize = object.getMethod("bcsDeserialize", new Class[]{byte[].class});
        //T t1 = (T) bcsDeserialize.invoke(null, value);

        T t = object.getDeclaredConstructor().newInstance();

        t.bcsDeserialize(value);

        return t;

    }

    public <T> T getResource2(StructTag path, Class<T> object)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException,
            InstantiationException, DeserializationError, SerializationError {

        byte[] value = mTree.get(AccessPath.resourceAccessVec(path));
        if (value == null)
            throw new InvalidParameterException("The parameter path cannot be found.");

        Method bcsDeserialize = object.getMethod("bcsDeserialize", byte[].class);
        T t = (T) bcsDeserialize.invoke(null, value);

        return t;

    }
}

package com.diem;

import static org.junit.Assert.assertEquals;

import com.diem.jsonrpc.DiemJsonRpcClient;
import com.diem.jsonrpc.JsonRpc;
import com.diem.types.AccountAddress;
import com.diem.types.AccountState;
import com.diem.types.ChainId;
import com.diem.types.Identifier;
import com.diem.types.StructTag;
import com.diem.types.TypeTag;
import com.diem.utils.Hex;
import com.novi.bcs.BcsDeserializer;
import com.novi.serde.Deserializer;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AccountStateTest {
    final String JSON_RPC_URL = "http://124.251.110.220:50001";
    final ChainId CHAIN_ID = new ChainId((byte) 4);

    public static class ExchangeRate implements AccountState.BcsObject {
        public long rate;
        public long timestamp;

        public ExchangeRate() {
            rate = 1;
            timestamp = 0;
        }

        public ExchangeRate(long rate, long timestamp) {
            this.rate = 1;
            this.timestamp = 0;
        }

        public static ExchangeRate deserialize(com.novi.serde.Deserializer deserializer) throws com.novi.serde.DeserializationError {
            deserializer.increase_container_depth();
            ExchangeRate exchangeRate = new ExchangeRate();

            //byte[] bytes = deserializer.deserialize_bytes().content();
            //exchangeRate.currency = new String(bytes, StandardCharsets.UTF_8);
            long rate = deserializer.deserialize_u64();
            long timestamp = deserializer.deserialize_u64();

            deserializer.decrease_container_depth();
            return new ExchangeRate(rate, timestamp);
        }

        public void bcsDeserialize(byte[] input) throws com.novi.serde.DeserializationError {
            if (input == null) {
                throw new com.novi.serde.DeserializationError("Cannot deserialize null array");
            }

            com.novi.serde.Deserializer deserializer = new com.novi.bcs.BcsDeserializer(input);

            rate = deserializer.deserialize_u64();
            timestamp = deserializer.deserialize_u64();

            if (deserializer.get_buffer_offset() < input.length) {
                throw new com.novi.serde.DeserializationError("Some input bytes were not read");
            }
        }
    }

    TypeTag make_currency_type_tag_struct(String currency) {
        final String rootAddress = "0000000000000000000000000A550C18";

        return new TypeTag.Struct(
                new StructTag(AccountAddress.valueOf(Hex.decode(rootAddress)),
                        new Identifier(currency),
                        new Identifier(currency),
                        new ArrayList<>())
        );
    }

    @Test
    public void testDeserialize() throws Exception {
        List<ExchangeRate> rates = new ArrayList<>();
        final String oracleAdminAccountAddress = "0000000000000000000000004f524143";
        DiemJsonRpcClient mClient = new DiemJsonRpcClient(JSON_RPC_URL, CHAIN_ID);

        JsonRpc.AccountStateWithProof stateWithProof = mClient.getAccountStateWithProof(oracleAdminAccountAddress, 0, 0);
        String blob = stateWithProof.getBlob();

        assertEquals(blob.isEmpty(), false);

        try {
            Deserializer des = new BcsDeserializer(Hex.decode(blob));
            AccountState accountState = AccountState.deserialize(des);

            //TypeTag.Struct
            StructTag tag = new StructTag(AccountAddress.valueOf(Hex.decode("00000000000000000000000000000001")),
                    new Identifier("Oracle"),
                    new Identifier("ExchangeRate"),
                    new ArrayList<TypeTag>() {
                        { add(make_currency_type_tag_struct("USD")); }
                    }
                    );

            ExchangeRate exchangeRate = accountState.getResource(tag.bcsSerialize(), ExchangeRate.class);

            System.console().printf("%d", exchangeRate.rate);

        } catch (Exception e) {
            System.console().printf("%s", e.getMessage());
        }


    }
}

package io.noone.androidcore.bnb.types

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import com.google.protobuf.ByteString
import io.noone.androidcore.bnb.Address
import io.noone.androidcore.bnb.protos.Binance
import java.util.stream.Collectors

@Suppress("unused", "SpellCheckingInspection")
class SignData(
    @SerializedName("chain_id")
    private val chainId: String,
    @SerializedName("account_number")
    private val accountNumber: String,
    private val sequence: String,
    private val memo: String,
    private val source: String,
    from: String,
    to: String,
    amount: Long,
    denom: String
) {
    private val data: ByteArray? = null
    val msgs: Array<SendOrder>

    init {
        val token = Token(amount, denom)
        val input = Destination(address = from, token = token)
        val output = Destination(address = to, token = token)
        msgs = arrayOf(SendOrder(input, output))
    }

    override fun toString(): String {
        val gson = GsonBuilder().serializeNulls().create()
        val str = gson.toJson(this)
        val jsonObjectSorted = sortAndGet(JsonParser.parseString(str).asJsonObject)
        return jsonObjectSorted.toString()
    }

    private fun sortAndGet(jsonObject: JsonObject): JsonObject {
        val keySet: List<String> =
            jsonObject.keySet().stream().sorted().collect(Collectors.toList())
        val temp = JsonObject()
        for (key in keySet) {
            var ele = jsonObject[key]
            when {
                ele.isJsonObject -> {
                    ele = sortAndGet(ele.asJsonObject)
                    temp.add(key, ele)
                }
                ele.isJsonArray -> {
                    val tempArray = JsonArray()
                    ele.asJsonArray.forEach {
                        tempArray.add(sortAndGet(it.asJsonObject))
                    }
                    temp.add(key, tempArray.asJsonArray)
                }
                ele.isJsonNull -> {
                    temp.add(key, ele.asJsonNull)
                }
                else -> temp.add(key, ele.asJsonPrimitive)
            }
        }
        return temp
    }

    fun toByteArray(): ByteArray = toString().toByteArray()

    inner class Token(
        @SerializedName("amount")
        private val amount: Long,
        @SerializedName("denom")
        private val denom: String
    ) {
        fun toProto(): Binance.SendOrder.Token {
            return Binance.SendOrder.Token.newBuilder()
                .setAmount(amount)
                .setDenom(denom)
                .build()
        }
    }

    inner class Destination(
        @SerializedName("address")
        private val address: String,
        token: Token
    ) {
        @SerializedName("coins")
        private val coins: Array<Token> = arrayOf(token)
        fun toInputProto(): Binance.SendOrder.Input {
            val builder: Binance.SendOrder.Input.Builder = Binance.SendOrder.Input.newBuilder()
                .setAddress(ByteString.copyFrom(Address(address).bytes))
            for (coin in coins) {
                builder.addCoins(coin.toProto())
            }
            return builder.build()
        }

        fun toOutputProto(): Binance.SendOrder.Output {
            val builder: Binance.SendOrder.Output.Builder = Binance.SendOrder.Output.newBuilder()
                .setAddress(ByteString.copyFrom(Address(address).bytes))
            for (coin in coins) {
                builder.addCoins(coin.toProto())
            }
            return builder.build()
        }

    }

    inner class SendOrder(input: Destination, output: Destination) {
        @SerializedName("inputs")
        private val inputs: Array<Destination> = arrayOf(input)

        @SerializedName("outputs")
        private val outputs: Array<Destination> = arrayOf(output)
        fun toProto(): Binance.SendOrder {
            val builder: Binance.SendOrder.Builder = Binance.SendOrder.newBuilder()
            for (input in inputs) {
                builder.addInputs(input.toInputProto())
            }
            for (output in outputs) {
                builder.addOutputs(output.toOutputProto())
            }
            return builder.build()
        }

    }
}

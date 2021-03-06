/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.operator.scalar;

import com.facebook.presto.spi.PrestoException;
import com.facebook.presto.spi.function.Description;
import com.facebook.presto.spi.function.LiteralParameters;
import com.facebook.presto.spi.function.ScalarFunction;
import com.facebook.presto.spi.function.SqlType;
import com.facebook.presto.spi.type.StandardTypes;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import io.airlift.slice.Slice;
import io.airlift.slice.Slices;
import io.airlift.slice.XxHash64;

import java.util.Base64;

import static com.facebook.presto.spi.StandardErrorCode.INVALID_FUNCTION_ARGUMENT;

public final class VarbinaryFunctions
{
    private VarbinaryFunctions() {}

    @Description("length of the given binary")
    @ScalarFunction
    @SqlType(StandardTypes.BIGINT)
    public static long length(@SqlType(StandardTypes.VARBINARY) Slice slice)
    {
        return slice.length();
    }

    @Description("encode binary data as base64")
    @ScalarFunction
    @SqlType(StandardTypes.VARCHAR)
    public static Slice toBase64(@SqlType(StandardTypes.VARBINARY) Slice slice)
    {
        return Slices.wrappedBuffer(Base64.getEncoder().encode(slice.getBytes()));
    }

    @Description("decode base64 encoded binary data")
    @ScalarFunction("from_base64")
    @LiteralParameters("x")
    @SqlType(StandardTypes.VARBINARY)
    public static Slice fromBase64Varchar(@SqlType("varchar(x)") Slice slice)
    {
        try {
            return Slices.wrappedBuffer(Base64.getDecoder().decode(slice.getBytes()));
        }
        catch (IllegalArgumentException e) {
            throw new PrestoException(INVALID_FUNCTION_ARGUMENT, e);
        }
    }

    @Description("decode base64 encoded binary data")
    @ScalarFunction("from_base64")
    @SqlType(StandardTypes.VARBINARY)
    public static Slice fromBase64Varbinary(@SqlType(StandardTypes.VARBINARY) Slice slice)
    {
        try {
            return Slices.wrappedBuffer(Base64.getDecoder().decode(slice.getBytes()));
        }
        catch (IllegalArgumentException e) {
            throw new PrestoException(INVALID_FUNCTION_ARGUMENT, e);
        }
    }

    @Description("encode binary data as base64 using the URL safe alphabet")
    @ScalarFunction("to_base64url")
    @SqlType(StandardTypes.VARCHAR)
    public static Slice toBase64Url(@SqlType(StandardTypes.VARBINARY) Slice slice)
    {
        return Slices.wrappedBuffer(Base64.getUrlEncoder().encode(slice.getBytes()));
    }

    @Description("decode URL safe base64 encoded binary data")
    @ScalarFunction("from_base64url")
    @SqlType(StandardTypes.VARBINARY)
    public static Slice fromBase64UrlVarchar(@SqlType(StandardTypes.VARCHAR) Slice slice)
    {
        try {
            return Slices.wrappedBuffer(Base64.getUrlDecoder().decode(slice.getBytes()));
        }
        catch (IllegalArgumentException e) {
            throw new PrestoException(INVALID_FUNCTION_ARGUMENT, e);
        }
    }

    @Description("decode URL safe base64 encoded binary data")
    @ScalarFunction("from_base64url")
    @SqlType(StandardTypes.VARBINARY)
    public static Slice fromBase64UrlVarbinary(@SqlType(StandardTypes.VARBINARY) Slice slice)
    {
        try {
            return Slices.wrappedBuffer(Base64.getUrlDecoder().decode(slice.getBytes()));
        }
        catch (IllegalArgumentException e) {
            throw new PrestoException(INVALID_FUNCTION_ARGUMENT, e);
        }
    }

    @Description("encode binary data as hex")
    @ScalarFunction
    @SqlType(StandardTypes.VARCHAR)
    public static Slice toHex(@SqlType(StandardTypes.VARBINARY) Slice slice)
    {
        return Slices.utf8Slice(BaseEncoding.base16().encode(slice.getBytes()));
    }

    @Description("decode hex encoded binary data")
    @ScalarFunction("from_hex")
    @SqlType(StandardTypes.VARBINARY)
    public static Slice fromHexVarchar(@SqlType(StandardTypes.VARCHAR) Slice slice)
    {
        if (slice.length() % 2 != 0) {
            throw new PrestoException(INVALID_FUNCTION_ARGUMENT, "invalid input length " + slice.length());
        }

        byte[] result = new byte[slice.length() / 2];
        for (int i = 0; i < slice.length(); i += 2) {
            result[i / 2] = (byte) ((hexDigitCharToInt(slice.getByte(i)) << 4) | hexDigitCharToInt(slice.getByte(i + 1)));
        }
        return Slices.wrappedBuffer(result);
    }

    @Description("encode value as a 64-bit 2's complement big endian varbinary")
    @ScalarFunction("to_big_endian_64")
    @SqlType(StandardTypes.VARBINARY)
    public static Slice toBigEndian64(@SqlType(StandardTypes.BIGINT) long value)
    {
        Slice slice = Slices.allocate(Long.BYTES);
        slice.setLong(0, Long.reverseBytes(value));
        return slice;
    }

    @Description("decode bigint value from a 64-bit 2's complement big endian varbinary")
    @ScalarFunction("from_big_endian_64")
    @SqlType(StandardTypes.BIGINT)
    public static long fromBigEndian64(@SqlType(StandardTypes.VARBINARY) Slice slice)
    {
        if (slice.length() != Long.BYTES) {
            throw new PrestoException(INVALID_FUNCTION_ARGUMENT, "expected 8-byte input, but got instead: " + slice.length());
        }
        return Long.reverseBytes(slice.getLong(0));
    }

    @Description("compute md5 hash")
    @ScalarFunction
    @SqlType(StandardTypes.VARBINARY)
    public static Slice md5(@SqlType(StandardTypes.VARBINARY) Slice slice)
    {
        return Slices.wrappedBuffer(Hashing.md5().hashBytes(slice.getBytes()).asBytes());
    }

    @Description("compute sha1 hash")
    @ScalarFunction
    @SqlType(StandardTypes.VARBINARY)
    public static Slice sha1(@SqlType(StandardTypes.VARBINARY) Slice slice)
    {
        return Slices.wrappedBuffer(Hashing.sha1().hashBytes(slice.getBytes()).asBytes());
    }

    @Description("compute sha256 hash")
    @ScalarFunction
    @SqlType(StandardTypes.VARBINARY)
    public static Slice sha256(@SqlType(StandardTypes.VARBINARY) Slice slice)
    {
        return Slices.wrappedBuffer(Hashing.sha256().hashBytes(slice.getBytes()).asBytes());
    }

    @Description("compute sha512 hash")
    @ScalarFunction
    @SqlType(StandardTypes.VARBINARY)
    public static Slice sha512(@SqlType(StandardTypes.VARBINARY) Slice slice)
    {
        return Slices.wrappedBuffer(Hashing.sha512().hashBytes(slice.getBytes()).asBytes());
    }

    private static int hexDigitCharToInt(byte b)
    {
        if (b >= '0' && b <= '9') {
            return b - '0';
        }
        else if (b >= 'a' && b <= 'f') {
            return b - 'a' + 10;
        }
        else if (b >= 'A' && b <= 'F') {
            return b - 'A' + 10;
        }
        throw new PrestoException(INVALID_FUNCTION_ARGUMENT, "invalid hex character: " + (char) b);
    }

    @Description("compute xxhash64 hash")
    @ScalarFunction
    @SqlType(StandardTypes.VARBINARY)
    public static Slice xxhash64(@SqlType(StandardTypes.VARBINARY) Slice slice)
    {
        Slice hash = Slices.allocate(Long.BYTES);
        hash.setLong(0, Long.reverseBytes(XxHash64.hash(slice)));
        return hash;
    }

    @Description("decode hex encoded binary data")
    @ScalarFunction("from_hex")
    @SqlType(StandardTypes.VARBINARY)
    public static Slice fromHexVarbinary(@SqlType(StandardTypes.VARBINARY) Slice slice)
    {
        return fromHexVarchar(slice);
    }
}

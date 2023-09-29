package io.github.btarg.blockdata;

import org.apache.commons.lang.SerializationUtils;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

public class InformationDataType implements PersistentDataType<byte[], ChunkBlockInformation> {
    @Override
    public @NotNull Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @Override
    public @NotNull Class<ChunkBlockInformation> getComplexType() {
        return ChunkBlockInformation.class;
    }

    @Override
    public byte @NotNull [] toPrimitive(@NotNull ChunkBlockInformation complex, @NotNull PersistentDataAdapterContext context) {
        return SerializationUtils.serialize(complex);
    }

    @Override
    public @NotNull ChunkBlockInformation fromPrimitive(byte @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) {
        try {
            InputStream inputStream = new ByteArrayInputStream(primitive);
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            return (ChunkBlockInformation) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new ChunkBlockInformation(null);
    }
}

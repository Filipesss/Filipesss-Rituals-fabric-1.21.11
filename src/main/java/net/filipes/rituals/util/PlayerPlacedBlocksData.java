package net.filipes.rituals.util;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashSet;
import java.util.Set;

public class PlayerPlacedBlocksData extends SavedData {

    private static final String DATA_KEY = "rituals_player_placed_blocks";

    private final Set<Long> placedPositions = new HashSet<>();

    public PlayerPlacedBlocksData() {}

    private static final Codec<Set<Long>> SET_CODEC =
            Codec.LONG.listOf().xmap(HashSet::new, list -> list.stream().toList());

    public static final Codec<PlayerPlacedBlocksData> CODEC =
            SET_CODEC.fieldOf("positions").codec().xmap(
                    positions -> {
                        PlayerPlacedBlocksData data = new PlayerPlacedBlocksData();
                        data.placedPositions.addAll(positions);
                        return data;
                    },
                    data -> data.placedPositions
            );

    public static final SavedDataType<PlayerPlacedBlocksData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("rituals", DATA_KEY),
            PlayerPlacedBlocksData::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    public static PlayerPlacedBlocksData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public void addPlaced(BlockPos pos)    { placedPositions.add(pos.asLong());    setDirty(); }
    public void removePlaced(BlockPos pos) { placedPositions.remove(pos.asLong()); setDirty(); }
    public boolean isPlayerPlaced(BlockPos pos) { return placedPositions.contains(pos.asLong()); }
}
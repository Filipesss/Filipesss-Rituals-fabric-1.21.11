package net.filipes.rituals.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

public class RitualPedestalBlockEntity extends BlockEntity implements Container {

    private final NonNullList<ItemStack> items = NonNullList.withSize(32, ItemStack.EMPTY);

    @Nullable private UUID displayEntityUuid = null;

    private final List<UUID>   floatingItemUuids = new ArrayList<>();
    private final List<String> floatingItemIds   = new ArrayList<>();

    private static final int     MAX_FLOATING        = 6;
    private static final boolean ENABLE_PARTICLES    = true;
    private static final int     END_ROD_PARTICLE_COUNT = 1;
    private static final double  END_ROD_OFFSET      = 0.02;

    public RitualPedestalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RITUAL_PEDESTAL_BE, pos, state);
    }

    // -------------------------------------------------------------------------
    // Tick
    // -------------------------------------------------------------------------

    public void tick(Level world) {
        if (!(world instanceof ServerLevel serverLevel)) return;

        // --- Manage text display entity ---
        if (displayEntityUuid != null) {
            Entity existing = serverLevel.getEntity(displayEntityUuid);
            if (existing == null || existing.isRemoved()) displayEntityUuid = null;
        }

        if (displayEntityUuid == null) {
            Display.TextDisplay display = new Display.TextDisplay(EntityType.TEXT_DISPLAY, serverLevel);
            // Display entity setup
            display.setPos(
                    worldPosition.getX() + 0.5,
                    worldPosition.getY() + 1.8,
                    worldPosition.getZ() + 0.5
            );
            display.setBillboardConstraints(Display.BillboardConstraints.CENTER);
            display.setNoGravity(true);
            display.setInvulnerable(true);
            display.setSilent(true);
            display.setText(buildDisplayText());
            serverLevel.addFreshEntity(display);
            displayEntityUuid = display.getUUID();
        }

        // --- Distinct item types (insertion order, capped at MAX_FLOATING) ---
        LinkedHashMap<String, ItemStack> exemplarMap = new LinkedHashMap<>();
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                if (!exemplarMap.containsKey(id)) {
                    ItemStack ex = stack.copy();
                    ex.setCount(1);
                    exemplarMap.put(id, ex);
                }
                if (exemplarMap.size() >= MAX_FLOATING) break;
            }
        }

        if (exemplarMap.isEmpty()) {
            killAllFloatingItems(serverLevel);
            return;
        }

        int desired = exemplarMap.size();

        // Remove extra floating slots
        while (floatingItemUuids.size() > desired) {
            int idx = floatingItemUuids.size() - 1;
            UUID u = floatingItemUuids.remove(idx);
            floatingItemIds.remove(idx);
            Entity e = serverLevel.getEntity(u);
            if (e != null) e.discard();
        }

        List<String>    desiredIds      = new ArrayList<>(exemplarMap.keySet());
        List<ItemStack> desiredExemplars = new ArrayList<>(exemplarMap.values());

        for (int i = 0; i < desired; i++) {
            String    wantId   = desiredIds.get(i);
            ItemStack exemplar = desiredExemplars.get(i);

            if (i < floatingItemUuids.size()) {
                UUID   uuid     = floatingItemUuids.get(i);
                Entity existing = serverLevel.getEntity(uuid);
                if (existing instanceof ItemEntity itemEnt && !existing.isRemoved()) {
                    String existingId = BuiltInRegistries.ITEM
                            .getKey(itemEnt.getItem().getItem()).toString();
                    if (!existingId.equals(wantId)) {
                        itemEnt.discard();
                        floatingItemUuids.set(i, null);
                        floatingItemIds.set(i, null);
                    } else {
                        orbitItemEntity(serverLevel, itemEnt, i, desired);
                        itemEnt.setNoGravity(true);
                        itemEnt.setInvulnerable(true);
                        itemEnt.setNeverPickUp();
                        itemEnt.setSilent(true);
                        itemEnt.setItem(exemplar.copy());
                        continue;
                    }
                } else {
                    floatingItemUuids.set(i, null);
                    floatingItemIds.set(i, null);
                }
            }

            // Spawn a new floating item
            ItemEntity floating = new ItemEntity(serverLevel,
                    worldPosition.getX() + 0.5,
                    worldPosition.getY() + 1.2,
                    worldPosition.getZ() + 0.5,
                    exemplar.copy()
            );
            floating.setNoGravity(true);
            floating.setInvulnerable(true);
            floating.setNeverPickUp();
            floating.setSilent(true);
            floating.setDeltaMovement(0.0, 0.0, 0.0);
            serverLevel.addFreshEntity(floating);

            if (i < floatingItemUuids.size()) {
                floatingItemUuids.set(i, floating.getUUID());
                floatingItemIds.set(i, wantId);
            } else {
                floatingItemUuids.add(floating.getUUID());
                floatingItemIds.add(wantId);
            }

            orbitItemEntity(serverLevel, floating, i, desired);
        }
    }

    private void orbitItemEntity(ServerLevel serverLevel, ItemEntity itemEnt, int index, int total) {
        long   time          = serverLevel.getGameTime();
        double angleProgress = time * 0.06;
        double spacing       = (Math.PI * 2.0) / Math.max(1, total);
        double angle         = angleProgress + index * spacing;
        double radius        = 0.6 + Math.min(0.35, total * 0.05);
        double cx = worldPosition.getX() + 0.5 + Math.cos(angle) * radius;
        double cz = worldPosition.getZ() + 0.5 + Math.sin(angle) * radius;
        double cy = worldPosition.getY() + 1.15 + Math.sin(angle * 2.0 + index) * 0.12;

        itemEnt.setPos(cx, cy, cz);

        if (ENABLE_PARTICLES) {
            serverLevel.sendParticles(
                    ParticleTypes.END_ROD,
                    cx, cy + 0.1, cz,
                    END_ROD_PARTICLE_COUNT,
                    END_ROD_OFFSET, END_ROD_OFFSET, END_ROD_OFFSET,
                    0.005
            );
        }
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @Override
    public void setRemoved() {
        super.setRemoved();
        killDisplayEntity();
        if (level instanceof ServerLevel serverLevel) killAllFloatingItems(serverLevel);
    }

    private void killDisplayEntity() {
        if (level instanceof ServerLevel serverLevel && displayEntityUuid != null) {
            Entity entity = serverLevel.getEntity(displayEntityUuid);
            if (entity != null) entity.discard();
            displayEntityUuid = null;
        }
    }

    private void killAllFloatingItems(ServerLevel serverLevel) {
        for (UUID u : floatingItemUuids) {
            if (u == null) continue;
            Entity e = serverLevel.getEntity(u);
            if (e != null) e.discard();
        }
        floatingItemUuids.clear();
        floatingItemIds.clear();
    }

    // -------------------------------------------------------------------------
    // Display text
    // -------------------------------------------------------------------------

    private Component buildDisplayText() {
        LinkedHashMap<String, Integer>   countMap = new LinkedHashMap<>();
        LinkedHashMap<String, Component> nameMap  = new LinkedHashMap<>();

        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                countMap.merge(id, stack.getCount(), Integer::sum);
                nameMap.putIfAbsent(id, stack.getHoverName().copy());
            }
        }

        if (countMap.isEmpty()) return Component.literal("(empty)");

        StringBuilder sb    = new StringBuilder();
        boolean       first = true;
        for (String id : countMap.keySet()) {
            if (!first) sb.append("\n");
            first = false;
            sb.append(nameMap.get(id).getString());
            int total = countMap.get(id);
            if (total > 1) sb.append(" x").append(total);
        }
        return Component.literal(sb.toString());
    }

    private void updateDisplayText() {
        if (!(level instanceof ServerLevel serverLevel) || displayEntityUuid == null) return;
        Entity entity = serverLevel.getEntity(displayEntityUuid);
        if (entity instanceof Display.TextDisplay display) display.setText(buildDisplayText());
    }

    // -------------------------------------------------------------------------
    // Container interface
    // -------------------------------------------------------------------------

    @Override public int  getContainerSize() { return items.size(); }

    @Override
    public boolean isEmpty() {
        for (ItemStack s : items) if (!s.isEmpty()) return false;
        return true;
    }

    @Override public ItemStack getItem(int slot) { return items.get(slot); }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack removed = ContainerHelper.removeItem(items, slot, amount);
        if (!removed.isEmpty()) setChanged();
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize());
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override public void clearContent() { items.clear(); setChanged(); }

    // -------------------------------------------------------------------------
    // Serialization
    // -------------------------------------------------------------------------

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items, true);

        if (displayEntityUuid != null)
            output.putString("DisplayEntityUUID", displayEntityUuid.toString());

        if (!floatingItemUuids.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < floatingItemUuids.size(); i++) {
                if (i != 0) sb.append(",");
                sb.append(floatingItemUuids.get(i));
            }
            output.putString("FloatingItemUUIDs", sb.toString());
        }
        if (!floatingItemIds.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < floatingItemIds.size(); i++) {
                if (i != 0) sb.append(",");
                sb.append(floatingItemIds.get(i));
            }
            output.putString("FloatingItemIds", sb.toString());
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, items);

        String raw = input.getStringOr("DisplayEntityUUID", "");
        if (!raw.isEmpty()) this.displayEntityUuid = UUID.fromString(raw);

        floatingItemUuids.clear();
        floatingItemIds.clear();

        String rawUuids = input.getStringOr("FloatingItemUUIDs", "");
        if (!rawUuids.isEmpty()) {
            for (String s : rawUuids.split(",")) {
                if (s.isEmpty()) continue;
                try { floatingItemUuids.add(UUID.fromString(s)); }
                catch (IllegalArgumentException ignored) {}
            }
        }
        String rawIds = input.getStringOr("FloatingItemIds", "");
        if (!rawIds.isEmpty()) {
            for (String s : rawIds.split(",")) floatingItemIds.add(s);
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        updateDisplayText();
        if (level != null && !level.isClientSide()) {
            BlockState state = level.getBlockState(worldPosition);
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
    }

    // -------------------------------------------------------------------------
    // Public helpers
    // -------------------------------------------------------------------------

    public ItemStack insertStack(ItemStack stack) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack toInsert = stack.copy();

        for (int i = 0; i < items.size() && !toInsert.isEmpty(); i++) {
            ItemStack slot = items.get(i);
            if (!slot.isEmpty() && ItemStack.isSameItemSameComponents(slot, toInsert)) {
                int space = Math.min(getMaxStackSize(), slot.getMaxStackSize()) - slot.getCount();
                if (space > 0) {
                    int put = Math.min(space, toInsert.getCount());
                    slot.grow(put);
                    toInsert.shrink(put);
                    items.set(i, slot);
                }
            }
        }
        for (int i = 0; i < items.size() && !toInsert.isEmpty(); i++) {
            if (items.get(i).isEmpty()) {
                items.set(i, toInsert.split(Math.min(toInsert.getCount(), getMaxStackSize())));
            }
        }

        if (toInsert.getCount() != stack.getCount()) setChanged();
        return toInsert;
    }

    public ItemStack removeFirstNonEmpty() {
        for (int i = 0; i < items.size(); i++) {
            if (!items.get(i).isEmpty()) {
                ItemStack copy = items.get(i).copy();
                items.set(i, ItemStack.EMPTY);
                setChanged();
                return copy;
            }
        }
        return ItemStack.EMPTY;
    }

    public List<ItemStack> removeAllOfFirstType() {
        String targetId = null;
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                targetId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                break;
            }
        }
        if (targetId == null) return List.of();

        List<ItemStack> removed = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty() &&
                    BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals(targetId)) {
                removed.add(stack.copy());
                items.set(i, ItemStack.EMPTY);
            }
        }
        if (!removed.isEmpty()) setChanged();
        return removed;
    }
}
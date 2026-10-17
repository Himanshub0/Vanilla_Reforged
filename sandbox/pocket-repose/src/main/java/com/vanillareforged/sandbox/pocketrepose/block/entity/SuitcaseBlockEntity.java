package com.vanillareforged.sandbox.pocketrepose.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class SuitcaseBlockEntity extends BlockEntity {

    private String boundKeystoneName = "";
    private boolean locked = false;
    private final List<UUID> enteredPlayers = new ArrayList<>();

    public SuitcaseBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SUITCASE_BLOCK_ENTITY, pos, state);
    }

    public String getBoundKeystoneName() {
        return this.boundKeystoneName;
    }

    public void bindKeystone(String keystoneName) {
        this.boundKeystoneName = keystoneName != null ? keystoneName.trim() : "";
        this.markDirty();
        if (this.world != null) {
            this.world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), 3);
        }
    }

    public boolean isLocked() {
        return this.locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
        this.markDirty();
        if (this.world != null) {
            this.world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), 3);
        }
    }

    public void addEnteredPlayer(UUID uuid) {
        if (!this.enteredPlayers.contains(uuid)) {
            this.enteredPlayers.add(uuid);
            this.markDirty();
        }
    }

    public void removeEnteredPlayer(UUID uuid) {
        if (this.enteredPlayers.remove(uuid)) {
            this.markDirty();
        }
    }

    public List<UUID> getEnteredPlayers() {
        return Collections.unmodifiableList(this.enteredPlayers);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        if (!this.boundKeystoneName.isEmpty()) {
            view.putString("BoundKeystone", this.boundKeystoneName);
        }
        view.putBoolean("Locked", this.locked);

        if (!this.enteredPlayers.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < this.enteredPlayers.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(this.enteredPlayers.get(i).toString());
            }
            view.putString("EnteredPlayers", sb.toString());
        }
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        this.boundKeystoneName = view.getString("BoundKeystone", "");
        this.locked = view.getBoolean("Locked", false);

        this.enteredPlayers.clear();
        String playersStr = view.getString("EnteredPlayers", "");
        if (!playersStr.isEmpty()) {
            String[] tokens = playersStr.split(",");
            for (String token : tokens) {
                try {
                    this.enteredPlayers.add(UUID.fromString(token.trim()));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        return this.createNbt(registries);
    }
}

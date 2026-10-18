package com.vanillareforged.mixin;

import com.vanillareforged.config.ModConfig;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Minecart Chunk Loading System.
 * Ensures minecarts dynamically keep chunks loaded (entity-ticking) around them,
 * allowing long-distance rail transit, automated item transport, and hopper minecarts
 * to travel seamlessly across the world without freezing in unloaded chunks.
 */
@Mixin(AbstractMinecartEntity.class)
public abstract class AbstractMinecartEntityMixin {

    @Unique
    private ChunkPos vanillaReforged$lastChunkPos;

    @Inject(method = "tick", at = @At("TAIL"))
    private void loadChunksAroundMinecart(CallbackInfo ci) {
        if (!ModConfig.MINECART_CHUNK_LOADING_ENABLED) {
            return;
        }

        AbstractMinecartEntity minecart = (AbstractMinecartEntity) (Object) this;
        World world = minecart.getEntityWorld();
        if (world == null || world.isClient() || !(world instanceof ServerWorld serverWorld)) {
            return;
        }

        if (!minecart.isAlive()) {
            return;
        }

        BlockPos currentPos = minecart.getBlockPos();
        ChunkPos currentChunk = new ChunkPos(currentPos);

        // Keep current chunk loaded: refresh ticket periodically or when entering a new chunk
        if (minecart.age % 20 == 0 || !currentChunk.equals(this.vanillaReforged$lastChunkPos)) {
            serverWorld.getChunkManager().addTicket(ChunkTicketType.PORTAL, currentChunk, ModConfig.MINECART_CHUNK_LOADING_RADIUS);
            this.vanillaReforged$lastChunkPos = currentChunk;
        }

        // Lookahead preloading: If the minecart is moving, preload the upcoming chunk along its travel direction
        Vec3d velocity = minecart.getVelocity();
        if (velocity.horizontalLengthSquared() > 0.005) {
            double speed = Math.sqrt(velocity.horizontalLengthSquared());
            double dirX = velocity.x / speed;
            double dirZ = velocity.z / speed;
            int aheadX = currentPos.getX() + (int) Math.round(dirX * 16.0);
            int aheadZ = currentPos.getZ() + (int) Math.round(dirZ * 16.0);
            ChunkPos aheadChunk = new ChunkPos(new BlockPos(aheadX, currentPos.getY(), aheadZ));

            if (!aheadChunk.equals(currentChunk) && (minecart.age % 10 == 0)) {
                serverWorld.getChunkManager().addTicket(ChunkTicketType.PORTAL, aheadChunk, ModConfig.MINECART_CHUNK_LOADING_RADIUS);
            }
        }
    }
}

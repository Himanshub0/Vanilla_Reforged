package com.vanillareforged.item;

import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;

public class ModSpawnEggItem extends SpawnEggItem {
    private final EntityType<?> targetEntityType;

    public ModSpawnEggItem(EntityType<?> entityType, Item.Settings settings) {
        super(settings.spawnEgg(entityType));
        this.targetEntityType = entityType;
    }

    public EntityType<?> getTargetEntityType() {
        return this.targetEntityType;
    }
}

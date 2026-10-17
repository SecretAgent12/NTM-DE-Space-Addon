// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity.machine;

import com.hbm.blockentity.PersistentDrop;
import com.hbm.blockentity.machine.BlockEntityCondenserPowered;
import com.hbm.interfaces.IRepairable;
import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.recipes.ingredient.CountIngredient;
import java.util.List;
import net.minecraft.core.BlockPos;
import com.hbm.backport.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import com.hbm.backport.storage.ValueInput;
import com.hbm.backport.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BlockEntityCondenserPoweredSpace extends BlockEntityCondenserPowered
        implements IRepairable, PersistentDrop {
    private static final List<CountIngredient> REPAIR_MATERIALS =
            List.of(
                    CountIngredient.of(OreDictManager.STEEL.plateWelded(), 4),
                    CountIngredient.of(OreDictManager.STEEL.pipe(), 12),
                    CountIngredient.of(OreDictManager.ANY_RESISTANTALLOY.plateWelded(), 2));

    public @Nullable Explosion lastExplosion;

    public BlockEntityCondenserPoweredSpace(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public boolean isDamaged() {
        return ThermalMachineDamage.damaged(this);
    }

    public void damage() {
        waterTimer = 0;
        ThermalMachineDamage.set(this, true);
    }

    @Override
    public void tickServer() {
        if (isDamaged()) {
            waterTimer = 0;
            networkPackNT(150);
            return;
        }
        super.tickServer();
    }

    @Override
    public List<CountIngredient> getRepairMaterials() {
        return REPAIR_MATERIALS;
    }

    @Override
    public void repair(Player player) {
        ThermalMachineDamage.set(this, false);
    }

    @Override
    public void tryExtinguish(Level level, BlockPos pos, EnumExtinguishType type) {}

    @Override
    public void writePersistent(DataComponentMap.Builder components) {
        ThermalMachineDamage.write(this, components);
    }

    @Override
    public void readPersistent(DataComponentGetter components) {
        ThermalMachineDamage.read(this, components);
    }

    @Override
    public String[] persistentKeys() {
        return ThermalMachineDamage.PERSISTENT_KEYS;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ThermalMachineDamage.set(this, input.getBooleanOr("damaged", isDamaged()));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("damaged", isDamaged());
    }


    // backport: woven trait PersistentDrop
    public void stripPersistent(ValueOutput output) {
        for (String key : persistentKeys()) output.discard(key);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        writePersistent(components);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        readPersistent(components);
    }

    @Override
    public void removeComponentsFromTag(ValueOutput output) {
        super.removeComponentsFromTag(output);
        stripPersistent(output);
    }
}

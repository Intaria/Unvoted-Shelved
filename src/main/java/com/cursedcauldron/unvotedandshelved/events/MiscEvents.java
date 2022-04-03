package com.cursedcauldron.unvotedandshelved.events;

import com.cursedcauldron.unvotedandshelved.UnvotedAndShelved;
import com.cursedcauldron.unvotedandshelved.block.WeatheringCopperButtonBlock;
import com.cursedcauldron.unvotedandshelved.init.USBlocks;
import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = UnvotedAndShelved.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MiscEvents {
    public static final Supplier<BiMap<Block, Block>> WAXABLES = Suppliers.memoize(() -> ImmutableBiMap.<Block, Block>builder().put(USBlocks.COPPER_BUTTON.get(), USBlocks.WAXED_COPPER_BUTTON.get()).put(USBlocks.EXPOSED_COPPER_BUTTON.get(), USBlocks.WAXED_EXPOSED_COPPER_BUTTON.get()).put(USBlocks.WEATHERED_COPPER_BUTTON.get(), USBlocks.WAXED_WEATHERED_COPPER_BUTTON.get()).put(USBlocks.OXIDIZED_COPPER_BUTTON.get(), USBlocks.WAXED_OXIDIZED_COPPER_BUTTON.get()).build());
    public static final Supplier<BiMap<Block, Block>> WAX_OFF_BY_BLOCK = Suppliers.memoize(() -> WAXABLES.get().inverse());

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        BlockPos blockPos = event.getPos();
        Level world = event.getWorld();
        BlockState state = world.getBlockState(blockPos);
        Player player = event.getPlayer();
        ItemStack stack = event.getItemStack();
        InteractionHand hand = event.getHand();
        if (stack.getItem() == Items.HONEYCOMB) {
            Optional<BlockState> waxables = Optional.ofNullable(WAXABLES.get().get(state.getBlock())).map((blockState) -> blockState.withPropertiesOf(state));
            if (waxables.isPresent()) {
                event.setCanceled(true);
                if (player instanceof ServerPlayer) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer) player, blockPos, stack);
                }
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                world.setBlock(blockPos, waxables.get(), 1);
                world.levelEvent(player, 3003, blockPos, 0);
//                player.swing(hand);
                event.setCancellationResult(InteractionResult.SUCCESS);
            }
        }
        if (stack.getItem() instanceof AxeItem) {
            Optional<BlockState> finalState = Optional.empty();
            if (state.getBlock() instanceof WeatheringCopperButtonBlock) {
                Optional<BlockState> previous = WeatheringCopperButtonBlock.getPreviousState(state);
                if (previous.isPresent()) {
                    world.playSound(player, blockPos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0F, 1.0F);
                    world.levelEvent(player, 3005, blockPos, 0);
                    finalState = previous;
                }
            }
            Optional<BlockState> previousWaxed = Optional.ofNullable(WAX_OFF_BY_BLOCK.get().get(state.getBlock())).map((blockState) -> blockState.withPropertiesOf(state));
            if (previousWaxed.isPresent()) {
                world.playSound(player, blockPos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0F, 1.0F);
                world.levelEvent(player, 3004, blockPos, 0);
                finalState = previousWaxed;
            }
            if (finalState.isPresent()) {
                event.setCanceled(true);
                world.setBlock(blockPos, finalState.get(), 11);
                stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                player.swing(hand);
                event.setCancellationResult(InteractionResult.SUCCESS);
            }
        }
    }
}
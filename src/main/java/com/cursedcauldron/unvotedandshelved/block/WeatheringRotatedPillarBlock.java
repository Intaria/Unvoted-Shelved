package com.cursedcauldron.unvotedandshelved.block;

import com.cursedcauldron.unvotedandshelved.api.IWeatheringObject;
import com.cursedcauldron.unvotedandshelved.init.USBlocks;
import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

public class WeatheringRotatedPillarBlock extends ConnectedRotatedPillarBlock implements WeatheringCopper, IWeatheringObject {
    public static final BooleanProperty CONNECTED = BooleanProperty.create("connected");
    private final WeatheringCopper.WeatherState weatherState;
    public static Supplier<BiMap<Block, Block>> NEXT_BY_BLOCK = Suppliers.memoize(() -> {
        return ImmutableBiMap.<Block, Block>builder()
                .put(USBlocks.COPPER_PILLAR.get(), USBlocks.EXPOSED_COPPER_PILLAR.get())
                .put(USBlocks.EXPOSED_COPPER_PILLAR.get(), USBlocks.WEATHERED_COPPER_PILLAR.get())
                .put(USBlocks.WEATHERED_COPPER_PILLAR.get(), USBlocks.OXIDIZED_COPPER_PILLAR.get())
                .build();
    });
    public static final Supplier<BiMap<Block, Block>> PREVIOUS_BY_BLOCK = Suppliers.memoize(() -> {
        return NEXT_BY_BLOCK.get().inverse();
    });

    public WeatheringRotatedPillarBlock(WeatherState state, Properties properties) {
        super(state, properties);
        this.weatherState = state;
        this.registerDefaultState(this.stateDefinition.any().setValue(CONNECTED, false));
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        this.onRandomTick(state, level, pos, random);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return Optional.ofNullable(NEXT_BY_BLOCK.get().get(state.getBlock())).isPresent();
    }

    @Override
    public Optional<BlockState> getNext(BlockState state) {
        return Optional.ofNullable(NEXT_BY_BLOCK.get().get(state.getBlock())).map(block -> block.withPropertiesOf(state));
    }

    @Override
    public WeatherState getAge() {
        return this.weatherState;
    }

    @Override
    public Optional<BlockState> getPrevState(BlockState state) {
        return Optional.ofNullable(PREVIOUS_BY_BLOCK.get().get(state.getBlock())).map(block -> block.withPropertiesOf(state));
    }

}

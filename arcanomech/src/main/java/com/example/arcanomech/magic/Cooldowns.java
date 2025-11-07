package com.example.arcanomech.magic;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.example.arcanomech.Arcanomech;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;

public final class Cooldowns {
    private static final String STATE_ID = "arcanomech_spell_cooldowns";

    private Cooldowns() {
    }

    public static boolean isOnCooldown(PlayerEntity player, SpellId id) {
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) {
            return false;
        }
        long now = serverWorld.getTime();
        SpellCooldownState state = SpellCooldownState.get(serverWorld);
        return state.getRemaining(player.getUuid(), id.id(), now) > 0;
    }

    public static int getRemainingTicks(PlayerEntity player, SpellId id) {
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) {
            return 0;
        }
        long now = serverWorld.getTime();
        SpellCooldownState state = SpellCooldownState.get(serverWorld);
        return state.getRemaining(player.getUuid(), id.id(), now);
    }

    public static void setCooldown(PlayerEntity player, SpellId id, int cooldown) {
        if (cooldown <= 0) {
            return;
        }
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }
        SpellCooldownState state = SpellCooldownState.get(serverWorld);
        long end = serverWorld.getTime() + cooldown;
        state.set(player.getUuid(), id.id(), end);
    }

    private static final class SpellCooldownState extends PersistentState {
        private final Map<UUID, Map<Identifier, Long>> cooldowns = new HashMap<>();

        private SpellCooldownState() {
        }

        private static SpellCooldownState get(ServerWorld world) {
            return world.getPersistentStateManager()
                    .getOrCreate(SpellCooldownState::fromNbt, SpellCooldownState::new, STATE_ID);
        }

        private int getRemaining(UUID playerId, Identifier id, long now) {
            Map<Identifier, Long> playerCooldowns = cooldowns.get(playerId);
            if (playerCooldowns == null) {
                return 0;
            }
            Long end = playerCooldowns.get(id);
            if (end == null) {
                return 0;
            }
            if (end <= now) {
                playerCooldowns.remove(id);
                markDirty();
                return 0;
            }
            return (int) Math.max(0, end - now);
        }

        private void set(UUID playerId, Identifier id, long endTick) {
            cooldowns.computeIfAbsent(playerId, ignored -> new HashMap<>()).put(id, endTick);
            markDirty();
        }

        @Override
        public NbtCompound writeNbt(NbtCompound nbt) {
            for (Map.Entry<UUID, Map<Identifier, Long>> entry : cooldowns.entrySet()) {
                NbtCompound playerTag = new NbtCompound();
                for (Map.Entry<Identifier, Long> cooldown : entry.getValue().entrySet()) {
                    playerTag.putLong(cooldown.getKey().toString(), cooldown.getValue());
                }
                nbt.put(entry.getKey().toString(), playerTag);
            }
            return nbt;
        }

        private static SpellCooldownState fromNbt(NbtCompound nbt) {
            SpellCooldownState state = new SpellCooldownState();
            for (String key : nbt.getKeys()) {
                try {
                    UUID playerId = UUID.fromString(key);
                    NbtCompound playerTag = nbt.getCompound(key);
                    Map<Identifier, Long> values = new HashMap<>();
                    for (String spellKey : playerTag.getKeys()) {
                        values.put(new Identifier(spellKey), playerTag.getLong(spellKey));
                    }
                    state.cooldowns.put(playerId, values);
                } catch (IllegalArgumentException exception) {
                    Arcanomech.LOGGER.warn("Skipping malformed cooldown entry {}", key, exception);
                }
            }
            return state;
        }
    }
}

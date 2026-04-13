package net.filipes.rituals.client.cooldown;

import java.util.LinkedHashMap;
import java.util.Map;

public class CooldownManager {

    public record AbilityDefinition(String displayName, long durationMs, int barColor) {}

    private static final Map<String, AbilityDefinition> definitions = new LinkedHashMap<>();
    private static final Map<String, Long> activeCooldowns = new LinkedHashMap<>();

    /**
     * Register an ability. Call this once during mod init.
     * barColor is RGB e.g. 0xFF4444
     */
    public static void register(String id, String displayName, long durationMs, int barColor) {
        definitions.put(id, new AbilityDefinition(displayName, durationMs, barColor));
    }

    /**
     * Start the cooldown for an ability. Call this when the ability is used.
     * Only call on the client side (level.isClientSide).
     */
    public static void trigger(String id) {
        if (!definitions.containsKey(id))
            throw new IllegalArgumentException("Unknown ability id: " + id);
        activeCooldowns.put(id, System.currentTimeMillis());
    }

    public static boolean isOnCooldown(String id) {
        Long start = activeCooldowns.get(id);
        if (start == null) return false;
        AbilityDefinition def = definitions.get(id);
        if (def == null) return false;
        return System.currentTimeMillis() - start < def.durationMs();
    }

    /** Returns 0.0 when just triggered, 1.0 when cooldown is over. */
    public static float getProgress(String id) {
        Long start = activeCooldowns.get(id);
        if (start == null) return 1.0f;
        AbilityDefinition def = definitions.get(id);
        if (def == null) return 1.0f;
        return Math.min(1.0f, (float)(System.currentTimeMillis() - start) / def.durationMs());
    }

    public static long getRemainingMs(String id) {
        Long start = activeCooldowns.get(id);
        if (start == null) return 0;
        AbilityDefinition def = definitions.get(id);
        if (def == null) return 0;
        return Math.max(0, def.durationMs() - (System.currentTimeMillis() - start));
    }

    public static Map<String, AbilityDefinition> getDefinitions() { return definitions; }
    public static Map<String, Long> getActiveCooldowns() { return activeCooldowns; }
}
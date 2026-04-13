package net.filipes.rituals.upgrade;

public class KillUpgradeRecipe {

    private final int resultStage;
    private final int killsRequired;

    public KillUpgradeRecipe(int resultStage, int killsRequired) {
        this.resultStage = resultStage;
        this.killsRequired = killsRequired;
    }

    public int getResultStage() { return resultStage; }
    public int getKillsRequired() { return killsRequired; }
}
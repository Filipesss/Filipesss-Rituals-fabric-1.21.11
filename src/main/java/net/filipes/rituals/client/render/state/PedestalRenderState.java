package net.filipes.rituals.client.render.state;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.util.FormattedCharSequence; // Replaces OrderedText

import java.util.ArrayList;
import java.util.List;

public class PedestalRenderState extends BlockEntityRenderState {
    public final List<FormattedCharSequence> lines = new ArrayList<>();
}
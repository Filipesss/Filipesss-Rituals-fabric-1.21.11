package net.filipes.rituals.entity.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;

public class PulseBlasterBeamModel {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath("rituals", "pulse_blaster_beam"), "main");

    private final ModelPart bone;

    public PulseBlasterBeamModel(ModelPart root) {
        this.bone = root.getChild("bone");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-8.001F, -7.0F, 7.0F, 0.001F, 7.0F, 2.0F, CubeDeformation.NONE)
                        .texOffs(4, 16).addBox(-8.05F, -7.025F, 7.05F, 0.1F, 7.05F, 1.9F, CubeDeformation.NONE)
                        .texOffs(0, 14).addBox(-8.125F, -7.05F, 7.15F, 0.25F, 7.1F, 1.7F, CubeDeformation.NONE)
                        .texOffs(3, 10).mirror().addBox(-8.025F, -7.5F, 7.5F, 0.05F, 8.0F, 1.0F, CubeDeformation.NONE).mirror(false)
                        .texOffs(4, 12).addBox(-8.075F, -7.5751F, 7.55F, 0.15F, 8.1501F, 0.9F, CubeDeformation.NONE),
                PartPose.offset(8.0F, 24.0F, -8.0F));

        bone.addOrReplaceChild("cube_r1", CubeListBuilder.create()
                        .texOffs(12, 8).addBox(-1.075F, -7.575F, 0.55F, 0.15F, 8.15F, 0.9F, CubeDeformation.NONE)
                        .texOffs(8, 14).addBox(-1.025F, -7.5F, 0.5F, 0.05F, 8.0F, 1.0F, CubeDeformation.NONE)
                        .texOffs(10, 4).addBox(-1.125F, -7.05F, 0.175F, 0.25F, 7.1F, 1.65F, CubeDeformation.NONE)
                        .texOffs(8, 0).addBox(-1.05F, -7.025F, 0.075F, 0.1F, 7.05F, 1.85F, CubeDeformation.NONE)
                        .texOffs(4, 0).addBox(-1.0F, -7.0F, 0.0F, 0.0F, 7.0F, 2.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-7.0F, 0.0F, 9.0F, 0.0F, -1.5708F, 0.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    public ModelPart getBone() {
        return bone;
    }
}
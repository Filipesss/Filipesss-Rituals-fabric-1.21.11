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
                        .texOffs(0, 0).addBox(0.0F, -11.0F, -1.0F, 0.0F, 12.0F, 2.0F, CubeDeformation.NONE)
                        .texOffs(8, 11).addBox(0.0F, -10.0F, 1.0F, 0.0F, 10.0F, 1.0F, CubeDeformation.NONE)
                        .texOffs(10, 11).addBox(0.0F, -10.0F, -2.0F, 0.0F, 10.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        bone.addOrReplaceChild("cube_r1", CubeListBuilder.create()
                        .texOffs(4, 0).addBox(0.0F, -11.0F, -1.0F, 0.0F, 12.0F, 2.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        bone.addOrReplaceChild("cube_r2", CubeListBuilder.create()
                        .texOffs(10, 0).addBox(0.0F, -10.0F, -1.0F, 0.0F, 10.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-1.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        bone.addOrReplaceChild("cube_r3", CubeListBuilder.create()
                        .texOffs(8, 0).addBox(0.0F, -10.0F, -1.0F, 0.0F, 10.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(2.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    public ModelPart getBone() {
        return bone;
    }
}
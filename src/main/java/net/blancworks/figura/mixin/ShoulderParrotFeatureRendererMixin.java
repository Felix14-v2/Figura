package net.blancworks.figura.mixin;

import net.blancworks.figura.avatar.AvatarData;
import net.blancworks.figura.avatar.AvatarDataManager;
import net.blancworks.figura.access.MatrixStackAccess;
import net.blancworks.figura.lua.api.model.ParrotModelAPI;
import net.blancworks.figura.lua.api.model.VanillaModelPartCustomization;
import net.blancworks.figura.trust.TrustContainer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ParrotEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.feature.ShoulderParrotFeatureRenderer;
import net.minecraft.client.render.entity.model.ParrotEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShoulderParrotFeatureRenderer.class)
public class ShoulderParrotFeatureRendererMixin<T extends PlayerEntity> extends FeatureRenderer<T, PlayerEntityModel<T>> {
    public ShoulderParrotFeatureRendererMixin(FeatureRendererContext<T, PlayerEntityModel<T>> context) {
        super(context);
    }

    @Shadow @Final private ParrotEntityModel model;

    public VanillaModelPartCustomization figura$customization;

    private int figura$pushedMatrixCount = 0;

    @Inject(at = @At("HEAD"), method = "renderShoulderParrot", cancellable = true)
    public void onRenderShoulder(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T player, float limbAngle, float limbDistance, float headYaw, float headPitch, boolean leftShoulder, CallbackInfo ci) {
        AvatarData data = AvatarDataManager.getDataForPlayer(player.getUuid());
        if (data == null || data.getTrustContainer().get(TrustContainer.Trust.VANILLA_MODEL_EDIT) == 0)
            return;

        try {
            if (data.model != null) {
                VanillaModelPartCustomization originModification = leftShoulder ? data.model.originModifications.get(ParrotModelAPI.VANILLA_LEFT_PARROT_ID) : data.model.originModifications.get(ParrotModelAPI.VANILLA_RIGHT_PARROT_ID);

                if (originModification != null) {
                    if (originModification.part == null || originModification.visible == null || data.getComplexity() > data.getTrustContainer().get(TrustContainer.Trust.COMPLEXITY)) {
                        ci.cancel();
                        return;
                    }

                    if (originModification.stackReference != null) {
                        //apply modifications
                        MatrixStack freshStack = new MatrixStack();
                        MatrixStackAccess access = (MatrixStackAccess) freshStack;
                        access.pushEntry(originModification.stackReference);

                        //flag to not render anymore
                        originModification.visible = null;

                        //render
                        NbtCompound compoundTag = leftShoulder ? player.getShoulderEntityLeft() : player.getShoulderEntityRight();
                        EntityType.get(compoundTag.getString("id")).filter((entityType) -> entityType == EntityType.PARROT).ifPresent((entityType) -> {
                            freshStack.push();
                            freshStack.translate(0f, -1.5f, 0f);
                            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(this.model.getLayer(ParrotEntityRenderer.TEXTURES[compoundTag.getInt("Variant")]));
                            this.model.poseOnShoulder(freshStack, vertexConsumer, light, OverlayTexture.DEFAULT_UV, limbAngle, limbDistance, headYaw, headPitch, player.age);
                            freshStack.pop();
                        });

                        ci.cancel();
                        return;
                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        try {
            if (data.script != null && data.script.allCustomizations != null) {
                figura$customization = data.script.allCustomizations.get(leftShoulder ? ParrotModelAPI.VANILLA_LEFT_PARROT : ParrotModelAPI.VANILLA_RIGHT_PARROT);
                if (figura$customization != null) {
                    if (figura$customization.visible != null && !figura$customization.visible) {
                        ci.cancel();
                        return;
                    }

                    matrices.push();
                    figura$pushedMatrixCount++;

                    if (figura$customization.pos != null)
                        matrices.translate(figura$customization.pos.getX() / 16.0f, figura$customization.pos.getY() / 16.0f, figura$customization.pos.getZ() / 16.0f);

                    if (figura$customization.rot != null) {
                        matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(figura$customization.rot.getZ()));
                        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(figura$customization.rot.getY()));
                        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(figura$customization.rot.getX()));
                    }

                    if (figura$customization.scale != null) {
                        Vec3f scale = figura$customization.scale;
                        matrices.scale(scale.getX(), scale.getY(), scale.getZ());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Inject(at = @At("RETURN"), method = "renderShoulderParrot")
    public void postRenderShoulder(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T player, float limbAngle, float limbDistance, float headYaw, float headPitch, boolean leftShoulder, CallbackInfo ci) {
        for (int i = 0; i < figura$pushedMatrixCount; i++)
            matrices.pop();

        figura$pushedMatrixCount = 0;
    }

    @Shadow
    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {

    }
}

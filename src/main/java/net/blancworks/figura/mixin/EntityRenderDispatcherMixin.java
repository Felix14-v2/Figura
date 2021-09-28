package net.blancworks.figura.mixin;

import net.blancworks.figura.PlayerData;
import net.blancworks.figura.PlayerDataManager;
import net.blancworks.figura.trust.PlayerTrustManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Shadow private boolean renderShadows;
    private boolean renderShadowOld;

    private final Predicate<Entity> MOUNT_DISABLED_PREDICATE = (entity -> {
        if (entity instanceof PlayerEntity) {
            PlayerData data = PlayerDataManager.getDataForPlayer(entity.getUuid());
            if (data != null && data.script != null && data.getTrustContainer().getBoolSetting(PlayerTrustManager.ALLOW_VANILLA_MOD_ID)) {
                this.renderShadows = data.script.renderMountShadow;
                return !data.script.renderMount;
            }
        }
        return false;
    });

    @Inject(at = @At("HEAD"), method = "render")
    public <E extends Entity> void render(E entity, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        renderShadowOld = this.renderShadows;
    }

    @Redirect(method = "render", at = @At(target = "Lnet/minecraft/client/render/entity/EntityRenderer;render(Lnet/minecraft/entity/Entity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", value = "INVOKE"))
    private <T extends Entity>void renderRenderEntity(EntityRenderer<T> entityRenderer, T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (!MOUNT_DISABLED_PREDICATE.test(entity.getPrimaryPassenger())) {
            entityRenderer.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private <T extends Entity> void renderEnd(T entity, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        this.renderShadows = renderShadowOld;
    }
}

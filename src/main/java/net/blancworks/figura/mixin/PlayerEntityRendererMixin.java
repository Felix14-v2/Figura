package net.blancworks.figura.mixin;

import net.blancworks.figura.Config;
import net.blancworks.figura.FiguraMod;
import net.blancworks.figura.PlayerData;
import net.blancworks.figura.PlayerDataManager;
import net.blancworks.figura.access.ModelPartAccess;
import net.blancworks.figura.access.PlayerEntityRendererAccess;
import net.blancworks.figura.lua.api.model.VanillaModelAPI;
import net.blancworks.figura.lua.api.model.VanillaModelPartCustomization;
import net.blancworks.figura.lua.api.nameplate.NamePlateAPI;
import net.blancworks.figura.lua.api.nameplate.NamePlateCustomization;
import net.blancworks.figura.trust.PlayerTrustManager;
import net.blancworks.figura.utils.TextUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.client.util.math.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> implements PlayerEntityRendererAccess {

    PlayerEntityRendererMixin(EntityRenderDispatcher dispatcher, PlayerEntityModel<AbstractClientPlayerEntity> model, float shadowRadius) {
        super(dispatcher, model, shadowRadius);
    }

    @Shadow protected abstract void renderLabelIfPresent(AbstractClientPlayerEntity abstractClientPlayerEntity, Text text, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i);

    private final ArrayList<ModelPart> figura$customizedParts = new ArrayList<>();

    @Inject(at = @At("HEAD"), method = "render")
    public void onRender(AbstractClientPlayerEntity abstractClientPlayerEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        FiguraMod.setRenderingData(abstractClientPlayerEntity, vertexConsumerProvider, this.getModel(), MinecraftClient.getInstance().getTickDelta());

        shadowRadius = 0.5f; //Vanilla shadow radius.
        //Reset this here because... Execution order.

        if (FiguraMod.currentData != null) {
            if (FiguraMod.currentData.getTrustContainer().getBoolSetting(PlayerTrustManager.ALLOW_VANILLA_MOD_ID)) {
                figura$applyPartCustomization(VanillaModelAPI.VANILLA_HEAD, this.getModel().head);
                figura$applyPartCustomization(VanillaModelAPI.VANILLA_TORSO, this.getModel().torso);
                figura$applyPartCustomization(VanillaModelAPI.VANILLA_LEFT_ARM, this.getModel().leftArm);
                figura$applyPartCustomization(VanillaModelAPI.VANILLA_RIGHT_ARM, this.getModel().rightArm);
                figura$applyPartCustomization(VanillaModelAPI.VANILLA_LEFT_LEG, this.getModel().leftLeg);
                figura$applyPartCustomization(VanillaModelAPI.VANILLA_RIGHT_LEG, this.getModel().rightLeg);

                figura$applyPartCustomization(VanillaModelAPI.VANILLA_HAT, this.getModel().helmet);
                figura$applyPartCustomization(VanillaModelAPI.VANILLA_JACKET, this.getModel().jacket);
                figura$applyPartCustomization(VanillaModelAPI.VANILLA_LEFT_SLEEVE, this.getModel().leftSleeve);
                figura$applyPartCustomization(VanillaModelAPI.VANILLA_RIGHT_SLEEVE, this.getModel().rightSleeve);
                figura$applyPartCustomization(VanillaModelAPI.VANILLA_LEFT_PANTS, this.getModel().leftPantLeg);
                figura$applyPartCustomization(VanillaModelAPI.VANILLA_RIGHT_PANTS, this.getModel().rightPantLeg);

                if (FiguraMod.currentData.getTrustContainer().getBoolSetting(PlayerTrustManager.ALLOW_VANILLA_MOD_ID) && FiguraMod.currentData.script != null && FiguraMod.currentData.script.customShadowSize != null) {
                    shadowRadius = FiguraMod.currentData.script.customShadowSize;
                }
            }
        }
    }

    @Override
    public boolean shouldRender(AbstractClientPlayerEntity entity, Frustum frustum, double x, double y, double z) {
        PlayerData data = PlayerDataManager.getDataForPlayer(entity.getGameProfile().getId());

        if (data != null) {
            //ikr this is not the right place, but no need to create a new mixin only for this
            if (data.script != null)
                data.script.onWorldRender(MinecraftClient.getInstance().getTickDelta());

            if (data.getTrustContainer().getBoolSetting(PlayerTrustManager.ALLOW_OFFSCREEN_RENDERING))
                return true;
        }

        return super.shouldRender(entity, frustum, x, y, z);
    }

    @Inject(at = @At("RETURN"), method = "render")
    public void postRender(AbstractClientPlayerEntity abstractClientPlayerEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (FiguraMod.currentData != null && FiguraMod.currentData.lastEntity != null) {
            PlayerData currData = FiguraMod.currentData;

            if (currData.script != null && currData.script.isDone) {
                for (VanillaModelAPI.ModelPartTable partTable : currData.script.vanillaModelPartTables) {
                    partTable.updateFromPart();
                }
            }
        }

        FiguraMod.clearRenderingData();
        figura$clearAllPartCustomizations();
    }

    @Inject(at = @At("HEAD"), method = "renderArm")
    private void onRenderArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, ModelPart arm, ModelPart sleeve, CallbackInfo ci) {
        FiguraMod.setRenderingData(player, vertexConsumers, this.getModel(), MinecraftClient.getInstance().getTickDelta());

        figura$applyPartCustomization(VanillaModelAPI.VANILLA_HEAD, this.getModel().head);
        figura$applyPartCustomization(VanillaModelAPI.VANILLA_TORSO, this.getModel().torso);
        figura$applyPartCustomization(VanillaModelAPI.VANILLA_LEFT_ARM, this.getModel().leftArm);
        figura$applyPartCustomization(VanillaModelAPI.VANILLA_RIGHT_ARM, this.getModel().rightArm);
        figura$applyPartCustomization(VanillaModelAPI.VANILLA_LEFT_LEG, this.getModel().leftLeg);
        figura$applyPartCustomization(VanillaModelAPI.VANILLA_RIGHT_LEG, this.getModel().rightLeg);

        figura$applyPartCustomization(VanillaModelAPI.VANILLA_HAT, this.getModel().helmet);
        figura$applyPartCustomization(VanillaModelAPI.VANILLA_JACKET, this.getModel().jacket);
        figura$applyPartCustomization(VanillaModelAPI.VANILLA_LEFT_SLEEVE, this.getModel().leftSleeve);
        figura$applyPartCustomization(VanillaModelAPI.VANILLA_RIGHT_SLEEVE, this.getModel().rightSleeve);
        figura$applyPartCustomization(VanillaModelAPI.VANILLA_LEFT_PANTS, this.getModel().leftPantLeg);
        figura$applyPartCustomization(VanillaModelAPI.VANILLA_RIGHT_PANTS, this.getModel().rightPantLeg);
    }

    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"), cancellable = true)
    private void renderFiguraLabelIfPresent(AbstractClientPlayerEntity entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        //get uuid and name
        UUID uuid = entity.getGameProfile().getId();
        String playerName = entity.getEntityName();

        //check for data and trust settings
        PlayerData currentData = PlayerDataManager.getDataForPlayer(uuid);
        if (!(boolean) Config.entries.get("nameTagMods").value || currentData == null || playerName.equals("") || !currentData.getTrustContainer().getBoolSetting(PlayerTrustManager.ALLOW_NAMEPLATE_MOD_ID))
            return;

        //cancel callback info
        ci.cancel();

        //nameplate
        NamePlateCustomization nameplateData = currentData.script == null ? null : currentData.script.nameplateCustomizations.get(NamePlateAPI.ENTITY);

        //apply text and/or badges
        try {
            if (text instanceof TranslatableText) {
                Object[] args = ((TranslatableText) text).getArgs();

                for (Object arg : args) {
                    if (NamePlateAPI.applyFormattingRecursive((LiteralText) arg, uuid, playerName, nameplateData, currentData))
                        break;
                }
            } else if (text instanceof LiteralText) {
                NamePlateAPI.applyFormattingRecursive((LiteralText) text, uuid, playerName, nameplateData, currentData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //nameplate transformations
        float spacing = entity.getHeight() + 0.5F;
        Vector3f translation = new Vector3f(0.0f, spacing, 0.0f);
        Vector3f scale = new Vector3f(1.0f, 1.0f, 1.0f);
        boolean enabled = true;

        //apply main nameplate transformations
        if (nameplateData != null) {
            if (nameplateData.enabled != null)
                enabled = nameplateData.enabled;
            if (nameplateData.position != null)
                translation.add(nameplateData.position);
            if (nameplateData.scale != null)
                scale = nameplateData.scale;
        }

        matrices.push();
        matrices.translate(translation.getX(), translation.getY(), translation.getZ());
        matrices.scale(scale.getX(), scale.getY(), scale.getZ());

        //render scoreboard
        double distance = this.dispatcher.getSquaredDistanceToCamera(entity);
        if (enabled && distance < 100.0D) {
            //get scoreboard
            Scoreboard scoreboard = entity.getScoreboard();
            ScoreboardObjective scoreboardObjective = scoreboard.getObjectiveForSlot(2);
            if (scoreboardObjective != null) {
                //render scoreboard
                matrices.translate(0.0D, -spacing, 0.0D);

                ScoreboardPlayerScore scoreboardPlayerScore = scoreboard.getPlayerScore(entity.getEntityName(), scoreboardObjective);
                super.renderLabelIfPresent(entity, (new LiteralText(Integer.toString(scoreboardPlayerScore.getScore()))).append(" ").append(scoreboardObjective.getDisplayName()), matrices, vertexConsumers, light);

                //apply line offset
                matrices.translate(0.0D, 9.0F * 1.15F * 0.025F + spacing, 0.0D);
            }
        }

        //render nameplate
        if (enabled && !(distance > 4096.0D)) {
            boolean sneaky = !entity.isSneaky();
            int i = 0;
            List<Text> textList = TextUtils.splitText(text, "\n");
            for (Text splitText : textList) {
                renderNameplate(matrices, vertexConsumers, sneaky, light, splitText, i - textList.size() + 1);
                i++;
            }
        }
        matrices.pop();
    }

    private void renderNameplate(MatrixStack matrices, VertexConsumerProvider vertexConsumers, boolean sneaky, int light, Text text, int line) {
        //matrices transformations
        matrices.push();

        matrices.multiply(this.dispatcher.getRotation());
        matrices.scale(-0.025F, -0.025F, 0.025F);
        Matrix4f matrix4f = matrices.peek().getModel();

        matrices.pop();

        int backgroundColor = (int) (MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25F) * 255.0F) << 24;

        TextRenderer textRenderer = Objects.requireNonNull(this.getFontRenderer());
        float textWidth = (float) (-textRenderer.getWidth(text) / 2);

        //render
        textRenderer.draw(text, textWidth, 10.5f * line, 553648127, false, matrix4f, vertexConsumers, sneaky, backgroundColor, light);
        if (sneaky)
            textRenderer.draw(text, textWidth, 10.5f * line, -1, false, matrix4f, vertexConsumers, false, 0, light);

    }

    @Inject(at = @At("RETURN"), method = "renderArm")
    private void postRenderArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, ModelPart arm, ModelPart sleeve, CallbackInfo ci) {
        PlayerEntityRenderer realRenderer = (PlayerEntityRenderer) (Object) this;
        PlayerEntityModel<?> model = realRenderer.getModel();
        PlayerData playerData = FiguraMod.currentData;

        if (playerData != null && playerData.model != null) {
            arm.pitch = 0;
            playerData.model.renderArm(playerData, matrices, vertexConsumers, light, arm, model, 1f);
        }

        FiguraMod.clearRenderingData();
        figura$clearAllPartCustomizations();
    }

    public void figura$applyPartCustomization(String id, ModelPart part) {
        PlayerData data = FiguraMod.currentData;

        if (data != null && data.script != null && data.script.allCustomizations != null) {
            VanillaModelPartCustomization customization = data.script.allCustomizations.get(id);

            if (customization != null) {
                ((ModelPartAccess) part).figura$setPartCustomization(customization);
                figura$customizedParts.add(part);
            }
        }
    }

    public void figura$clearAllPartCustomizations() {
        for (ModelPart part : figura$customizedParts) {
            ((ModelPartAccess) part).figura$setPartCustomization(null);
        }
        figura$customizedParts.clear();
    }

    public void figura$setupTransformsPublic(AbstractClientPlayerEntity abstractClientPlayerEntity, MatrixStack matrixStack, float f, float g, float h){
        this.setupTransforms(abstractClientPlayerEntity, matrixStack, f, g, h);
    }

    @Shadow
    @Override
    public Identifier getTexture(AbstractClientPlayerEntity entity) {
        return null;
    }

}

package mcvmcomputers.mixins;

import mcvmcomputers.client.entities.model.OrderingTabletModel;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mcvmcomputers.client.ClientMod;
import mcvmcomputers.item.ItemOrderingTablet;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;

import java.awt.*;

@Mixin(HeldItemRenderer.class)
public class HeldItemMixin {
	@Shadow
	private void renderArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, Arm arm) {}
	
	@Inject(at = @At("HEAD"), method = "renderFirstPersonItem")
	private void renderItemHead(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
		if(!item.isEmpty()) {
			if(item.getItem() instanceof ItemOrderingTablet) {
				if(ClientMod.tabletOS.orderingTabletModel == null) {
					ClientMod.tabletOS.orderingTabletModel = new OrderingTabletModel(MinecraftClient.getInstance().getEntityModelLoader().getModelPart(ClientMod.MODEL_TABLET_MODEL));
				}
				if(ClientMod.tabletOS.font == null) {
					try {
						ClientMod.tabletOS.font = Font.createFont(Font.PLAIN, MinecraftClient.getInstance().getResourceManager().getResource(new Identifier("mcvmcomputers", "font/tabletfont.ttf")).getInputStream());
					} catch (Exception ignored) {
					}
				}
				matrices.push();
				matrices.translate(0, -equipProgress*2, 0);
					matrices.push();
						matrices.translate(0, 0.1, 0.38);
						matrices.multiply(new Quaternion(-90, 0, 0, true));
						ClientMod.tabletOS.orderingTabletModel.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntityCutout(new Identifier("mcvmcomputers", "textures/entity/tablet.png"))), light, OverlayTexture.DEFAULT_UV, 0, 0, 0, 1);
						if(ClientMod.tabletOS.textureIdentifier != null) {
							matrices.push();
							matrices.scale(0.19f, 0.19f, 0.19f);
							matrices.multiply(new Quaternion(-90, 0, 0, true));
							matrices.translate(-1.65, -1.65, 7.57);
							Matrix4f matrix4f = matrices.peek().getModel();
							VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getText(ClientMod.tabletOS.textureIdentifier));
							vertexConsumer.vertex(matrix4f, 0.0F, 3.3F, -0.01F).color(255, 255, 255, 255).texture(0.0F, 1.0F).light(15728640).next();
					        vertexConsumer.vertex(matrix4f, 3.3F, 3.3F, -0.01F).color(255, 255, 255, 255).texture(1.0F, 1.0F).light(15728640).next();
					        vertexConsumer.vertex(matrix4f, 3.3F, 0.0F, -0.01F).color(255, 255, 255, 255).texture(1.0F, 0.0F).light(15728640).next();
					        vertexConsumer.vertex(matrix4f, 0.0F, 0.0F, -0.01F).color(255, 255, 255, 255).texture(0.0F, 0.0F).light(15728640).next();
					        matrices.pop();
						}
					matrices.pop();
					matrices.push();
						boolean b = hand == Hand.MAIN_HAND;
						Arm arm = b ? Arm.RIGHT : Arm.LEFT;
						matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(90.0F));
						matrices.translate(1.03, 0.20, 0);
						this.renderArm(matrices, vertexConsumers, light, arm);
					matrices.pop();
				matrices.pop();
			}
		}
		matrices.push();
	}
	
	@Inject(at = @At("TAIL"), method = "renderFirstPersonItem")
	private void renderItemTail(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
		matrices.pop();
	}
}

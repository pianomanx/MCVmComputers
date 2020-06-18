package mcvmcomputers.client.gui;

import java.awt.Color;
import java.io.File;
import java.util.Arrays;

import org.lwjgl.glfw.GLFW;
import org.virtualbox_6_1.AccessMode;
import org.virtualbox_6_1.CleanupMode;
import org.virtualbox_6_1.DeviceType;
import org.virtualbox_6_1.IMachine;
import org.virtualbox_6_1.IMedium;
import org.virtualbox_6_1.IProgress;
import org.virtualbox_6_1.ISession;
import org.virtualbox_6_1.LockType;
import org.virtualbox_6_1.MachineState;
import org.virtualbox_6_1.StorageBus;
import org.virtualbox_6_1.VBoxException;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import io.netty.buffer.Unpooled;
import mcvmcomputers.ClientMod;
import mcvmcomputers.entities.EntityPC;
import mcvmcomputers.item.ItemHarddrive;
import mcvmcomputers.item.ItemList;
import mcvmcomputers.networking.PacketList;
import mcvmcomputers.utils.MVCUtils;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation.Mode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.world.dimension.DimensionType;

public class GuiPCEditing extends Screen{
	private float introScale;
	private float panelX;
	private EntityPC pc_case;
	private boolean openCase;
	
	private static final ItemStack CASE_NO_PANEL = new ItemStack(ItemList.PC_CASE_NO_PANEL);
	private static final ItemStack CASE_ONLY_PANEL = new ItemStack(ItemList.PC_CASE_ONLY_PANEL);
	private static final ItemStack CASE_ONLY_GLASS_PANEL = new ItemStack(ItemList.PC_CASE_GLASS_PANEL);
	private static final ItemStack MOBO = new ItemStack(ItemList.ITEM_MOTHERBOARD);
	private static final ItemStack CPU = new ItemStack(ItemList.ITEM_CPU2);
	private static final ItemStack GPU = new ItemStack(ItemList.ITEM_GPU);
	private static final ItemStack RAM = new ItemStack(ItemList.ITEM_RAM1G);
	private static final ItemStack HARD_DRIVE = new ItemStack(ItemList.ITEM_HARDDRIVE);
	
	public GuiPCEditing(EntityPC pc_case) {
		super(new TranslatableText("text.pc_editor.title"));
		this.pc_case = pc_case;
	}
	
	public void renderBackgroundAndMobo() {
		this.fillGradient(0, 0, this.width, this.height, new Color(0f,0f,0f,Math.max(0.5f*introScale,0)).getRGB(), new Color(0f,0f,0f,0.5f*introScale).getRGB());
		RenderSystem.pushMatrix();
		RenderSystem.enableRescaleNormal();
	    RenderSystem.enableAlphaTest();
	    RenderSystem.defaultAlphaFunc();
	    RenderSystem.enableBlend();
	    RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
	    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
	    RenderSystem.translatef((this.width / 2), (this.height / 2)-40, 100.0F);
	    RenderSystem.rotatef(90f*introScale, 0f, -1f, 0f);
	    RenderSystem.rotatef(6f*introScale, 0, 0.1f, 0.1f);
	    RenderSystem.scalef(1.0F, -1.0F, 1.0F);
	    RenderSystem.scalef(introScale, introScale, introScale);
	    RenderSystem.scalef(230.0F, 230.0F, 230.0F);
	    renderItem(CASE_NO_PANEL);
	    if(pc_case.getMotherboardInstalled()) {
	    	RenderSystem.pushMatrix();
		    RenderSystem.rotatef(90f, 0f, 0f, -1f);
		    RenderSystem.rotatef(90f, 0f, -1f, 0f);
		    RenderSystem.translatef(0.04f, 0.2f, -0.16f);
		    RenderSystem.scalef(0.55f, 0.55f, 0.55f);
		    renderItem(MOBO);
		    RenderSystem.popMatrix();
	    }
	    if(pc_case.getCpuDividedBy() > 0) {
		    RenderSystem.pushMatrix();
		    RenderSystem.rotatef(90f, 0f, 0f, -1f);
		    RenderSystem.scalef(0.55f, 0.55f, 0.55f);
		    RenderSystem.translatef(0.23f, 0.48f, 0.13f);
	    	renderItem(CPU);
	    	RenderSystem.popMatrix();
	    }
	    if(pc_case.getGpuInstalled()) {
	    	RenderSystem.pushMatrix();
	    	RenderSystem.rotatef(90f, 0f, -1f, 0f);
	    	RenderSystem.scalef(0.55f, 0.55f, 0.55f);
	    	RenderSystem.rotatef(-90f, 1f, 0f, 0f);
	    	RenderSystem.translatef(0.33f, 0.5f, -0.565f);
	    	renderItem(GPU);
	    	RenderSystem.popMatrix();
	    }
	    if(pc_case.getGigsOfRamInSlot0() > 0) {
	    	RenderSystem.pushMatrix();
	    	RenderSystem.scalef(0.55f, 0.55f, 0.55f);
	    	RenderSystem.rotatef(90f, 0f, 0f, -1f);
	    	RenderSystem.translatef(0.07f, 0.5f, -0.203f);
	    	renderItem(RAM);
	    	RenderSystem.popMatrix();
	    }
	    if(pc_case.getGigsOfRamInSlot1() > 0) {
	    	RenderSystem.pushMatrix();
	    	RenderSystem.scalef(0.55f, 0.55f, 0.55f);
	    	RenderSystem.rotatef(90f, 0f, 0f, -1f);
	    	RenderSystem.translatef(0.07f, 0.5f, -0.33f);
	    	renderItem(RAM);
	    	RenderSystem.popMatrix();
	    }
	    if(!pc_case.getHardDriveFileName().isEmpty()) {
	    	RenderSystem.pushMatrix();
		    RenderSystem.scalef(0.55f, 0.55f, 0.55f);
		    RenderSystem.translatef(0.1f, -0.3f, -0.5f);
		    renderItem(HARD_DRIVE);
		    RenderSystem.popMatrix();
	    }
	    RenderSystem.pushMatrix();
	    RenderSystem.translatef(0, -panelX, 0);
	    if(pc_case.getGlassSidepanel()) {
	    	renderItem(CASE_ONLY_GLASS_PANEL);
	    }else{
	    	renderItem(CASE_ONLY_PANEL);
	    }
		RenderSystem.popMatrix();
		RenderSystem.disableAlphaTest();
	    RenderSystem.disableRescaleNormal();
		RenderSystem.popMatrix();
	}
	
	private void renderItem(ItemStack stack) {
		BakedModel mdll = this.minecraft.getItemRenderer().getHeldItemModel(stack, null, null);
		MatrixStack matrixStackk = new MatrixStack();
	    VertexConsumerProvider.Immediate immediatee = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
	    boolean ble = !mdll.isSideLit();
	      if (ble) {
	         DiffuseLighting.disableGuiDepthLighting();
	      }
	    this.minecraft.getItemRenderer().renderItem(stack, Mode.NONE, false, matrixStackk, immediatee, 15728640, OverlayTexture.DEFAULT_UV, mdll);
		immediatee.draw();
		RenderSystem.enableDepthTest();
	      if (ble) {
	         DiffuseLighting.enableGuiDepthLighting();
	      }
	}
	
	
	private void addMotherboard(boolean sixtyFour) {
		PacketByteBuf b = new PacketByteBuf(Unpooled.buffer());
		b.writeBoolean(sixtyFour);
		b.writeInt(this.pc_case.getEntityId());
		ClientSidePacketRegistry.INSTANCE.sendToServer(PacketList.C2S_ADD_MOBO, b);
	}
	
	private void removeMotherboard() {
		PacketByteBuf b = new PacketByteBuf(Unpooled.buffer());
		b.writeInt(this.pc_case.getEntityId());
		ClientSidePacketRegistry.INSTANCE.sendToServer(PacketList.C2S_REMOVE_MOBO, b);
	}
	
	private void addCPU(Item cpuItem, int dividedBy) {
		PacketByteBuf b = new PacketByteBuf(Unpooled.buffer());
		b.writeInt(dividedBy);
		b.writeInt(this.pc_case.getEntityId());
		ClientSidePacketRegistry.INSTANCE.sendToServer(PacketList.C2S_ADD_CPU, b);
	}
	
	private void addGPU() {
		PacketByteBuf b = new PacketByteBuf(Unpooled.buffer());
		b.writeInt(this.pc_case.getEntityId());
		ClientSidePacketRegistry.INSTANCE.sendToServer(PacketList.C2S_ADD_GPU, b);
	}
	
	private void addHardDrive(String fileName) {
		PacketByteBuf b = new PacketByteBuf(Unpooled.buffer());
		b.writeString(fileName);
		b.writeInt(this.pc_case.getEntityId());
		ClientSidePacketRegistry.INSTANCE.sendToServer(PacketList.C2S_ADD_HARD_DRIVE, b);
	}
	
	private void removeHardDrive() {
		PacketByteBuf b = new PacketByteBuf(Unpooled.buffer());
		b.writeInt(this.pc_case.getEntityId());
		ClientSidePacketRegistry.INSTANCE.sendToServer(PacketList.C2S_REMOVE_HARD_DRIVE, b);
	}
	private void removeGPU() {
		PacketByteBuf b = new PacketByteBuf(Unpooled.buffer());
		b.writeInt(this.pc_case.getEntityId());
		ClientSidePacketRegistry.INSTANCE.sendToServer(PacketList.C2S_REMOVE_GPU, b);
	}
	
	private void addRamStick(Item ramItem, int gigs) {
		PacketByteBuf b = new PacketByteBuf(Unpooled.buffer());
		b.writeInt(gigs);
		b.writeInt(this.pc_case.getEntityId());
		ClientSidePacketRegistry.INSTANCE.sendToServer(PacketList.C2S_ADD_RAM, b);
	}
	
	private void removeRamStick(int slot) {
		PacketByteBuf b = new PacketByteBuf(Unpooled.buffer());
		b.writeInt(slot);
		b.writeInt(this.pc_case.getEntityId());
		ClientSidePacketRegistry.INSTANCE.sendToServer(PacketList.C2S_REMOVE_RAM, b);
	}
	private void removeCPU() {
		PacketByteBuf b = new PacketByteBuf(Unpooled.buffer());
		b.writeInt(this.pc_case.getEntityId());
		ClientSidePacketRegistry.INSTANCE.sendToServer(PacketList.C2S_REMOVE_CPU, b);
	}
	
	@Override
	public void render(int mouseX, int mouseY, float delta) {
		if(introScale > 0.92f && openCase)
			panelX = MVCUtils.lerp(panelX, 8, delta/20f);
		if(!openCase) {
			panelX = MVCUtils.lerp(panelX, 0, delta/1.5f);
		}
		introScale = MVCUtils.lerp(introScale, 1f, delta/4f);
		this.renderBackgroundAndMobo();
		this.buttons.clear();
		this.children.clear();
		if(introScale > 0.99f) {
			if(openCase) {
				if((ClientMod.vmTurningOn || ClientMod.vmTurnedOn) && ClientMod.vmEntityID == pc_case.getEntityId()) {
					openCase = false;
				}
				this.font.draw("Put panel back on: Right Ctrl", 4, 14, -1);
				if(GLFW.glfwGetKey(minecraft.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS) {
					openCase = false;
					panelX = -panelX;
				}
				if(pc_case.getMotherboardInstalled()) {
					this.addButton(new ButtonWidget(this.width/2-70, this.height / 2 - 70, 10, 10, "x", (btn) -> this.removeMotherboard()));
					RenderSystem.disableDepthTest();
					RenderSystem.pushMatrix();
					RenderSystem.translatef(0, 0, 200);
					if(pc_case.get64Bit()) {
						this.font.draw("64-bit", this.width/2 - 66, this.height/2 - 56, -1);
					}else {
						this.font.draw("32-bit", this.width/2 - 66, this.height/2 - 56, -1);
					}
					RenderSystem.popMatrix();
					RenderSystem.enableDepthTest();
					if(pc_case.getCpuDividedBy() == 0) {
						RenderSystem.disableDepthTest();
						RenderSystem.pushMatrix();
						RenderSystem.translatef(0, 0, 200);
						this.font.draw("Add CPU", this.width/2 - 120, this.height/2 - 40, -1);
						RenderSystem.popMatrix();
						RenderSystem.enableDepthTest();
						ButtonWidget div2 = new ButtonWidget(this.width/2 - 120, this.height / 2 - 31, 70, 12, "Add 1/2 CPU", (btn) -> this.addCPU(ItemList.ITEM_CPU2, 2));
						ButtonWidget div4 = new ButtonWidget(this.width/2 - 120, this.height / 2 - 18, 70, 12, "Add 1/4 CPU", (btn) -> this.addCPU(ItemList.ITEM_CPU4, 4));
						ButtonWidget div6 = new ButtonWidget(this.width/2 - 120, this.height / 2 - 5, 70, 12, "Add 1/6 CPU", (btn) -> this.addCPU(ItemList.ITEM_CPU6, 6));
						if(!minecraft.player.inventory.contains(new ItemStack(ItemList.ITEM_CPU2))) {
							div2.active = false;
						}
						if(!minecraft.player.inventory.contains(new ItemStack(ItemList.ITEM_CPU4))) {
							div4.active = false;
						}
						if(!minecraft.player.inventory.contains(new ItemStack(ItemList.ITEM_CPU6))) {
							div6.active = false;
						}
						this.addButton(div2);
						this.addButton(div4);
						this.addButton(div6);
					}else {
						this.addButton(new ButtonWidget(this.width/2-43, this.height / 2 - 16, 10, 10, "x", (btn) -> this.removeCPU()));
						RenderSystem.disableDepthTest();
						RenderSystem.pushMatrix();
						RenderSystem.translatef(0, 0, 200);
						this.font.draw("1/" + pc_case.getCpuDividedBy(), this.width/2-25, this.height/2+2, -1);
						RenderSystem.popMatrix();
						RenderSystem.enableDepthTest();
					}
					if(!pc_case.getGpuInstalled()) {
						this.addButton(new ButtonWidget(this.width/2 - 53, this.height / 2 + 33, 48, 12, "Add GPU", (btn) -> this.addGPU()));
					}
					if(pc_case.getHardDriveFileName().isEmpty()) {
						int lastYOffset = 0;
						int xOffCount = 0;
						int lastXOffset = 0;
						int count = 0;
						RenderSystem.disableDepthTest();
						RenderSystem.pushMatrix();
						RenderSystem.translatef(0, 0, 200);
						this.font.draw("Add hard drives", this.width/2 + 20, this.height/2 + 30, -1);
						RenderSystem.popMatrix();
						RenderSystem.enableDepthTest();
						for(ItemStack is : minecraft.player.inventory.main) {
							if(is.getItem() instanceof ItemHarddrive) {
                                if(is.getTag() != null){
                                    if(is.getTag().contains("vhdfile")) {
		    							String file = is.getTag().getString("vhdfile");
                                        if(new File(ClientMod.vhdDirectory, file).exists()) {
                                            int w = Math.max(50, this.font.getStringWidth(file)+4);
                                            this.addButton(new ButtonWidget(this.width/2 + 20 + lastXOffset, this.height / 2 + 40 + lastYOffset, Math.max(50, this.font.getStringWidth(file)+4), 12, file, (btn) -> this.addHardDrive(file)));
                                            lastXOffset += w+1;
                                            xOffCount += 1;
                                            if(xOffCount >= 3) {
                                                xOffCount = 0;
                                                lastXOffset = 0;
                                                lastYOffset += 13;
                                            }
                                            count++;
                                        }
								    }
                                }
							}
						}
						if(count == 0) {
							RenderSystem.disableDepthTest();
							RenderSystem.pushMatrix();
							RenderSystem.translatef(0, 0, 200);
							this.font.draw((char) (0xfeff00a7) + "7No valid hard drives found :(", this.width/2 + 20, this.height/2 + 40, -1);
							RenderSystem.popMatrix();
							RenderSystem.enableDepthTest();
						}
					}else {
						this.addButton(new ButtonWidget(this.width/2+30, this.height / 2 + 55, 10, 10, "x", (btn) -> this.removeHardDrive()));
						RenderSystem.disableDepthTest();
						RenderSystem.pushMatrix();
						RenderSystem.translatef(0, 0, 200);
						this.font.draw(pc_case.getHardDriveFileName(), this.width/2+45, this.height/2+65, -1);
						RenderSystem.popMatrix();
						RenderSystem.enableDepthTest();
					}
					if(pc_case.getGigsOfRamInSlot0() == 0 || pc_case.getGigsOfRamInSlot1() == 0) {
						RenderSystem.disableDepthTest();
						RenderSystem.pushMatrix();
						RenderSystem.translatef(0, 0, 200);
						this.font.draw("Add RAM", this.width/2 + 50, this.height/2 - 60, -1);
						RenderSystem.popMatrix();
						RenderSystem.enableDepthTest();
						ButtonWidget oneG = new ButtonWidget(this.width/2 + 50, this.height / 2 - 51, 80, 12, "Add 1GB of RAM", (btn) -> this.addRamStick(ItemList.ITEM_RAM1G, 1));
						ButtonWidget twoG = new ButtonWidget(this.width/2 + 50, this.height / 2 - 38, 80, 12, "Add 2GB of RAM", (btn) -> this.addRamStick(ItemList.ITEM_RAM2G, 2));
						ButtonWidget fourG = new ButtonWidget(this.width/2 + 50, this.height / 2 - 25, 80, 12, "Add 4GB of RAM", (btn) -> this.addRamStick(ItemList.ITEM_RAM4G, 4));
						if(!minecraft.player.inventory.contains(new ItemStack(ItemList.ITEM_RAM1G))) {
							oneG.active = false;
						}
						if(!minecraft.player.inventory.contains(new ItemStack(ItemList.ITEM_RAM2G))) {
							twoG.active = false;
						}
						if(!minecraft.player.inventory.contains(new ItemStack(ItemList.ITEM_RAM4G))) {
							fourG.active = false;
						}
						this.addButton(oneG);
						this.addButton(twoG);
						this.addButton(fourG);
					}
					if(pc_case.getGigsOfRamInSlot0() > 0) {
						RenderSystem.disableDepthTest();
						RenderSystem.pushMatrix();
						RenderSystem.translatef(0, 0, 200);
						this.font.draw((int) pc_case.getGigsOfRamInSlot0() + " GB", this.width/2+8, this.height/2+2, -1);
						RenderSystem.popMatrix();
						RenderSystem.enableDepthTest();
						this.addButton(new ButtonWidget(this.width/2+21, this.height / 2 - 70, 10, 10, "x", (btn) -> this.removeRamStick(0)));
					}
					if(pc_case.getGigsOfRamInSlot1() > 0) {
						RenderSystem.disableDepthTest();
						RenderSystem.pushMatrix();
						RenderSystem.translatef(0, 0, 200);
						this.font.draw((int) pc_case.getGigsOfRamInSlot1() + " GB", this.width/2+40, this.height/2+2, -1);
						RenderSystem.popMatrix();
						RenderSystem.enableDepthTest();
						this.addButton(new ButtonWidget(this.width/2 + 37, this.height / 2 - 70, 10, 10, "x", (btn) -> this.removeRamStick(1)));
					}
				}else {
					ButtonWidget thirtytwo = new ButtonWidget(this.width/2 - 64, this.height / 2 - 23, 128, 14, "Add 32-bit Motherboard", (btn) -> this.addMotherboard(false));
					ButtonWidget sixtyfour = new ButtonWidget(this.width/2 - 64, this.height / 2 - 7, 128, 14, "Add 64-bit Motherboard", (btn) -> this.addMotherboard(true));
					
					if(!minecraft.player.inventory.contains(new ItemStack(ItemList.ITEM_MOTHERBOARD))) {
						thirtytwo.active = false;
					}
					if(!minecraft.player.inventory.contains(new ItemStack(ItemList.ITEM_MOTHERBOARD64))) {
						sixtyfour.active = false;
					}
					
					this.addButton(thirtytwo);
					this.addButton(sixtyfour);
				}
			}else {
				boolean turnedOn = (ClientMod.vmTurningOn && ClientMod.vmEntityID == pc_case.getEntityId()) || (ClientMod.vmTurnedOn && ClientMod.vmEntityID == pc_case.getEntityId());
				if(turnedOn) {
					this.addButton(new ButtonWidget(this.width/2 + 35, this.height / 2 - 80, 70, 12, "Turn off PC", (btn) -> this.turnOffPC(btn)));
				}else {
					this.addButton(new ButtonWidget(this.width/2 + 35, this.height / 2 - 80, 70, 12, "Turn on PC", (btn) -> this.turnOnPC(btn)));
				}
				
				if(ClientMod.vmSession != null) {
					boolean ejected = false;
					
					try {
						ejected = ClientMod.vmSession.getMachine().getMediumAttachment("IDE Controller", 0, 0).getIsEjected();
					}catch(VBoxException e) {}
					
					if(ejected && !pc_case.getIsoFileName().isEmpty()) {
						this.removeISO();
					}
				}
				
				if(pc_case.getIsoFileName().isEmpty()) {
					RenderSystem.disableDepthTest();
					RenderSystem.pushMatrix();
					RenderSystem.translatef(0, 0, 200);
					this.font.draw("Select an ISO", this.width/2 - 75, this.height/2 - 75, -1);
					RenderSystem.popMatrix();
					RenderSystem.enableDepthTest();
					int offX = 0;
					int offY = 0;
					for(File f : ClientMod.isoDirectory.listFiles()) {
						if(f.getName().endsWith(".iso")) {
							if((this.width/2 - 75 + offX) + this.font.getStringWidth(f.getName())+10 > this.width/2 + 105) {
								offX = 0;
								offY += 14;
							}
							this.addButton(new ButtonWidget(this.width/2 - 75 + offX, this.height / 2 - 62 + offY, this.font.getStringWidth(f.getName())+8, 12, f.getName(), (btn) -> insertISO(f.getName())));
							offX += this.font.getStringWidth(f.getName())+10;
						}
					}
					/*
					File f = new File(MCVmComputersMod.vb.getSystemProperties().getDefaultAdditionsISO());
					if(!f.getName().isEmpty()) {
						if((this.width/2 - 75 + offX) + this.font.getStringWidth("VirtualBox Guest Additions")+10 > this.width/2 + 105) {
							offX = 0;
							offY += 14;
						}
						this.addButton(new ButtonWidget(this.width/2 - 75 + offX, this.height / 2 - 62 + offY, this.font.getStringWidth("VirtualBox Guest Additions")+8, 12, "VirtualBox Guest Additions", (btn) -> insertISO(f)));
					}*/
				}else {
					RenderSystem.disableDepthTest();
					RenderSystem.pushMatrix();
					RenderSystem.translatef(0, 0, 200);
					this.font.draw("Inserted ISO", this.width/2 - 75, this.height/2 - 75, -1);
					this.font.draw((char) (0xfeff00a7) + "7" + pc_case.getIsoFileName(), this.width/2 - 75, this.height/2 - 65, -1);
					RenderSystem.popMatrix();
					RenderSystem.enableDepthTest();
					this.addButton(new ButtonWidget(this.width/2 - 75, this.height / 2 - 50, 50, 12, "Eject", (btn) -> removeISO()));
				}
				ButtonWidget bw = new ButtonWidget(this.width/2 - 82, this.height / 2 + 65, 60, 12, "Open Case", (btn) -> openCase = true);
				bw.active = !turnedOn;
				this.addButton(bw);
			}
		}
		RenderSystem.disableDepthTest();
		RenderSystem.translatef(0, 0, 200);
		super.render(mouseX, mouseY, delta);
		this.font.draw("Close: ESC", 4, 4, -1);
		RenderSystem.enableDepthTest();
	}
	
	private void removeISO() {
		if((ClientMod.vmTurningOn || ClientMod.vmTurnedOn) && ClientMod.vmEntityID == pc_case.getEntityId()) {
			try {
				ClientMod.vmSession.getMachine().unmountMedium("IDE Controller", 0, 0, true);
			}catch(VBoxException ex) {}
		}
		PacketByteBuf b = new PacketByteBuf(Unpooled.buffer());
		b.writeInt(this.pc_case.getEntityId());
		ClientSidePacketRegistry.INSTANCE.sendToServer(PacketList.C2S_REMOVE_ISO, b);
	}
	
	private void insertISO(String name) {
		if((ClientMod.vmTurningOn || ClientMod.vmTurnedOn) && ClientMod.vmEntityID == pc_case.getEntityId()) {
			IMedium m = ClientMod.vb.openMedium(new File(ClientMod.isoDirectory, name).getPath(), DeviceType.DVD, AccessMode.ReadOnly, true);
			ClientMod.vmSession.getMachine().mountMedium("IDE Controller", 0, 0, m, true);
		}
		PacketByteBuf b = new PacketByteBuf(Unpooled.buffer());
		b.writeString(name);
		b.writeInt(this.pc_case.getEntityId());
		ClientSidePacketRegistry.INSTANCE.sendToServer(PacketList.C2S_ADD_ISO, b);
	}
	
	public void turnOffPC(ButtonWidget wdgt) {
		//this.minecraft.openScreen(null);
		ClientMod.vmTurningOff = true;
		ClientMod.vmTurnedOn = false;
		PacketByteBuf b = new PacketByteBuf(Unpooled.buffer());
		ClientSidePacketRegistry.INSTANCE.sendToServer(PacketList.C2S_TURN_OFF_PC, b);
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(ClientMod.vmTurningOn) {}
				IProgress ip = ClientMod.vmSession.getConsole().powerDown();
				ip.waitForCompletion(-1);
				ClientMod.vmSession.unlockMachine();
				ClientMod.vmSession = null;
				ClientMod.vmTurnedOn = false;
				ClientMod.vmTurningOff = false;
				ClientMod.vmEntityID = -1;
				
			}
		}, "Turn off PC").start();
	}
	
	public void turnOnPC(ButtonWidget wdgt) {
		if(pc_case.getCpuDividedBy() > 0 && pc_case.getGpuInstalled() && pc_case.getMotherboardInstalled() && (pc_case.getGigsOfRamInSlot0() + pc_case.getGigsOfRamInSlot1()) >= 1) {
			if(!pc_case.getHardDriveFileName().isEmpty()) {
				if(!new File(ClientMod.vhdDirectory, pc_case.getHardDriveFileName()).exists()) {
					minecraft.player.sendMessage(new LiteralText("The inserted hard drive does not exist on your computer!").formatted(Formatting.RED));
					return;
				}
			}
			if(!pc_case.getIsoFileName().isEmpty()) {
				if(!new File(ClientMod.isoDirectory, pc_case.getIsoFileName()).exists()) {
					minecraft.player.sendMessage(new LiteralText("The inserted ISO does not exist on your computer!").formatted(Formatting.RED));
					return;
				}
			}
			
			if(ClientMod.vmTurningOn || ClientMod.vmTurnedOn) {
				return;
			}
			//this.minecraft.openScreen(null);
			ClientMod.vmTurningOn = true;
			ClientMod.vmEntityID = pc_case.getEntityId();
			PacketByteBuf b = new PacketByteBuf(Unpooled.buffer());
			b.writeInt(pc_case.getEntityId());
			ClientSidePacketRegistry.INSTANCE.sendToServer(PacketList.C2S_TURN_ON_PC, b);
			new Thread(new Runnable() {
				@Override
				public void run() {
					IMachine found = null;
					try {
						found = ClientMod.vb.findMachine("VmComputersVm");
					}catch(VBoxException e) {}
					
					if(found != null) {
						if(found.getState() == MachineState.Running) {
							ISession sess = ClientMod.vbManager.getSessionObject();
							found.lockMachine(sess, LockType.Shared);
							IProgress ip = sess.getConsole().powerDown();
							ip.waitForCompletion(-1);
							sess.unlockMachine();
						}
						found.unregister(CleanupMode.UnregisterOnly);
					}
					
					String OSType = "Other";
					if(pc_case.get64Bit()) {
						OSType += "_64";
					}
					IMachine machine = ClientMod.vb.createMachine("", "VmComputersVm", null, OSType, "forceOverwrite=1");
					ClientMod.vb.registerMachine(machine);
					ISession sess = ClientMod.vbManager.getSessionObject();
					machine.lockMachine(sess, LockType.Write);
					IMachine edit = sess.getMachine();
					edit.setMemorySize((long) Math.min(ClientMod.maxRam, (pc_case.getGigsOfRamInSlot0() + pc_case.getGigsOfRamInSlot1())*1024));
					edit.setCPUCount(ClientMod.vb.getHost().getProcessorCount() / pc_case.getCpuDividedBy());
					edit.getGraphicsAdapter().setAccelerate2DVideoEnabled(true);
					edit.getGraphicsAdapter().setAccelerate3DEnabled(true);
					edit.getGraphicsAdapter().setVRAMSize((long)ClientMod.videoMem);
					edit.addStorageController("SATA Controller", StorageBus.SATA);
					edit.addStorageController("IDE Controller", StorageBus.IDE);
					if(!pc_case.getHardDriveFileName().isEmpty()) {
						if(new File(ClientMod.vhdDirectory, pc_case.getHardDriveFileName()).exists()) {
							IMedium medium = ClientMod.vb.openMedium(new File(ClientMod.vhdDirectory, pc_case.getHardDriveFileName()).getPath(), DeviceType.HardDisk, AccessMode.ReadWrite, true);
							edit.attachDevice("SATA Controller", 0, 0, DeviceType.HardDisk, medium);
						}
					}
					if(!pc_case.getIsoFileName().isEmpty()) {
						if(new File(ClientMod.isoDirectory, pc_case.getIsoFileName()).exists()) {
							IMedium cd = ClientMod.vb.openMedium(new File(ClientMod.isoDirectory, pc_case.getIsoFileName()).getPath(), DeviceType.DVD, AccessMode.ReadOnly, true);
							try {
								edit.attachDevice("IDE Controller", 0, 0, DeviceType.DVD, cd);
							}catch(VBoxException ex) {}
						}else if(pc_case.getIsoFileName().equals("Additions")) {
							IMedium cd = ClientMod.vb.openMedium(new File(ClientMod.vb.getSystemProperties().getDefaultAdditionsISO()).getPath(), DeviceType.DVD, AccessMode.ReadOnly, true);
							try {
								edit.attachDevice("IDE Controller", 0, 0, DeviceType.DVD, cd);
							}catch(VBoxException ex) {}
						}
					}
					edit.saveSettings();
					sess.unlockMachine();
					machine = ClientMod.vb.findMachine("VmComputersVm");
					ClientMod.vmSession = ClientMod.vbManager.getSessionObject();
					IProgress pr = machine.launchVMProcess(ClientMod.vmSession, "headless", Arrays.asList());
					pr.waitForCompletion(-1);
					ClientMod.vmTurningOn = false;
					ClientMod.vmTurnedOn = true;
				}
			}, "Turn on PC").start();
		}
	}
	
	@Override
	public boolean isPauseScreen() {
		return false;
	}
	
}
package lightningtow.hudify.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;

@Environment(EnvType.CLIENT)
@Mixin(value = InGameHud.class)
public abstract class HudifyMixin {

//	private BlockifyHUD blockifyHUD;
//
//	@Shadow
//	@Final
//	private MinecraftClient client;
//
//	@Inject(method = "<init>(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/render/item/ItemRenderer;)V", at = @At(value = "RETURN"))
//	private void onInit(MinecraftClient client, ItemRenderer itemRenderer, CallbackInfo ci) throws IOException
//	{
//		this.blockifyHUD = new BlockifyHUD(client);
//	}
//
//	@Inject(method = "render", at = @At("HEAD"))
//	private void onDraw(MatrixStack matrixStack, float esp, CallbackInfo ci)
//	{
//		if (!MinecraftClient.getInstance().options.debugEnabled)
//			BlockifyHUD.draw(matrixStack);
//	}
}



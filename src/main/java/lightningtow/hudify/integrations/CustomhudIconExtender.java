package lightningtow.hudify.integrations;
import  com.minenash.customhud.HudElements.icon.IconElement;
import com.minenash.customhud.data.Flags;
import com.minenash.customhud.render.RenderPiece;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.NativeImage;

import net.minecraft.util.Identifier;

import static lightningtow.hudify.util.SpotifyData.g_album_art_identifier;
import static lightningtow.hudify.util.SpotifyData.g_native_image;

public class CustomhudIconExtender extends IconElement {
    private static final MinecraftClient client = MinecraftClient.getInstance();

    private static final Identifier TEXTURE_NOT_FOUND = new Identifier("textures/item/barrier.png");

//    private static final Identifier LOCATION = new Identifier("textures/item/albumart");
//    private Identifier texture;// = new Identifier("textures/item/albumart");
    private final Identifier texture;// = new Identifier("textures/item/albumart");

//    private final Identifier texture= new Identifier("textures/hudify/albumart.png");; // a filepath to a minecraft png lol
    private final int textureWidth;
    private final int textureHeight;
    private final int width;
    private final int height;
    private final int yOffset;
    private final int textWidth;

    private final boolean iconAvailable;
    private double ratio;

//    public CustomhudIconExtender(NativeImageBackedTexture image, Flags flags) {
    public CustomhudIconExtender(Identifier texture, Flags flags) {
        super(flags, 0);

        NativeImage img = g_native_image;


//        NativeImage img = null;
//        try {
//
//
//            Optional<Resource> resource = client.getResourceManager().getResource(texture);
//            if (resource.isPresent())
//                img = NativeImage.read(resource.get().getInputStream());
//        }
//        catch (IOException e) { CustomHud.LOGGER.catching(e); }


        iconAvailable = img != null;
//        this.texture = iconAvailable ? texture : TEXTURE_NOT_FOUND;

        this.texture = iconAvailable ? texture : g_album_art_identifier;

        textureWidth = iconAvailable ? img.getWidth() : 16;
        textureHeight = iconAvailable ? img.getHeight() : 16;

        height = (int) (11 * flags.scale);
        width = (int) (height * ((float)textureWidth/textureHeight));
        yOffset = referenceCorner ? 0 : (int) ((height*scale-height)/(scale*2));
        textWidth = flags.iconWidth == -1 ? width : flags.iconWidth;

//    public CustomhudIconExtender(String link, Flags flags) {
//        super(flags, 0);
//
//        ratio = 1;  //   ratio = image.getImage().getWidth()/(double)image.getImage().getHeight();
//
//
//        this.texture = texture;
////        iconAvailable = img != null;
////        texture = (texture);
////        this.texture = iconAvailable ? texture : TEXTURE_NOT_FOUND;
//
//        textureWidth = 300; textureHeight = 300;
////        textureWidth = iconAvailable ? img.getWidth() : 128;
////        textureHeight = iconAvailable ? img.getHeight() : 128;
//
//        height = (int) (11 * flags.scale);
//        width = (int) (height * ((float)textureWidth/textureHeight));
//        yOffset = referenceCorner ? 0 : (int) ((height*scale-height)/(scale*2));
//        textWidth = flags.iconWidth == -1 ? width : flags.iconWidth;

    }

    @Override
    public void render(DrawContext context, RenderPiece piece) {
        if (width == 0)
            return;
        context.getMatrices().push();
        context.getMatrices().translate(piece.x+shiftX, piece.y+shiftY-yOffset-2, 0);
        rotate(context.getMatrices(), width, height);
//        context.draw();
        if (!referenceCorner)
            context.getMatrices().translate(0, -(11*scale-11)/2F, 0);
        context.drawTexture(texture, 0, 0, width, height, 0, 0, textureWidth, textureHeight, textureWidth, textureHeight);
        context.getMatrices().pop();
    }





}

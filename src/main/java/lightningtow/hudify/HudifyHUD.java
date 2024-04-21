package lightningtow.hudify;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HudifyHUD
{
    private static MinecraftClient client;
    private static MatrixStack matrixStack;
    private static int scaledWidth;
    private static int scaledHeight;
   // private static URLImage albumImage;
    private static TextRenderer fontRenderer;
    public static String[] hudInfo;
    private static String prevImage;
    private static int progressMS;
    private static int durationMS;
    private static int prevVolume;
    private static int newVolume;
    public static boolean isHidden = false;
    public static final Logger LOGGER = LogManager.getLogger("Hudify");
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    public HudifyHUD(MinecraftClient client)
    {
        HudifyHUD.client = client;
        scaledWidth = client.getWindow().getScaledWidth();
        scaledHeight = client.getWindow().getScaledHeight();
      //  albumImage = new URLImage(300, 300);
        fontRenderer = client.textRenderer;
        int ticks = 0;
        hudInfo = new String[7];
        prevImage = "empty";
        progressMS = 0;
        durationMS = -1;
    }

    public static void draw(MatrixStack matrixStack)
    {
        matrixStack.push();
        if (hudInfo[1] == null || isHidden)
        {
            return;
        }

        double percentProgress = (double) progressMS / (double) durationMS;
        if (percentProgress < 0)
        {
            percentProgress = 0;
        }

//        HudifyHUD.matrixStack = matrixStack;
//        matrixStack.translate((lightningtow.hudify.util.HudifyConfig.anchor == lightningtow.hudify.util.HudifyConfig.Anchor.TOP_LEFT || lightningtow.hudify.util.HudifyConfig.anchor == lightningtow.hudify.util.HudifyConfig.Anchor.BOTTOM_LEFT) ? lightningtow.hudify.util.HudifyConfig.posX : scaledWidth - 185 - lightningtow.hudify.util.HudifyConfig.posX,
//                (lightningtow.hudify.util.HudifyConfig.anchor == lightningtow.hudify.util.HudifyConfig.Anchor.TOP_LEFT || lightningtow.hudify.util.HudifyConfig.anchor == lightningtow.hudify.util.HudifyConfig.Anchor.TOP_RIGHT) ? lightningtow.hudify.util.HudifyConfig.posY : scaledHeight - 55 - lightningtow.hudify.util.HudifyConfig.posY, 0);
//        matrixStack.scale((float) lightningtow.hudify.util.HudifyConfig.scale, (float) lightningtow.hudify.util.HudifyConfig.scale,1);
//        scaledWidth = client.getWindow().getScaledWidth();
//        scaledHeight = client.getWindow().getScaledHeight();
//        int textOffset = 55;
//        if ((lightningtow.hudify.util.HudifyConfig.drawCover) && hudInfo[4] != null && (!prevImage.equals(hudInfo[4]) && !hudInfo[4].equals("")))
//        {
//            LOGGER.info("Drawing new album cover.");
////            albumImage.setImage(hudInfo[4]);
//            prevImage = hudInfo[4];
//        }

//        if (hudInfo[4] != null && (lightningtow.hudify.util.HudifyConfig.drawCover))
//        {
//            drawRectangle(5, 5, 50, 50, new Color(0,0,0,150));
////            RenderUtil.drawTexture(matrixStack, albumImage, 5, 5, .15F);
//            textOffset = 0;
//        }

//        drawRectangle(0, 0, 185 - textOffset, 55, new Color(MidnightColorUtil.hex2Rgb(lightningtow.hudify.util.HudifyConfig.backgroundColor).getRed(), MidnightColorUtil.hex2Rgb(lightningtow.hudify.util.HudifyConfig.backgroundColor).getGreen(), MidnightColorUtil.hex2Rgb(lightningtow.hudify.util.HudifyConfig.backgroundColor).getBlue(), lightningtow.hudify.util.HudifyConfig.backgroundTransparency));
//        drawRectangle(60 - textOffset, 48, 180 - textOffset, 50, MidnightColorUtil.hex2Rgb(lightningtow.hudify.util.HudifyConfig.barColor).darker().darker());
//        drawRectangle(60 - textOffset, 48, (float) (60 + (120 * percentProgress)) - textOffset, 50, MidnightColorUtil.hex2Rgb(lightningtow.hudify.util.HudifyConfig.barColor));

        List<OrderedText> nameWrap = fontRenderer.wrapLines(StringVisitable.plain(hudInfo[0]), 125);
        int yOffset = 0;
//        if (nameWrap.size() > 1)
//        {
//            fontRenderer.drawWithShadow(matrixStack, nameWrap.get(0), 60 - textOffset, 5, MidnightColorUtil.hex2Rgb(lightningtow.hudify.util.HudifyConfig.titleColor).getRGB());
//            fontRenderer.drawWithShadow(matrixStack, nameWrap.get(1), 60 - textOffset, 18, MidnightColorUtil.hex2Rgb(lightningtow.hudify.util.HudifyConfig.titleColor).getRGB());
//            yOffset = 15;
//        }
//        else
//        {
//            fontRenderer.drawWithShadow(matrixStack, nameWrap.get(0), 60 - textOffset, 5, MidnightColorUtil.hex2Rgb(lightningtow.hudify.util.HudifyConfig.titleColor).getRGB());
//        }
//        matrixStack.scale(.5F, .5F, .5F);
//
//        List<OrderedText> artistWrap = fontRenderer.wrapLines(StringVisitable.plain(hudInfo[1]), 140);
//        int artistYOffset = 0;
//        if (artistWrap.size() > 1)
//        {
//            fontRenderer.drawWithShadow(matrixStack, artistWrap.get(0), 120 - (textOffset * 2), 44 + yOffset, MidnightColorUtil.hex2Rgb(lightningtow.hudify.util.HudifyConfig.artistColor).getRGB());
//            fontRenderer.drawWithShadow(matrixStack, artistWrap.get(1), 120 - (textOffset * 2), 57 + yOffset, MidnightColorUtil.hex2Rgb(lightningtow.hudify.util.HudifyConfig.artistColor).getRGB());
//            artistYOffset = 15;
//        }
//        else
//        {
//            fontRenderer.drawWithShadow(matrixStack, artistWrap.get(0), 120 - (textOffset * 2), 44 + yOffset, MidnightColorUtil.hex2Rgb(lightningtow.hudify.util.HudifyConfig.artistColor).getRGB());
//        }
//        String progressText = (progressMS / (1000 * 60)) + ":" + String.format("%02d", (progressMS / 1000 % 60));
//        String durationText = (durationMS / (1000 * 60)) + ":" + String.format("%02d ", (durationMS / 1000 % 60)) + I18n.translate("Hudify.hud.volume") + ": " + hudInfo[6];

//        fontRenderer.drawWithShadow(matrixStack, progressText, 120 - (textOffset * 2), 85, MidnightColorUtil.hex2Rgb(lightningtow.hudify.util.HudifyConfig.timeColor).getRGB());
//        fontRenderer.drawWithShadow(matrixStack, durationText, 360 - (fontRenderer.getWidth(durationText)) - (textOffset * 2), 85, MidnightColorUtil.hex2Rgb(lightningtow.hudify.util.HudifyConfig.timeColor).getRGB());
//        matrixStack.scale(2F, 2F, 2F);
        matrixStack.scale(1,1,1);
        matrixStack.pop();
    }

//    public static void drawRectangle(float x1, float y1, float x2, float y2, Color color)
//    {
//        InGameHud.fill(matrixStack, (int) (x1), (int) (y1), (int) (x2), (int) (y2), color.getRGB());
//    }

    public static void updateData(String[] data)
    {
//        try {
//            String progressText = (progressMS / (1000 * 60)) + ":" + String.format("%02d", (progressMS / 1000 % 60));
//            String durationText = (durationMS / (1000 * 60)) + ":" + String.format("%02d ", (durationMS / 1000 % 60)) + I18n.translate("Hudify.hud.volume") + ": " + hudInfo[6];
//        }
//        catch(NullPointerException e) {
//            LOGGER.error("updateData nullpointer caught");
//        }
        // why tf do i need the above?
        hudInfo = data;
        progressMS = hudInfo[2] == null ? 0 : (Integer.parseInt(hudInfo[2]) - 1000);
        durationMS = hudInfo[3] == null ? -1 : Integer.parseInt(hudInfo[3]);

        LOGGER.error("updateData" + Arrays.toString(data));

    }

    public static int getProgress()
    {
        int ms = 0;
        try {
           ms = progressMS;
        }
        catch (NullPointerException ignored) {
            LOGGER.error("getProgress nullpointer");
        };
        return ms;
//        return progressMS;
//        return (progressMS == null ? 0 : progressMS - 1000);

        // int ms = hudInfo[2] == null ? 0 : (Integer.parseInt(hudInfo[2]) - 1000);

    }

    public static int getDuration()
    {
//        return progressMS;
        int ms = 1;
        try {
            ms = durationMS;
        }
        catch (NullPointerException ignored) {
            LOGGER.error("getDuration nullpointer");
        };
        return ms;
        // int ms = hudInfo[3] == null ? -1 : Integer.parseInt(hudInfo[3]);
    }

    public static void setProgress(int progress)
    {
        progressMS = progress;
    }

    public static void setDuration(int duration)
    {
        durationMS = duration;
    }

//    public static void increaseVolume()
//    {
//        EXECUTOR_SERVICE.execute(() -> {
//            prevVolume = Integer.parseInt(hudInfo[6]);
//            LOGGER.info("Prev volume: " + prevVolume);
//            newVolume = prevVolume + lightningtow.hudify.util.HudifyConfig.volumeStep;
//            if (prevVolume == 100) {
//                return;
//            } else if (newVolume > 100) {
//                newVolume = 100;
//            }
//            SpotifyUtil.putRequest("volume?volume_percent=" + newVolume);
//            LOGGER.info("Set new volume: " + newVolume);
//            hudInfo[6] = String.valueOf(newVolume);
//        });
//    }
//    public static void decreaseVolume()
//    {
//        EXECUTOR_SERVICE.execute(() -> {
//            prevVolume = Integer.parseInt(hudInfo[6]);
//            LOGGER.info("Prev volume: " + prevVolume);
//            newVolume = prevVolume - lightningtow.hudify.util.HudifyConfig.volumeStep;
//            if (prevVolume == 0)
//            {
//                return;
//            }
//            else if (newVolume < 0)
//            {
//                newVolume = 0;
//            }
//            SpotifyUtil.putRequest("volume?volume_percent=" + newVolume);
//            LOGGER.info("Set new volume: " + newVolume);
//            hudInfo[6] = String.valueOf(newVolume);
//        });
//    }
}
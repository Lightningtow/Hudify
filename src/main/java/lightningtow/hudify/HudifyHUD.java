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

        //LOGGER.error("HudifyHud.updateData" + Arrays.toString(data));

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

    public static void setProgress(int progress) {
        progressMS = progress;
    }

    public static void setDuration(int duration) {
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
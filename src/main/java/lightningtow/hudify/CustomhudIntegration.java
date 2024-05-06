package lightningtow.hudify;

import com.minenash.customhud.ComplexData;
import com.minenash.customhud.HudElements.HudElement;
import com.minenash.customhud.HudElements.StringElement;
import com.minenash.customhud.HudElements.supplier.BooleanSupplierElement;
import com.minenash.customhud.HudElements.supplier.IntegerSuppliers;
import com.minenash.customhud.HudElements.supplier.StringSupplierElement;
import lightningtow.hudify.util.SpotifyUtil;
import net.fabricmc.api.ClientModInitializer;
import com.minenash.customhud.HudElements.supplier.NumberSupplierElement;
import com.minenash.customhud.mod_compat.CustomHudRegistry;
import net.minecraft.world.Heightmap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.minenash.customhud.mod_compat.CustomHudRegistry.registerElement;

public class CustomhudIntegration implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger(HudifyMain.MOD_ID);

    @Override
    public void onInitializeClient() {
//        LOGGER.info("running CHIntegration.onInitializeClient()");
        LOGGER.info("integrating with CustomHud");

//        String[] info = SpotifyUtil.getPlaybackInfo();

//    public static void registerCompat() {
        // 0 name, 1 artists, 2 progress, 3 duration, 4 album?, 5 external_url?, 6, volume percent
        // 5 url is like https://open.spotify.com/track/536ZTi6wWJQ2gYXkXnJwVX?si=4e244a84c9884ae4
     //   registerElement("spotify_track", (_str) -> new StringSupplierElement(() -> HudifyHUD.hudInfo[0]));

    //    registerElement("spotify_ready", (_str) -> new BooleanSupplierElement(() -> Objects.equals(HudifyHUD.hudInfo[0], "-")));

//       // HudElement track = new StringSupplierElement(HudifyHUD.hudInfo[0]);
//        registerElement("name1", (_str) -> VERSION);
//        registerElement("name2", (_str) -> VERSION);

//        Supplier<String> tracksupplier = () -> HudifyHUD.hudInfo[0];
//        track  = (str) -> new StringSupplierElement(HudifyHUD.hudInfo[0]);

//        Supplier<String> track =   HudifyHUD.hudInfo[0];
        StringSupplierElement track = new StringSupplierElement(() -> HudifyMain.hudInfo[0]);
        CustomHudRegistry.registerElement("spotify_track", (_str) -> track);
        CustomHudRegistry.registerElement("sp_track",  (_str) ->  track);
//        CustomHudRegistry.registerElement("spotify_track", track);
//        CustomHudRegistry.registerElement("sp_track", track);

        StringSupplierElement artists = new StringSupplierElement(() -> HudifyMain.hudInfo[1]);
        CustomHudRegistry.registerElement("spotify_artists", (_str) -> artists);
        CustomHudRegistry.registerElement("sp_artists",  (_str) ->  artists);

        StringSupplierElement context_type = new StringSupplierElement(() -> HudifyMain.hudInfo[7]);
        CustomHudRegistry.registerElement("sp_context_type", (_str) -> context_type);

       // registerElement("spotify_artist", (_str) -> new StringSupplierElement(() -> HudifyHUD.hudInfo[1]));
        StringSupplierElement progress = new StringSupplierElement(() ->
                (HudifyMain.getProgress() / 60) + ":" + String.format("%02d", HudifyMain.getProgress() % 60));
        CustomHudRegistry.registerElement("spotify_progress", (_str) -> progress);
        CustomHudRegistry.registerElement("sp_prog",  (_str) ->  progress);

        StringSupplierElement duration = new StringSupplierElement(() ->
                (HudifyMain.getDuration() / 60) + ":" + String.format("%02d", HudifyMain.getDuration() % 60));
        CustomHudRegistry.registerElement("spotify_duration", (_str) -> duration);
        CustomHudRegistry.registerElement("sp_dur",  (_str) ->  duration);

       // registerElement("spotify_progress_ms", (_str) -> new StringSupplierElement(() -> (HudifyHUD.getProgress() / (1000 * 60)) + ":" + String.format("%02d", HudifyHUD.getProgress() / 1000 % 60)));
       // registerElement("spotify_duration_ms", (_str) -> new StringSupplierElement(() -> (HudifyHUD.getDuration() / (1000 * 60)) + ":" + String.format("%02d", HudifyHUD.getDuration() / 1000 % 60)));



        registerElement("spotify_url", (_str) -> new StringSupplierElement(() -> HudifyMain.hudInfo[5]));
        registerElement("spotify_volume", (_str) -> new StringSupplierElement(() -> HudifyMain.hudInfo[6]));

        ////        String progressText = (progressMS / (1000 * 60)) + ":" + String.format("%02d", (progressMS / 1000 % 60));
        // (HudifyHUD.hudInfo[2] / (1000 * 60)) + ":" + String.format("%02d", (progressMS / 1000 % 60))
        // approximately 180 calls per minute without throwing 429, 3 calls per second

       // registerElement("spotify_progress_ms", (str) -> new NumberSupplierElement(() ->  toInt(info[2]), 1));
//	response code 429 -> Too Many Requests - Rate limiting has been applied.
    }

}

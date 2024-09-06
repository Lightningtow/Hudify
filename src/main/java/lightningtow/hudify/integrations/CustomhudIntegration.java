package lightningtow.hudify.integrations;
import com.minenash.customhud.HudElements.supplier.BooleanSupplierElement;
import com.minenash.customhud.HudElements.supplier.NumberSupplierElement;
import com.minenash.customhud.HudElements.supplier.SpecialSupplierElement;
import com.minenash.customhud.HudElements.supplier.StringSupplierElement;

import static lightningtow.hudify.HudifyMain.LogThis;
import static lightningtow.hudify.util.SpotifyData.*;

/*
TO SWITCH BETWEEN CUSTOMHUD VERSIONS:

swap the dependencies in build.gradle
swap customhud_api_label in gradle.properties
comment the appropriate section below

if switching to v4: comment the entire CustomhudBoolSupplier.java file
if switching to v3: uncomment the entire CustomhudBoolSupplier.java file

 */
// MAIN BRANCH SHOULD NOT EVER BE USING 4.0 STUFF

/* 4.0 */
import com.minenash.customhud.data.Flags;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.Level;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static com.minenash.customhud.data.Flags.wrap;
import static com.minenash.customhud.registry.CustomHudRegistry.registerElement;
public class CustomhudIntegration {
        public static void initCustomhud() {
            UpdateMaps();

            registerElement("sp_album_art", (f, c) -> new CustomhudIconExtender(sp_album_art_identifier, f)  );


            for ( String key : stringmap.keySet() ) { // f, c == flags, context
                registerElement(key, (f, c) ->  wrap(new StringSupplierElement(() -> stringmap.get(key).isEmpty() ? null : stringmap.get(key)), f)); }

            for ( String key : boolmap.keySet() ) {
                registerElement(key, (f, c) ->  wrap(new BooleanSupplierElement(() -> boolmap.get(key)), f)); }
            for ( String key : intmap.keySet() ) {
                registerElement(key, (f, c) -> new NumberSupplierElement(() -> intmap.get(key), new Flags())); }

            for ( String key : specialmap.keySet() ) {
                registerElement(key, (f, c) -> new SpecialSupplierElement(SpecialSupplierElement.of(
                        () -> specialmap.get(key).getA(),
                        () -> specialmap.get(key).getB(),
                        () -> specialmap.get(key).getC()
                )));
            }
        }
}

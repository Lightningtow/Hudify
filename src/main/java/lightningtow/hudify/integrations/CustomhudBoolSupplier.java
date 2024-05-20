package lightningtow.hudify.integrations;

import com.minenash.customhud.HudElements.HudElement;

import java.util.function.Supplier;

public class CustomhudBoolSupplier implements HudElement {
    private final Supplier<Boolean> supplier;

    public CustomhudBoolSupplier(Supplier<Boolean> supplier) {
        this.supplier = supplier;
    }

    @Override
    public String getString() {
        return sanitize(supplier, false) ? "true" : "false";
    }

    @Override
    public Number getNumber() {
        return sanitize(supplier, false) ? 1 : 0;
    }

    @Override
    public boolean getBoolean() {
        return sanitize(supplier, false);
    }
}

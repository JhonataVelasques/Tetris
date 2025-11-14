package com.tetris.view;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Gerencia o tema claro/escuro da aplicação.
 * Uso simples via propriedades JavaFX para ligação reativa.
 */
public final class Theme {

    private static final BooleanProperty dark = new SimpleBooleanProperty(true);

    private Theme() {}

    public static BooleanProperty darkProperty() {
        return dark;
    }

    public static boolean isDark() {
        return dark.get();
    }

    public static void setDark(boolean value) {
        dark.set(value);
    }

    public static void toggle() {
        dark.set(!dark.get());
    }
}

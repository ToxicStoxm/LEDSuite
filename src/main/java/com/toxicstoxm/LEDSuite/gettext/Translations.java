package com.toxicstoxm.LEDSuite.gettext;

import io.github.jwharm.javagi.interop.Interop;
import org.gnome.glib.GLib;
import org.jetbrains.annotations.NotNull;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

/**
 * A utility class for handling string translations using the GNU `gettext` system.
 * This class provides methods to initialize a translation domain and retrieve translations
 * for specific keys based on the current locale or other specified parameters.
 * @since 1.0.0
 */
public class Translations {

    /**
     * Initializes the translation domain to be used for all following translation operations.
     * <p>
     * If this method is not called, the default translation domain specified by the C function
     * {@code textdomain()} will be used for translations.
     * </p>
     *
     * @param translationDomain the name of the translation domain (usually the name of the application or module).
     */
    public static void init(String translationDomain, String directory) throws AssertionError {
        if (directory != null) {
            try (var _arena = Arena.ofConfined()) {
                try {
                    MethodHandles.bindtextdomain.invokeExact(
                            (MemorySegment) Interop.allocateNativeString(translationDomain, _arena),
                            (MemorySegment) Interop.allocateNativeString(directory, _arena));
                } catch (Throwable _err) {
                    throw new AssertionError(_err);
                }
            }
        }
        if (translationDomain != null) {
            try (var _arena = Arena.ofConfined()) {
                try {
                    MethodHandles.textdomain.invokeExact(
                            (MemorySegment) Interop.allocateNativeString(translationDomain, _arena)
                    );
                } catch (Throwable _err) {
                    throw new AssertionError(_err);
                }
            }
        }
    }

    /**
     * Checks if the provided {@code key} can be translated using the current locale
     * and the specified translation domain.
     *
     * @param key the message key to check for translatability.
     * @return {@code true} if the message can be translated; {@code false} otherwise.
     */
    public static boolean isTranslatable(String key) {
        return !getText(key).equals(key);
    }

    /**
     * Translates the given message key to the current locale using the specified translation domain.
     * <p>
     * This method fetches the singular version of the translation. To handle plural forms, use
     * {@link #getTextPlural(String, int)}.
     * </p>
     *
     * @param key the message key to translate.
     * @return the translated string if available; otherwise, the original key.
     */
    public static @NotNull String getText(String key) {
        return getTextPlural(key, 1);
    }

    /**
     * Translates the given message key to the current locale using the specified translation domain,
     * considering pluralization based on the given quantity.
     * <p>
     * If the quantity is singular (e.g., {@code n = 1}), the singular translation is returned.
     * For quantities greater than one, the plural translation is returned.
     * </p>
     *
     * @param key the base message key to translate.
     * @param n the quantity determining singular or plural translation.
     * @return the appropriate translation for the given quantity.
     */
    public static @NotNull String getTextPlural(@NotNull String key, int n) {
        return GLib.dngettext(null, key, key + "_plural", n);
    }

    /**
     * Translates the given message key to a specified language category using the translation domain.
     * <p>
     * This method allows overriding the current locale for translation by specifying a category,
     * such as a specific language or regional variant.
     * </p>
     *
     * @param key the message key to translate.
     * @param category the category or language code to use for translation (e.g., {@code LC_MESSAGES}).
     * @return the translated string for the specified language category.
     */
    public static @NotNull String getTextOtherLanguage(@NotNull String key, int category) {
        return GLib.dcgettext(null, key, category);
    }

    /**
     * Method handles for handling downcalls to native methods.
     */
    private static final class MethodHandles {
        static final MethodHandle bindtextdomain = Interop.downcallHandle("bindtextdomain",
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS), false);
        static final MethodHandle textdomain = Interop.downcallHandle("textdomain",
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS), false);
    }
}
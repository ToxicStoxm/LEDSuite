package com.toxicstoxm.LEDSuite.gettext;

import com.toxicstoxm.YAJL.Logger;
import io.github.jwharm.javagi.interop.Interop;
import org.gnome.glib.GLib;
import org.jetbrains.annotations.NotNull;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

/**
 * Utility class for handling string translations using the GNU `gettext` system.
 * This class provides methods to initialize the translation domain and retrieve translations
 * for specific keys based on the current locale or other specified parameters.
 * <p>
 * It provides support for:
 * <ul>
 *     <li>Initializing translation domains.</li>
 *     <li>Fetching translations for message keys, including pluralization support.</li>
 *     <li>Translating strings to other languages using specific categories.</li>
 * </ul>
 *
 * <p>Internally, this class relies on the `bindtextdomain()`, `textdomain()`, and `dngettext()` functions from the GNU `gettext` system,
 * and it uses Java's `foreign` API to interoperate with native code.</p>
 *
 * @since 1.0.0
 */
public class Translations {

    private static final Logger logger = Logger.autoConfigureLogger();

    // Default translation domain is the application name or module
    private static final String defaultTranslationDomain = "LED_Suite";

    /**
     * Initializes the translation domain to be used for all following translation operations.
     * <p>
     * If this method is not called, the default translation domain specified by the C function
     * {@code textdomain()} will be used for translations. The directory path indicates where the translation files are stored.
     * </p>
     * <p>
     * This method should be called at the beginning of the application lifecycle to set up the translation system correctly.
     * </p>
     *
     * @param translationDomain the name of the translation domain (usually the name of the application or module).
     * @param directory the path to the directory containing translation files (e.g., `.mo` files).
     * @throws AssertionError if there is an issue initializing the domain or the path.
     */
    public static void init(String translationDomain, String directory) throws AssertionError {
        logger.verbose("Initializing instance -> TranslationDomain: '{}' Directory: '{}'", translationDomain, directory);
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
     * <p>
     * This method compares the provided key with its translation. If the translation matches
     * the original key, it indicates the key is not translatable.
     * </p>
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
     * This method fetches the singular version of the translation. For handling plural forms,
     * use {@link #getTextPlural(String, int)}.
     * </p>
     *
     * @param key the message key to translate.
     * @param vars optional variables to replace placeholders in the translated string.
     * @return the translated string if available; otherwise, the original key.
     */
    public static @NotNull String getText(String key, String... vars) {
        String translatedString = getTextPlural(key, 1);  // Fetch the singular translation
        if (vars != null) {
            for (String s : vars) {
                translatedString = translatedString.replaceFirst("\\$", s);  // Replace placeholders
            }
        }
        return translatedString;
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
     * Method handles for handling downcalls to native methods in the GNU gettext system.
     */
    private static final class MethodHandles {
        // Handle for calling 'bindtextdomain' to set the directory of translation files
        static final MethodHandle bindtextdomain = Interop.downcallHandle("bindtextdomain",
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS), false);

        // Handle for calling 'textdomain' to set the current translation domain
        static final MethodHandle textdomain = Interop.downcallHandle("textdomain",
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS), false);
    }
}

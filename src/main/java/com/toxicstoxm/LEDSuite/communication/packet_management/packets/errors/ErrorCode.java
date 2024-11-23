package com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors;

import com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors.menu_error.MenuErrorPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors.server_error.ServerErrorPacket;
import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * <p>Defines a set of error codes for various types of errors that can occur during communication with the client or server.</p>
 * <p>These error codes are used in the {@link ServerErrorPacket} and {@link MenuErrorPacket} to identify specific issues that
 * arise during processing, such as YAML parsing errors, file errors, or widget-related issues.</p>
 * <p>Each error code has an associated integer value that helps in identifying the specific problem encountered.</p>
 *
 * @since 1.0.0
 */
@Getter
public enum ErrorCode {
    /**
     * The error code is undefined or unknown.
     */
    Undefined(0),

    /**
     * The request type could not be parsed correctly.
     */
    FailedToParseRequestType(1),

    /**
     * A message could not be received from the client.
     */
    FailedToReceiveMessage(2),

    /**
     * The checksum of a file was found to be invalid.
     */
    ChecksumOfFileIsInvalid(3),

    /**
     * The feature or functionality is not implemented yet.
     */
    NotImplementedYet(4),

    /**
     * There was an error writing data to the filesystem.
     */
    FailedToWriteToFilesystem(5),

    /**
     * A widget is missing a required type specification.
     */
    WidgetMissingType(6),

    /**
     * The specified filename is invalid.
     */
    InvalidFilenameSpecified(7),

    /**
     * The YAML file could not be parsed correctly.
     */
    FailedToParseYAML(8),

    /**
     * A required menu content key was missing from the configuration.
     */
    MenuContentKeyMissing(9),

    /**
     * A group section in the configuration is either empty or missing.
     */
    GroupSectionEmptyOrMissing(10),

    /**
     * The top-level widget is not of type 'group' as expected.
     */
    TopLevelWidgetIsNotGroup(11),

    /**
     * A required group content key was missing from the configuration.
     */
    GroupContentKeyMissing(12),

    /**
     * A group content section is either empty or missing.
     */
    GroupContentSectionEmptyOrMissing(13),

    /**
     * A widget section in the configuration is either empty or missing.
     */
    WidgetSectionEmptyOrMissing(14),

    /**
     * A widget has an invalid or unknown type.
     */
    WidgetInvalidOrUnknownType(15),

    /**
     * A generic client-side error has occurred.
     */
    GenericClientError(16),

    /**
     * The file state is invalid.
     */
    InvalidFileState(17),

    /**
     * The status update contains an invalid 'animations' section.
     */
    StatusUpdateInvalidAnimationsSection(18),

    /**
     * A required key was missing from the configuration.
     */
    RequiredKeyIsMissing(19),

    /**
     * A combo row is missing content, which is invalid.
     */
    ComboRowWithoutContent(20),

    /**
     * An expander row is missing content, which is invalid.
     */
    ExpanderRowWithoutContent(21),

    /**
     * An expander widget is placed inside another expander, which is invalid.
     */
    ExpanderInExpander(22),

    /**
     * The group header suffix widget section is invalid.
     */
    GroupHeaderSuffixWidgetSectionInvalid(23),

    /**
     * The group header suffix widget type is invalid.
     */
    GroupHeaderSuffixWidgetInvalidType(24);

    private final int code;

    ErrorCode(int code) {
        this.code = code;
    }

    /**
     * Converts an integer value into the corresponding {@link ErrorCode}.
     * <p>If the integer does not correspond to any defined error code, {@link ErrorCode#Undefined} is returned as a fallback.</p>
     *
     * @param code The integer value representing the error code.
     * @return The corresponding {@link ErrorCode}, or {@link ErrorCode#Undefined} if the integer is not associated with a known error code.
     */
    @Contract(pure = true)
    public static @NotNull ErrorCode fromInt(int code) {
        for (ErrorCode errorCode : ErrorCode.values()) {
            if (errorCode.code == code) {
                return errorCode;
            }
        }
        return ErrorCode.Undefined;  // Fallback in case of an invalid error code.
    }
}

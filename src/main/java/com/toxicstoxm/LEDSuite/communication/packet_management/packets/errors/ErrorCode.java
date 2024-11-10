package com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors;

import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@Getter
public enum ErrorCode {
    Undefined(0),
    FailedToParseRequestType(1),
    FailedToReceiveMessage(2),
    ChecksumOfFileIsInvalid(3),
    NotImplementedYet(4),
    FailedToWriteToFilesystem(5),
    WidgetMissingType(6),
    InvalidFilenameSpecified(7),
    FailedToParseYAML(8),
    MenuContentKeyMissing(9),
    GroupSectionEmptyOrMissing(10),
    TopLevelWidgetIsNotGroup(11),
    GroupContentKeyMissing(12),
    GroupContentSectionEmptyOrMissing(13),
    WidgetSectionEmptyOrMissing(14),
    WidgetInvalidOrUnknownType(15),
    GenericClientError(16),
    InvalidFileState(17),
    StatusUpdateInvalidAnimationsSection(18),
    RequiredKeyIsMissing(19),
    ComboRowWithoutContent(20),
    ExpanderRowWithoutContent(21),
    ExpanderInExpander(22),
    GroupHeaderSuffixWidgetSectionInvalid(23),
    GroupHeaderSuffixWidgetInvalidType(24);

    private final int code;

    ErrorCode(int code) {
        this.code = code;
    }

    /**
     * Gets the {@link ErrorCode} associated with the specified integer
     *
     * @param code error code
     * @return {@link ErrorCode} associated with the specified inteǵer,
     * or {@link ErrorCode#Undefined} if the specified integer is not associated with an enum entry
     */
    @Contract(pure = true)
    public static @NotNull ErrorCode fromInt(int code) {
        for (ErrorCode errorCode : ErrorCode.values()) {
            if (errorCode.code == code) {
                return errorCode;
            }
        }

        return ErrorCode.Undefined;
    }
}

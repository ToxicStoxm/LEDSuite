package com.toxicstoxm.LEDSuite.formatting;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StringFormatterTest {

    @ParameterizedTest
    @CsvSource({
            "'/home/toxicstoxm/my-files/documents from July/MyTestFile.jar', 'MyTestFile.jar'",
            "'C:/Users/Example/Documents/Test.docx', 'Test.docx'",
            "'relative/path/to/file.txt', 'file.txt'",
            "'singlefile', 'singlefile'"
    })
    void testGetFileNameFromPath(String path, String expectedFileName) {
        assertEquals(expectedFileName, StringFormatter.getFileNameFromPath(path));
    }

    @ParameterizedTest
    @CsvSource({
            "'/home/toxicstoxm/my-files/do%cuments from July/MyTestFile.jar', '%', 'cuments from July/MyTestFile.jar'",
            "'/folder/anotherfolder/file%name.txt', '%', 'name.txt'",
            "'/something_special#delimiter.txt', '#', 'delimiter.txt'"
    })
    void testGetFileNameFromPathWithDelimiter(String path, String delimiter, String expectedFileName) {
        assertEquals(expectedFileName, StringFormatter.getFileNameFromPath(path, delimiter));
    }


    @Test
    void testGetClassName() {
        assertEquals("StringFormatterTest", StringFormatter.getClassName(this.getClass()));
        assertEquals("Integer", StringFormatter.getClassName(Integer.class));
        assertEquals("String", StringFormatter.getClassName(String.class));
    }

    @Test
    void testFormatDateTimeDefault() {
        Date fixedDate = new Date(0L); // January 1, 1970, 00:00:00 GMT
        String expected = new SimpleDateFormat("dd:MM:yy hh:mm:ss").format(fixedDate);
        assertEquals(expected, StringFormatter.formatDateTime(fixedDate));
    }

    @Test
    void testFormatDateTimeCustomFormat() {
        Date fixedDate = new Date(0L); // Always the same for stable tests

        assertEquals(new SimpleDateFormat("DD:->mm:->YY HH:->MM:->SS").format(fixedDate),
                StringFormatter.formatDateTime(fixedDate, "DD:->mm:->YY HH:->MM:->SS"));

        assertEquals(new SimpleDateFormat("hh.mm.ss").format(fixedDate),
                StringFormatter.formatDateTime(fixedDate, "hh.mm.ss"));

        assertEquals(new SimpleDateFormat("dd.MM.yy_hh:mm:ss").format(fixedDate),
                StringFormatter.formatDateTime(fixedDate, "dd.MM.yy_hh:mm:ss"));
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0s",
            "45000, 45s",
            "120000, 2min",
            "90000, 1min 30s",
            "3600000, 1h",
            "8100000, 2h 15min",
            "3661000, 1h 1min 1s",
            "500, 0s"
    })
    void testFormatDuration(long millis, String expected) {
        assertEquals(expected, StringFormatter.formatDuration(millis));
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0.00 Bps",
            "512, 512.00 Bps",
            "1023, 1023.00 Bps",
            "1024, 1.00 KBps",
            "1536, 1.50 KBps",
            "1048576, 1.00 MBps",
            "1073741824, 1.00 GBps",
            "1099511627776, 1.00 TBps",
            "1125899906842624, 1024.00 TBps",
            "-512, -512.00 Bps"
    })
    void testFormatSpeed(long bytesPerSecond, String expected) {
        assertEquals(expected, StringFormatter.formatSpeed(bytesPerSecond));
    }
}
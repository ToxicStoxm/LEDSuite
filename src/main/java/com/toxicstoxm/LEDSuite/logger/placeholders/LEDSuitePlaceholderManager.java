package com.toxicstoxm.LEDSuite.logger.placeholders;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class LEDSuitePlaceholderManager implements PlaceholderManager {
    private final HashMap<Placeholder, PlaceholderReplacer> placeholders = new HashMap<>();

    public LEDSuitePlaceholderManager() {

    }

    public LEDSuitePlaceholderManager(boolean withDefault) {
        if (withDefault) {
            this.registerPlaceholder(
                    new Placeholder() {
                        @Override
                        public String getText() {
                            return "TIME";
                        }

                        @Override
                        public char getRegex() {
                            return '%';
                        }
                    },
                    (stringWithPlaceHolders, placeholder) -> {
                        SimpleDateFormat simpleFormat = new SimpleDateFormat("HH:mm:ss");
                        return stringWithPlaceHolders.replace(placeholder.getPlaceholder(), simpleFormat.format(new Date()));
                    }
            );
            this.registerPlaceholder(
                    new Placeholder() {
                        @Override
                        public String getText() {
                            return "DATE";
                        }

                        @Override
                        public char getRegex() {
                            return '%';
                        }
                    },
                    (stringWithPlaceHolders, placeholder) -> {
                        SimpleDateFormat simpleFormat = new SimpleDateFormat("yy.MM.dd");
                        return stringWithPlaceHolders.replace(placeholder.getPlaceholder(), simpleFormat.format(new Date()));
                    }
            );
            this.registerPlaceholder(
                    new Placeholder() {
                        @Override
                        public String getText() {
                            return "HOURS";
                        }

                        @Override
                        public char getRegex() {
                            return '%';
                        }
                    },
                    (stringWithPlaceHolders, placeholder) -> {
                        SimpleDateFormat simpleFormat = new SimpleDateFormat("HH");
                        return stringWithPlaceHolders.replace(placeholder.getPlaceholder(), simpleFormat.format(new Date()));
                    }
            );
            this.registerPlaceholder(
                    new Placeholder() {
                        @Override
                        public String getText() {
                            return "MINUTES";
                        }

                        @Override
                        public char getRegex() {
                            return '%';
                        }
                    },
                    (stringWithPlaceHolders, placeholder) -> {
                        SimpleDateFormat simpleFormat = new SimpleDateFormat("mm");
                        return stringWithPlaceHolders.replace(placeholder.getPlaceholder(), simpleFormat.format(new Date()));
                    }
            );
            this.registerPlaceholder(
                    new Placeholder() {
                        @Override
                        public String getText() {
                            return "SECONDS";
                        }

                        @Override
                        public char getRegex() {
                            return '%';
                        }
                    },
                    (stringWithPlaceHolders, placeholder) -> {
                        SimpleDateFormat simpleFormat = new SimpleDateFormat("ss");
                        return stringWithPlaceHolders.replace(placeholder.getPlaceholder(), simpleFormat.format(new Date()));
                    }
            );
            this.registerPlaceholder(
                    new Placeholder() {
                        @Override
                        public String getText() {
                            return "YEARS";
                        }

                        @Override
                        public char getRegex() {
                            return '%';
                        }
                    },
                    (stringWithPlaceHolders, placeholder) -> {
                        SimpleDateFormat simpleFormat = new SimpleDateFormat("yy");
                        return stringWithPlaceHolders.replace(placeholder.getPlaceholder(), simpleFormat.format(new Date()));
                    }
            );
            this.registerPlaceholder(
                    new Placeholder() {
                        @Override
                        public String getText() {
                            return "MONTHS";
                        }

                        @Override
                        public char getRegex() {
                            return '%';
                        }
                    },
                    (stringWithPlaceHolders, placeholder) -> {
                        SimpleDateFormat simpleFormat = new SimpleDateFormat("MM");
                        return stringWithPlaceHolders.replace(placeholder.getPlaceholder(), simpleFormat.format(new Date()));
                    }
            );
            this.registerPlaceholder(
                    new Placeholder() {
                        @Override
                        public String getText() {
                            return "DAYS";
                        }

                        @Override
                        public char getRegex() {
                            return '%';
                        }
                    },
                    (stringWithPlaceHolders, placeholder) -> {
                        SimpleDateFormat simpleFormat = new SimpleDateFormat("dd");
                        return stringWithPlaceHolders.replace(placeholder.getPlaceholder(), simpleFormat.format(new Date()));
                    }
            );
            this.registerPlaceholder(
                    new Placeholder() {
                        @Override
                        public String getText() {
                            return "CUSTOM_DATE_TIME*";
                        }

                        @Override
                        public char getRegex() {
                            return '%';
                        }
                    },
                    (stringWithPlaceHolders, placeholder) -> {
                        String placeholderText = placeholder.getText().replace("*", "");
                        String prefixCalc = stringWithPlaceHolders.substring(
                                stringWithPlaceHolders.indexOf(
                                        placeholderText
                                ) - 1
                        );
                        String suffixCalc = prefixCalc.substring(
                                1,
                                prefixCalc.indexOf(
                                        placeholder.getRegex(),
                                        1
                                )
                        );
                        SimpleDateFormat simpleFormat = new SimpleDateFormat(
                                suffixCalc.replace(
                                        String.valueOf(placeholder.getRegex()),
                                        ""
                                ).replace(
                                        placeholderText,
                                        ""
                                ).strip());
                        return stringWithPlaceHolders.replace(placeholder.getRegex() + suffixCalc + placeholder.getRegex(), (simpleFormat.format(new Date())));
                    }
            );
        }
    }

    @Override
    public void registerPlaceholder(Placeholder placeholder, PlaceholderReplacer replacer) {
        placeholders.put(placeholder, replacer);
    }

    @Override
    public String processPlaceholders(String stringWithPlaceholders) {
        AtomicReference<String> result = new AtomicReference<>(stringWithPlaceholders);
        placeholders.forEach((placeholder, replacer) -> result.set(
                contains(result.get(), placeholder) ?
                        replacer.onPlaceholderRequest(
                                result.get(),
                                placeholder
                        ) :
                        result.get()
                )
        );
        return result.get();
    }

    private boolean contains(String base, Placeholder placeholder) {
        String placeholderString = placeholder.getPlaceholder();
        if (placeholderString.contains("*")) placeholderString = placeholder.getText().replace("*", "");
        return base.contains(placeholderString);
    }

    public static LEDSuitePlaceholderManager withDefault() {
        return new LEDSuitePlaceholderManager(true);
    }
}

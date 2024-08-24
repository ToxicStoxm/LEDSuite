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
                    _ -> {
                        SimpleDateFormat simpleFormat = new SimpleDateFormat("HH:mm:ss");
                        return simpleFormat.format(new Date());
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
                    _ -> {
                        SimpleDateFormat simpleFormat = new SimpleDateFormat("yy.MM.dd");
                        return simpleFormat.format(new Date());
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
                    _ -> {
                        SimpleDateFormat simpleFormat = new SimpleDateFormat("HH");
                        return simpleFormat.format(new Date());
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
                    _ -> {
                        SimpleDateFormat simpleFormat = new SimpleDateFormat("mm");
                        return simpleFormat.format(new Date());
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
                    _ -> {
                        SimpleDateFormat simpleFormat = new SimpleDateFormat("ss");
                        return simpleFormat.format(new Date());
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
                    _ -> {
                        SimpleDateFormat simpleFormat = new SimpleDateFormat("yy");
                        return simpleFormat.format(new Date());
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
                    _ -> {
                        SimpleDateFormat simpleFormat = new SimpleDateFormat("MM");
                        return simpleFormat.format(new Date());
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
                    _ -> {
                        SimpleDateFormat simpleFormat = new SimpleDateFormat("dd");
                        return simpleFormat.format(new Date());
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
                    placeholder -> {
                        String placeholderString = placeholder.getPlaceholder();

                        String sub = placeholderString.substring(
                                placeholderString.indexOf(
                                        placeholder.getText()
                                ) - placeholder.getText().length() - 1
                        );
                        sub = sub.substring(
                                1,
                                sub.indexOf(
                                        placeholder.getRegex()
                                )
                        ).replace(
                                String.valueOf(placeholder.getRegex()),
                                ""
                        ).replace(
                                placeholder.getText(),
                                ""
                        );


                        SimpleDateFormat simpleFormat = new SimpleDateFormat(sub);
                        return simpleFormat.format(new Date());
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
                result.get().replace(
                        placeholder.getPlaceholder(),
                        replacer.onPlaceholderRequest(
                                placeholder
                        )
                ) : result.get()
        ));
        return result.get();
    }

    private boolean contains(String base, Placeholder placeholder) {
        String placeholderString = placeholder.getPlaceholder();
        if (placeholderString.contains("*") && base.contains("*")) {
            return base.contains(placeholder.getText());
        } else return base.contains(placeholderString);
    }

    public static LEDSuitePlaceholderManager withDefault() {
        return new LEDSuitePlaceholderManager(true);
    }
}

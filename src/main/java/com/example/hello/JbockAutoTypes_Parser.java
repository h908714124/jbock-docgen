package com.example.hello;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.AssertionError;
import java.lang.Character;
import java.lang.IllegalArgumentException;
import java.lang.IllegalStateException;
import java.lang.Override;
import java.lang.RuntimeException;
import java.lang.String;
import java.lang.System;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Generated by
 * <a href="https://github.com/h908714124/jbock">jbock 2.8.4</a>
 */
final class JbockAutoTypes_Parser {
  private PrintStream out = System.out;

  private PrintStream err = System.err;

  private int indent = 4;

  private int errorExitCode = 1;

  private Map<String, String> messages;

  private JbockAutoTypes_Parser() {
  }

  static JbockAutoTypes_Parser create() {
    return new JbockAutoTypes_Parser();
  }

  ParseResult parse(String[] args) {
    IndentPrinter outStream = new IndentPrinter(out, indent);
    IndentPrinter errStream = new IndentPrinter(err, indent);
    Messages msg = new Messages(messages == null ? Collections.emptyMap() : messages);
    Tokenizer tokenizer = new Tokenizer(outStream, errStream, msg);
    return tokenizer.parse(args);
  }

  JbockAutoTypes parseOrExit(String[] args) {
    ParseResult result = parse(args);
    if (result instanceof ParsingSuccess) {
      return ((ParsingSuccess) result).result();
    }
    if (result instanceof HelpPrinted) {
      System.exit(0);
    }
    if (result instanceof ParsingFailed) {
      System.exit(errorExitCode);
    }
    // all cases handled
    throw new AssertionError("never thrown");
  }

  JbockAutoTypes_Parser withErrorStream(PrintStream err) {
    this.err = Objects.requireNonNull(err);
    return this;
  }

  JbockAutoTypes_Parser withIndent(int indent) {
    this.indent = indent;
    return this;
  }

  JbockAutoTypes_Parser withErrorExitCode(int errorExitCode) {
    this.errorExitCode = errorExitCode;
    return this;
  }

  JbockAutoTypes_Parser withMessages(Map<String, String> map) {
    if (messages != null) {
      throw new IllegalStateException("setting messages twice");
    }
    this.messages = Objects.requireNonNull(map);
    return this;
  }

  JbockAutoTypes_Parser withResourceBundle(ResourceBundle bundle) {
    Map<String, String> map = new HashMap<>();
    for (String name :Collections.list(bundle.getKeys())) {
      map.put(name, bundle.getString(name));
    }
    return withMessages(map);
  }

  JbockAutoTypes_Parser withMessages(InputStream stream) {
    if (stream == null) {
      return withMessages(Collections.emptyMap());
    }
    try {
      Properties properties = new Properties();
      properties.load(stream);
      Map<String, String> map = new HashMap<>();
      for (String name : properties.stringPropertyNames()) {
        map.put(name, properties.getProperty(name));
      }
      return withMessages(map);
    }
    catch (IOException exception) {
      throw new RuntimeException(exception);
    }
  }

  private static String readValidArgument(String token, Iterator<String> it) {
    if (token.length() < 2) {
      throw new IllegalArgumentException();
    }
    boolean isLong = token.charAt(1) == '-';
    int index = token.indexOf('=');
    if (isLong && index >= 0) {
      return token.substring(index + 1);
    }
    if (!isLong && token.length() >= 3) {
      return token.substring(2);
    }
    return readNext(token, it);
  }

  private static String readNext(String token, Iterator<String> it) {
    if (!it.hasNext()) {
      throw new IllegalArgumentException("Missing value after token: " + token);
    }
    return it.next();
  }

  JbockAutoTypes_Parser withOutputStream(PrintStream out) {
    this.out = Objects.requireNonNull(out);
    return this;
  }

  private static class Tokenizer {
    final IndentPrinter err;

    final IndentPrinter out;

    final Messages messages;

    Tokenizer(IndentPrinter out, IndentPrinter err, Messages messages) {
      this.err = err;
      this.out = out;
      this.messages = messages;
    }

    ParseResult parse(String[] args) {
      try {
        Optional<? extends JbockAutoTypes> result = parse(Arrays.asList(args).iterator());
        if (result.isPresent()) {
          return new ParsingSuccess(result.get());
        }
        printUsage();
        return new HelpPrinted();
      }
      catch (RuntimeException e) {
        e.printStackTrace(err.out);
        err.println("Usage:");
        err.incrementIndent();
        err.println(synopsis());
        err.decrementIndent();
        err.println();
        err.println("Error:");
        err.incrementIndent();
        err.println(e.getMessage());
        err.decrementIndent();
        err.println();
        err.println(String.format("Try '%s --help' for more information.", "JbockAutoTypes"));
        err.println();
        return new ParsingFailed(e.getMessage());
      }
      finally {
        err.flush();
        out.flush();
      }
    }

    Optional<? extends JbockAutoTypes> parse(Iterator<String> tokens) {
      boolean first = true;
      Helper helper = new Helper();
      int position = 0;
      while (tokens.hasNext()) {
        String token = tokens.next();
        if (first && "--help".equals(token)) {
          return Optional.empty();
        }
        first = false;
        Option option = helper.readRegularOption(token);
        if (option != null) {
          helper.read(option, token, tokens);
          continue;
        }
        if (token.charAt(0) == '-') {
          throw new IllegalArgumentException("Invalid option: " + token);
        }
        if (position >= helper.positionalParsers.size()) {
          throw new IllegalArgumentException("Invalid option: " + token);
        }
        helper.positionalParsers.get(position).read(token);
        position += helper.positionalParsers.get(position).positionIncrement();
      }
      return Optional.of(helper.build());
    }

    void printUsage() {
      out.println("NAME");
      out.incrementIndent();
      String missionStatement = messages.getMessage("jbock.mission", "");
      if (missionStatement.isEmpty()) {
        out.println("JbockAutoTypes");
      }
      else {
        out.println(String.format("%s - %s", "JbockAutoTypes", missionStatement));
      }
      out.println();
      out.decrementIndent();
      out.println("SYNOPSIS");
      out.incrementIndent();
      out.println(synopsis());
      out.println();
      out.decrementIndent();
      out.println("DESCRIPTION");
      out.incrementIndent();
      List<String> descriptionFromJavadoc = new ArrayList<>();
      descriptionFromJavadoc.add("This class contains all the basic parameter types");
      descriptionFromJavadoc.add("that can be used without custom mappers or collectors in jbock 2.8.4.");
      descriptionFromJavadoc.add("Primitives and boxed primitives are omitted here.");
      descriptionFromJavadoc.add("All enums can also be used; they are mapped by their {@code valueOf} method.");
      descriptionFromJavadoc.add("");
      descriptionFromJavadoc.add("For any type {@code X} in that list,");
      descriptionFromJavadoc.add("{@code Optional<X>} is the corresponding optional type, and");
      descriptionFromJavadoc.add("{@code List<X>} is the corresponding repeatable type.");
      for (String line : messages.getMessage("jbock.description", descriptionFromJavadoc)) {
        out.println(line);
      }
      out.decrementIndent();
      out.println();
      for (Option option: Option.values()) {
        if (option.positional()) {
          printDescription(option);
          out.println();
        }
      }
      out.println("OPTIONS");
      out.incrementIndent();
      for (Option option: Option.values()) {
        if (!option.positional()) {
          printDescription(option);
          out.println();
        }
      }
      out.decrementIndent();
      List<String> defaultHelp = new ArrayList<>();
      defaultHelp.add("Print this help page.");
      defaultHelp.add("The help flag may only be passed as the first argument.");
      defaultHelp.add("Any further arguments will be ignored.");
      out.incrementIndent();
      out.println("--help");
      out.incrementIndent();
      for (String line : messages.getMessage("jbock.help", defaultHelp)) {
        out.println(line);
      }
      out.println();
      out.decrementIndent();
      out.decrementIndent();
    }

    static String synopsis() {
      StringJoiner joiner = new StringJoiner(" ");
      joiner.add("JbockAutoTypes");
      joiner.add(Option.BIG_DECIMAL.example());
      joiner.add(Option.BIG_INTEGER.example());
      joiner.add(Option.FILE.example());
      joiner.add(Option.LOCAL_DATE.example());
      joiner.add(Option.PATH.example());
      joiner.add(Option.PATTERN.example());
      joiner.add(Option.STRING.example());
      joiner.add(Option.U_RI.example());
      return joiner.toString();
    }

    void printDescription(Option option) {
      if (option.positional()) {
        out.println(option.describe().toUpperCase());
      }
      else {
        out.println(option.describe());
      }
      out.incrementIndent();
      for (String line : messages.getMessage(option.bundleKey.toLowerCase(), option.description)) {
        out.println(line);
      }
      out.decrementIndent();
    }
  }

  private static class JbockAutoTypesImpl extends JbockAutoTypes {
    final BigDecimal bigDecimal;

    final BigInteger bigInteger;

    final File file;

    final LocalDate localDate;

    final Path path;

    final Pattern pattern;

    final String string;

    final URI uRi;

    JbockAutoTypesImpl(BigDecimal bigDecimal, BigInteger bigInteger, File file, LocalDate localDate,
        Path path, Pattern pattern, String string, URI uRi) {
      this.bigDecimal = bigDecimal;
      this.bigInteger = bigInteger;
      this.file = file;
      this.localDate = localDate;
      this.path = path;
      this.pattern = pattern;
      this.string = string;
      this.uRi = uRi;
    }

    @Override
    BigDecimal bigDecimal() {
      return bigDecimal;
    }

    @Override
    BigInteger bigInteger() {
      return bigInteger;
    }

    @Override
    File file() {
      return file;
    }

    @Override
    LocalDate localDate() {
      return localDate;
    }

    @Override
    Path path() {
      return path;
    }

    @Override
    Pattern pattern() {
      return pattern;
    }

    @Override
    String string() {
      return string;
    }

    @Override
    URI uRI() {
      return uRi;
    }
  }

  private enum Option {
    BIG_DECIMAL("BigDecimal", null, "null", OptionalInt.empty(), "BIG_DECIMAL", Collections.singletonList("Mapped by: java.math.BigDecimal::new")) {
      @Override
      OptionParser parser() {
        return new RegularOptionParser(this);
      }
    },

    BIG_INTEGER("BigInteger", null, "null", OptionalInt.empty(), "BIG_INTEGER", Collections.singletonList("Mapped by: java.math.BigInteger::new")) {
      @Override
      OptionParser parser() {
        return new RegularOptionParser(this);
      }
    },

    FILE("File", null, "null", OptionalInt.empty(), "FILE", Collections.singletonList("Mapped by: java.io.File::new")) {
      @Override
      OptionParser parser() {
        return new RegularOptionParser(this);
      }
    },

    LOCAL_DATE("LocalDate", null, "null", OptionalInt.empty(), "LOCAL_DATE", Collections.singletonList("Mapped by: java.time.LocalDate::parse")) {
      @Override
      OptionParser parser() {
        return new RegularOptionParser(this);
      }
    },

    PATH("Path", null, "null", OptionalInt.empty(), "PATH", Collections.singletonList("Mapped by: java.nio.file.Paths::get")) {
      @Override
      OptionParser parser() {
        return new RegularOptionParser(this);
      }
    },

    PATTERN("Pattern", null, "null", OptionalInt.empty(), "PATTERN", Collections.singletonList("Mapped by: java.util.regex.Pattern::compile")) {
      @Override
      OptionParser parser() {
        return new RegularOptionParser(this);
      }
    },

    STRING("String", null, "null", OptionalInt.empty(), "STRING", Collections.singletonList("Mapped by: java.util.function.Function.identity()")) {
      @Override
      OptionParser parser() {
        return new RegularOptionParser(this);
      }
    },

    U_RI("URI", null, "null", OptionalInt.empty(), "U_RI", Collections.singletonList("Mapped by: java.net.URI::create")) {
      @Override
      OptionParser parser() {
        return new RegularOptionParser(this);
      }
    };

    final String longName;

    final Character shortName;

    final String bundleKey;

    final OptionalInt positionalIndex;

    final String descriptionArgumentName;

    final List<String> description;

    Option(String longName, Character shortName, String bundleKey, OptionalInt positionalIndex,
        String descriptionArgumentName, List<String> description) {
      this.longName = longName;
      this.shortName = shortName;
      this.bundleKey = bundleKey;
      this.positionalIndex = positionalIndex;
      this.description = description;
      this.descriptionArgumentName = descriptionArgumentName;
    }

    boolean positional() {
      return positionalIndex.isPresent();
    }

    String describeParam(String argname) {
      if (shortName == null) {
        return "--" + longName + argname;
      }
      if (longName == null) {
        return "-" + shortName + argname;
      }
      return "-" + shortName + argname + ", --" + longName + argname;
    }

    String example() {
      if (shortName == null) {
        return String.format("--%s=<%s>", longName, descriptionArgumentName);
      }
      return String.format("-%s <%s>", shortName, descriptionArgumentName);
    }

    Supplier<IllegalArgumentException> missingRequired() {
      return () -> positionalIndex.isPresent() ? new IllegalArgumentException(String.format("Missing parameter: <%s>", this)) : new IllegalArgumentException(String.format("Missing required option: %s (%s)", this, describeParam(""))); }

    static Map<Character, Option> shortNameMap() {
      Map<Character, Option> shortNames = new HashMap<>(Option.values().length);
      for (Option option : Option.values()) {
        if (option.shortName != null) {
          shortNames.put(option.shortName, option);
        }
      }
      return shortNames;
    }

    static Map<String, Option> longNameMap() {
      Map<String, Option> longNames = new HashMap<>(Option.values().length);
      for (Option option : Option.values()) {
        if (option.longName != null) {
          longNames.put(option.longName, option);
        }
      }
      return longNames;
    }

    static Map<Option, OptionParser> parsers() {
      Map<Option, OptionParser> parsers = new EnumMap<>(Option.class);
      for (Option option : Option.values()) {
        if (!option.positional()) {
          parsers.put(option, option.parser());
        }
      }
      return parsers;
    }

    Stream<String> values(List<String> positional) {
      if (!positionalIndex.isPresent()) {
        return Stream.empty();
      }
      if (positionalIndex.getAsInt() >= positional.size()) {
        return Stream.empty();
      }
      return positional.subList(positionalIndex.getAsInt(), positional.size()).stream();
    }

    Optional<String> value(List<String> positional) {
      if (!positionalIndex.isPresent()) {
        return Optional.empty();
      }
      if (positionalIndex.getAsInt() >= positional.size()) {
        return Optional.empty();
      }
      return Optional.of(positional.get(positionalIndex.getAsInt()));
    }

    static List<PositionalOptionParser> positionalParsers() {
      List<PositionalOptionParser> parsers = new ArrayList<>();
      for (Option option : Option.values()) {
        if (option.positional()) {
          parsers.add(option.positionalParser());
        }
      }
      return parsers;
    }

    boolean validShortToken(String token) {
      return token.length() >= 2 && token.charAt(0) == '-';
    }

    String describe() {
      return describeParam(String.format(" <%s>", descriptionArgumentName));
    }

    OptionParser parser() {
      throw new AssertionError();
    }

    PositionalOptionParser positionalParser() {
      throw new AssertionError();
    }
  }

  private static class Helper {
    final Map<String, Option> longNames = Collections.unmodifiableMap(Option.longNameMap());

    final Map<Character, Option> shortNames = Collections.unmodifiableMap(Option.shortNameMap());

    final Map<Option, OptionParser> parsers = Collections.unmodifiableMap(Option.parsers());

    final List<PositionalOptionParser> positionalParsers = Collections.unmodifiableList(Option.positionalParsers());

    void read(Option option, String token, Iterator<String> it) {
      parsers.get(option).read(token, it);
    }

    Option readRegularOption(String token) {
      if (token.length() < 2 || token.charAt(0) != '-') {
        return null;
      }
      if (token.charAt(1) == '-') {
        return readLong(token);
      }
      Option option = shortNames.get(token.charAt(1));
      if (option == null) {
        return null;
      }
      if (!option.validShortToken(token)) {
        return null;
      }
      return option;
    }

    JbockAutoTypesImpl build() {
      return new JbockAutoTypesImpl(
          parsers.get(Option.BIG_DECIMAL).value().map(BigDecimal::new).orElseThrow(Option.BIG_DECIMAL.missingRequired()),
          parsers.get(Option.BIG_INTEGER).value().map(BigInteger::new).orElseThrow(Option.BIG_INTEGER.missingRequired()),
          parsers.get(Option.FILE).value().map(File::new).orElseThrow(Option.FILE.missingRequired()),
          parsers.get(Option.LOCAL_DATE).value().map(LocalDate::parse).orElseThrow(Option.LOCAL_DATE.missingRequired()),
          parsers.get(Option.PATH).value().map(Paths::get).orElseThrow(Option.PATH.missingRequired()),
          parsers.get(Option.PATTERN).value().map(Pattern::compile).orElseThrow(Option.PATTERN.missingRequired()),
          parsers.get(Option.STRING).value().map(Function.identity()).orElseThrow(Option.STRING.missingRequired()),
          parsers.get(Option.U_RI).value().map(URI::create).orElseThrow(Option.U_RI.missingRequired()));
    }

    Option readLong(String token) {
      int index = token.indexOf('=');
      if (index < 0) {
        return longNames.get(token.substring(2));
      }
      else {
        return longNames.get(token.substring(2, index));
      }
    }
  }

  private abstract static class OptionParser {
    final Option option;

    OptionParser(Option option) {
      this.option = option;
    }

    abstract void read(String token, Iterator<String> it);

    Optional<String> value() {
      throw new AssertionError();
    }

    Stream<String> values() {
      throw new AssertionError();
    }

    boolean flag() {
      throw new AssertionError();
    }
  }

  private static class FlagOptionParser extends OptionParser {
    boolean flag;

    FlagOptionParser(Option option) {
      super(option);
    }

    @Override
    void read(String token, Iterator<String> it) {
      if (flag) {
        throw new IllegalArgumentException(String.format("Option %s (%s) is not repeatable", option, option.describeParam("")));
      }
      flag = true;
    }

    @Override
    boolean flag() {
      return flag;
    }
  }

  private static class RegularOptionParser extends OptionParser {
    String value;

    RegularOptionParser(Option option) {
      super(option);
    }

    @Override
    void read(String token, Iterator<String> it) {
      if (value != null) {
        throw new IllegalArgumentException(String.format("Option %s (%s) is not repeatable", option, option.describeParam("")));
      }
      value = readValidArgument(token, it);
    }

    @Override
    Optional<String> value() {
      return Optional.ofNullable(value);
    }
  }

  private static class RepeatableOptionParser extends OptionParser {
    List<String> values = new ArrayList<>();

    RepeatableOptionParser(Option option) {
      super(option);
    }

    @Override
    void read(String token, Iterator<String> it) {
      values.add(readValidArgument(token, it));
    }

    @Override
    Stream<String> values() {
      return values.stream();
    }
  }

  private abstract static class PositionalOptionParser {
    abstract void read(String token);

    abstract int positionIncrement();

    Optional<String> value() {
      throw new AssertionError();
    }

    Stream<String> values() {
      throw new AssertionError();
    }
  }

  private static class RegularPositionalOptionParser extends PositionalOptionParser {
    String value;

    @Override
    void read(String value) {
      this.value = value;
    }

    @Override
    int positionIncrement() {
      return 1;
    }

    @Override
    Optional<String> value() {
      return Optional.ofNullable(value);
    }
  }

  private static class RepeatablePositionalOptionParser extends PositionalOptionParser {
    List<String> values = new ArrayList<>();

    @Override
    void read(String value) {
      values.add(value);
    }

    @Override
    int positionIncrement() {
      return 0;
    }

    @Override
    Stream<String> values() {
      return values.stream();
    }
  }

  private static class IndentPrinter {
    final int baseIndent;

    final PrintWriter out;

    int indentLevel;

    IndentPrinter(PrintStream out, int baseIndent) {
      this.out = new PrintWriter(out);
      this.baseIndent = baseIndent;
    }

    void println() {
      out.println();
    }

    void println(String text) {
      if (Objects.toString(text, "").isEmpty()) {
        out.println();
        return;
      }
      for (int i = 0; i < indentLevel; i++) {
        out.print(' ');
      }
      out.println(text);
    }

    void incrementIndent() {
      indentLevel += baseIndent;
    }

    void decrementIndent() {
      indentLevel -= baseIndent;
    }

    void flush() {
      out.flush();
    }
  }

  private static class Messages {
    final Pattern br = Pattern.compile("\\r?\\n");

    final Map<String, String> messages;

    Messages(Map<String, String> messages) {
      this.messages = messages;
    }

    String getMessage(String key, String defaultValue) {
      return messages.getOrDefault(key, defaultValue);
    }

    List<String> getMessage(String key, List<String> defaultValue) {
      if (!messages.containsKey(key)) {
        return defaultValue;
      }
      return Arrays.asList(br.split(messages.get(key), -1));
    }
  }

  /**
   * This will be a sealed type in the future.
   */
  abstract static class ParseResult {
    private ParseResult() {
    }
  }

  static final class HelpPrinted extends ParseResult {
    private HelpPrinted() {
    }
  }

  static final class ParsingFailed extends ParseResult {
    private final String message;

    private ParsingFailed(String message) {
      this.message = Objects.requireNonNull(message);
    }

    String message() {
      return message;
    }
  }

  static final class ParsingSuccess extends ParseResult {
    private final JbockAutoTypes result;

    private ParsingSuccess(JbockAutoTypes result) {
      this.result = Objects.requireNonNull(result);
    }

    JbockAutoTypes result() {
      return result;
    }
  }
}

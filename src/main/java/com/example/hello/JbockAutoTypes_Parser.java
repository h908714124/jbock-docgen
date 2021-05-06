package com.example.hello;

import java.io.File;
import java.io.PrintStream;
import java.lang.IllegalStateException;
import java.lang.RuntimeException;
import java.lang.String;
import java.lang.StringBuilder;
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
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <h3>Generated by <a href="https://github.com/h908714124/jbock">jbock 4.1.000</a></h3>
 * <p>Use the default constructor to obtain an instance of this parser.</p>
 */
final class JbockAutoTypes_Parser {
  private static final Map<String, Option> OPTIONS_BY_NAME = optionsByName();

  private static final Pattern SUSPICIOUS = Pattern.compile("-[a-zA-Z0-9]+|--[a-zA-Z0-9-]+");

  private PrintStream err = System.err;

  private String programName = "jbock-auto-types";

  private int terminalWidth = 80;

  private Consumer<ParseResult> exitHook = result ->
    System.exit(result instanceof HelpRequested ? 0 : 1);

  private Map<String, String> messages = Collections.emptyMap();

  /**
   * This parse method has no side effects.
   * Consider {@link #parseOrExit()} instead which does standard error-handling
   * like printing error messages, and potentially shutting down the JVM.
   */
  ParseResult parse(String[] args) {
    if (args.length == 0)
      return new HelpRequested();
    if (args.length == 1 && ("--help".equals(args[0]) || "-h".equals(args[0])))
      return new HelpRequested();
    StatefulParser state = new StatefulParser();
    Iterator<String> it = Arrays.asList(args).iterator();
    try {
      JbockAutoTypes result = state.parse(it);
      return new ParsingSuccess(result);
    }
    catch (RuntimeException e) {
      return new ParsingFailed(e);
    }
  }

  JbockAutoTypes parseOrExit(String[] args) {
    ParseResult result = parse(args);
    if (result instanceof ParsingSuccess)
      return ((ParsingSuccess) result).getResult();
    if (result instanceof HelpRequested) {
      printOnlineHelp();
      err.flush();
      exitHook.accept(result);
      throw new RuntimeException("help requested");
    }
    err.println("Error: " + ((ParsingFailed) result).getError().getMessage());
    printTokens("        ", usage());
    err.println("Try '" + programName + " --help' for more information.");
    err.flush();
    exitHook.accept(result);
    throw new RuntimeException("parsing error");
  }

  JbockAutoTypes_Parser withProgramName(String programName) {
    this.programName = programName;
    return this;
  }

  JbockAutoTypes_Parser withTerminalWidth(int width) {
    this.terminalWidth = width == 0 ? this.terminalWidth : width;
    return this;
  }

  JbockAutoTypes_Parser withMessages(Map<String, String> map) {
    this.messages = map;
    return this;
  }

  JbockAutoTypes_Parser withExitHook(Consumer<ParseResult> exitHook) {
    this.exitHook = exitHook;
    return this;
  }

  JbockAutoTypes_Parser withErrorStream(PrintStream err) {
    this.err = err;
    return this;
  }

  void printOnlineHelp() {
    List<String> description = new ArrayList<>();
    Collections.addAll(description, "This class contains all the basic parameter types".split("\\s+", -1));
    Collections.addAll(description, "that can be used without a custom converter in jbock 4.1.000.".split("\\s+", -1));
    Collections.addAll(description, "Primitives and boxed primitives are also auto types, except the booleans.".split("\\s+", -1));
    Collections.addAll(description, "All enums are also auto types; they are converted via their static {@code valueOf} method.".split("\\s+", -1));
    Collections.addAll(description, "Special rules apply for java.util.List and java.util.Optional, see skew rules.".split("\\s+", -1));
    Collections.addAll(description, "A custom converter must be used for all other types.".split("\\s+", -1));
    printTokens("", description);
    err.println();
    err.println("USAGE");
    printTokens("        ", usage());
    err.println();
    err.println("OPTIONS");
    printOption(Option.BIG_DECIMAL, "", "  --bigdecimal BIGDECIMAL ");
    printOption(Option.BIG_INTEGER, "", "  --biginteger BIGINTEGER ");
    printOption(Option.FILE, "", "  --file FILE             ");
    printOption(Option.LOCAL_DATE, "", "  --localdate LOCALDATE   ");
    printOption(Option.PATH, "", "  --path PATH             ");
    printOption(Option.PATTERN, "", "  --pattern PATTERN       ");
    printOption(Option.U_RI, "", "  --uri URI               ");
  }

  private void printOption(Option option, String messageKey, String names) {
    String message = messageKey.isEmpty() ? null : messages.get(messageKey);
    List<String> tokens = new ArrayList<>();
    tokens.add(names);
    tokens.addAll(Optional.ofNullable(message)
          .map(String::trim)
          .map(s -> s.split("\\s+", -1))
          .map(Arrays::asList)
          .orElseGet(() -> Arrays.stream(option.description)
            .map(s -> s.split("\\s+", -1))
            .flatMap(Arrays::stream)
            .collect(Collectors.toList())));
    String continuationIndent = String.join("", Collections.nCopies(names.length() + 1, " "));
    printTokens(continuationIndent, tokens);
  }

  private void printTokens(String continuationIndent, List<String> tokens) {
    List<String> lines = makeLines(continuationIndent, tokens);
    for (String line : lines)
      err.println(line);
  }

  private List<String> makeLines(String continuationIndent, List<String> tokens) {
    List<String> result = new ArrayList<>();
    StringBuilder line = new StringBuilder();
    int i = 0;
    while (i < tokens.size()) {
      String token = tokens.get(i);
      boolean fresh = line.length() == 0;
      if (!fresh && token.length() + line.length() + 1 > terminalWidth) {
        result.add(line.toString());
        line.setLength(0);
        continue;
      }
      if (i > 0) {
        line.append(fresh ? continuationIndent : " ");
      }
      line.append(token);
      i++;
    }
    if (line.length() > 0) {
      result.add(line.toString());
    }
    return result;
  }

  private List<String> usage() {
    List<String> result = new ArrayList<>();
    result.add(" ");
    result.add(programName);
    result.add(String.format("%s %s", "--bigdecimal", "BIGDECIMAL"));
    result.add(String.format("%s %s", "--biginteger", "BIGINTEGER"));
    result.add(String.format("%s %s", "--file", "FILE"));
    result.add(String.format("%s %s", "--localdate", "LOCALDATE"));
    result.add(String.format("%s %s", "--path", "PATH"));
    result.add(String.format("%s %s", "--pattern", "PATTERN"));
    result.add(String.format("%s %s", "--uri", "URI"));
    return result;
  }

  private static String readOptionArgument(String token, Iterator<String> it) {
    if (token.charAt(1) == '-' && token.indexOf('=') >= 0)
      return token.substring(token.indexOf('=') + 1);
    if (token.charAt(1) != '-' && token.length() >= 3)
      return token.substring(2);
    if (!it.hasNext())
      throw new RuntimeException("Missing value after token: " + token);
    return it.next();
  }

  private static Map<String, Option> optionsByName() {
    Map<String, Option> result = new HashMap<>(7);
    result.put("--bigdecimal", Option.BIG_DECIMAL);
    result.put("--biginteger", Option.BIG_INTEGER);
    result.put("--file", Option.FILE);
    result.put("--localdate", Option.LOCAL_DATE);
    result.put("--path", Option.PATH);
    result.put("--pattern", Option.PATTERN);
    result.put("--uri", Option.U_RI);
    return result;
  }

  private static Map<Option, OptionParser> optionParsers() {
    Map<Option, OptionParser> parsers = new EnumMap<>(Option.class);
    parsers.put(Option.BIG_DECIMAL, new RegularOptionParser(Option.BIG_DECIMAL));
    parsers.put(Option.BIG_INTEGER, new RegularOptionParser(Option.BIG_INTEGER));
    parsers.put(Option.FILE, new RegularOptionParser(Option.FILE));
    parsers.put(Option.LOCAL_DATE, new RegularOptionParser(Option.LOCAL_DATE));
    parsers.put(Option.PATH, new RegularOptionParser(Option.PATH));
    parsers.put(Option.PATTERN, new RegularOptionParser(Option.PATTERN));
    parsers.put(Option.U_RI, new RegularOptionParser(Option.U_RI));
    return parsers;
  }

  private static RuntimeException missingRequired(String name) {
    return new RuntimeException("Missing required: " + name);
  }

  private static class StatefulParser {
    boolean endOfOptionParsing;

    Map<Option, OptionParser> optionParsers = optionParsers();

    JbockAutoTypes parse(Iterator<String> it) {
      while (it.hasNext()) {
        String token = it.next();
        if (!endOfOptionParsing && "--".equals(token)) {
          endOfOptionParsing = true;
          continue;
        }
        if (tryParseOption(token, it)) {
          continue;
        }
        if (!endOfOptionParsing) {
          if (SUSPICIOUS.matcher(token).matches())
            throw new RuntimeException("Invalid option: " + token);
        }
        throw new RuntimeException("Excess param: " + token);
      }
      return build();
    }

    boolean tryParseOption(String token, Iterator<String> it) {
      if (endOfOptionParsing)
        return false;
      Option option = tryReadOption(token);
      if (option == null)
        return false;
      optionParsers.get(option).read(token, it);
      return true;
    }

    Option tryReadOption(String token) {
      if (token.length() <= 1 || token.charAt(0) != '-')
        return null;
      if (token.charAt(1) != '-')
        return OPTIONS_BY_NAME.get(token.substring(0, 2));
      int index = token.indexOf('=');
      if (index < 0)
        return OPTIONS_BY_NAME.get(token);
      return OPTIONS_BY_NAME.get(token.substring(0, index));
    }

    JbockAutoTypes build() {
      return new JbockAutoTypesImpl(
          optionParsers.get(Option.BIG_DECIMAL).stream()
            .map(BigDecimal::new)
            .findAny()
            .orElseThrow(() -> missingRequired("BIG_DECIMAL (--bigdecimal)")),
          optionParsers.get(Option.BIG_INTEGER).stream()
            .map(BigInteger::new)
            .findAny()
            .orElseThrow(() -> missingRequired("BIG_INTEGER (--biginteger)")),
          optionParsers.get(Option.FILE).stream()
            .map(s -> {
              File f = new File(s);
              if (!f.exists()) {
                throw new IllegalStateException("File does not exist: " + s);
              }
              if (!f.isFile()) {
                throw new IllegalStateException("Not a file: " + s);
              }
              return f;
            })
            .findAny()
            .orElseThrow(() -> missingRequired("FILE (--file)")),
          optionParsers.get(Option.LOCAL_DATE).stream()
            .map(LocalDate::parse)
            .findAny()
            .orElseThrow(() -> missingRequired("LOCAL_DATE (--localdate)")),
          optionParsers.get(Option.PATH).stream()
            .map(Paths::get)
            .findAny()
            .orElseThrow(() -> missingRequired("PATH (--path)")),
          optionParsers.get(Option.PATTERN).stream()
            .map(Pattern::compile)
            .findAny()
            .orElseThrow(() -> missingRequired("PATTERN (--pattern)")),
          optionParsers.get(Option.U_RI).stream()
            .map(URI::create)
            .findAny()
            .orElseThrow(() -> missingRequired("U_RI (--uri)")));
    }
  }

  private enum Option {
    BIG_DECIMAL("Converted by: java.math.BigDecimal::new"),

    BIG_INTEGER("Converted by: java.math.BigInteger::new"),

    FILE("Converted by: <pre>{@code s -> {",
    "java.io.File f = new java.io.File(s);",
    "if (!f.exists()) {",
    "throw new java.lang.IllegalStateException(\"File does not exist: \" + s);",
    "}",
    "if (!f.isFile()) {",
    "throw new java.lang.IllegalStateException(\"Not a file: \" + s);",
    "}",
    "return f;",
    "}}</pre>"),

    LOCAL_DATE("Converted by: java.time.LocalDate::parse"),

    PATH("Converted by: java.nio.file.Paths::get"),

    PATTERN("Converted by: java.util.regex.Pattern::compile"),

    U_RI("Converted by: java.net.URI::create");

    String[] description;

    Option(String... description) {
      this.description = description;
    }
  }

  private static class JbockAutoTypesImpl extends JbockAutoTypes {
    BigDecimal bigDecimal;

    BigInteger bigInteger;

    File file;

    LocalDate localDate;

    Path path;

    Pattern pattern;

    URI uRi;

    JbockAutoTypesImpl(BigDecimal bigDecimal, BigInteger bigInteger, File file, LocalDate localDate,
        Path path, Pattern pattern, URI uRi) {
      this.bigDecimal = bigDecimal;
      this.bigInteger = bigInteger;
      this.file = file;
      this.localDate = localDate;
      this.path = path;
      this.pattern = pattern;
      this.uRi = uRi;
    }

    BigDecimal bigDecimal() {
      return bigDecimal;
    }

    BigInteger bigInteger() {
      return bigInteger;
    }

    File file() {
      return file;
    }

    LocalDate localDate() {
      return localDate;
    }

    Path path() {
      return path;
    }

    Pattern pattern() {
      return pattern;
    }

    URI uRI() {
      return uRi;
    }
  }

  private abstract static class OptionParser {
    final Option option;

    OptionParser(Option option) {
      this.option = option;
    }

    abstract void read(String token, Iterator<String> it);

    abstract Stream<String> stream();
  }

  private static class RegularOptionParser extends OptionParser {
    String value;

    RegularOptionParser(Option option) {
      super(option);
    }

    void read(String token, Iterator<String> it) {
      if (value != null)
        throw new RuntimeException(String.format("Option '%s' is a repetition", token));
      value = readOptionArgument(token, it);
    }

    Stream<String> stream() {
      return value == null ? Stream.empty() : Stream.of(value);
    }
  }

  abstract static class ParseResult {
    private ParseResult() {
    }
  }

  static final class ParsingFailed extends ParseResult {
    private final RuntimeException error;

    private ParsingFailed(RuntimeException error) {
      this.error = error;
    }

    RuntimeException getError() {
      return error;
    }
  }

  static final class ParsingSuccess extends ParseResult {
    private final JbockAutoTypes result;

    private ParsingSuccess(JbockAutoTypes result) {
      this.result = result;
    }

    JbockAutoTypes getResult() {
      return result;
    }
  }

  static final class HelpRequested extends ParseResult {
  }
}

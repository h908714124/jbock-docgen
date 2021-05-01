package com.example.hello;

import java.io.PrintStream;
import java.lang.Integer;
import java.lang.RuntimeException;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.System;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <h3>Generated by <a href="https://github.com/h908714124/jbock">jbock 3.6.006</a></h3>
 * <p>Use the default constructor to obtain an instance of this parser.</p>
 */
final class MyCommand_Parser {
  private static final Map<String, Option> OPTIONS_BY_NAME = optionsByName();

  private PrintStream out = System.out;

  private PrintStream err = System.err;

  private String programName = "my-command";

  private int maxLineWidth = 80;

  private BiConsumer<ParseResult, Integer> exitHook = (r, code) -> System.exit(code);

  private Map<String, String> messages = Collections.emptyMap();

  /**
   * This parse method has no side effects.
   * Consider {@link #parseOrExit()} instead which does standard error-handling
   * like printing error messages, and potentially shutting down the JVM.
   */
  ParseResult parse(String[] args) {
    if (args.length >= 1 && "--help".equals(args[0]))
      return new HelpRequested();
    StatefulParser state = new StatefulParser();
    Iterator<String> it = Arrays.asList(args).iterator();
    try {
      MyCommand result = state.parse(it);
      return new ParsingSuccess(result);
    }
    catch (RuntimeException e) {
      return new ParsingFailed(e);
    }
  }

  MyCommand parseOrExit(String[] args) {
    ParseResult result = parse(args);
    if (result instanceof ParsingSuccess)
      return ((ParsingSuccess) result).getResult();
    if (result instanceof HelpRequested) {
      printOnlineHelp(out);
      out.flush();
      exitHook.accept(result, 0);
      throw new RuntimeException("help requested");
    }
    err.println("Error: " + ((ParsingFailed) result).getError().getMessage());
    printTokens(err, "        ", usage());
    err.println("Try '" + programName + " --help' for more information.");
    err.flush();
    exitHook.accept(result, 1);
    throw new RuntimeException("parsing error");
  }

  MyCommand_Parser withProgramName(String programName) {
    this.programName = programName;
    return this;
  }

  MyCommand_Parser withMaxLineWidth(int chars) {
    this.maxLineWidth = chars;
    return this;
  }

  MyCommand_Parser withMessages(Map<String, String> map) {
    this.messages = map;
    return this;
  }

  MyCommand_Parser withResourceBundle(ResourceBundle bundle) {
    return withMessages(Collections.list(bundle.getKeys()).stream()
      .collect(Collectors.toMap(Function.identity(), bundle::getString)));
  }

  MyCommand_Parser withExitHook(BiConsumer<ParseResult, Integer> exitHook) {
    this.exitHook = exitHook;
    return this;
  }

  MyCommand_Parser withErrorStream(PrintStream err) {
    this.err = err;
    return this;
  }

  MyCommand_Parser withHelpStream(PrintStream out) {
    this.out = out;
    return this;
  }

  void printOnlineHelp(PrintStream printStream) {
    printStream.println("USAGE");
    printTokens(printStream, "        ", usage());
    printStream.println();
    printStream.println("PARAMETERS");
    printOption(printStream, Option.PATH, "", "path");
    printStream.println();
    printStream.println("OPTIONS");
    printOption(printStream, Option.VERBOSITY, "", "--verbosity VERBOSITY");
  }

  private void printOption(PrintStream printStream, Option option, String messageKey,
      String sample) {
    String shape_padded_24_characters = String.format("  %1$-22s", sample);
    String message = messageKey.isEmpty() ? null : messages.get(messageKey);
    List<String> tokens = new ArrayList<>();
    tokens.add(shape_padded_24_characters);
    tokens.addAll(Optional.ofNullable(message)
          .map(String::trim)
          .map(s -> s.split("\\s+", -1))
          .map(Arrays::asList)
          .orElseGet(() -> Arrays.stream(option.description)
            .map(s -> s.split("\\s+", -1))
            .flatMap(Arrays::stream)
            .collect(Collectors.toList())));
    printTokens(printStream, "                         ", tokens);
  }

  private void printTokens(PrintStream printStream, String continuationIndent,
      List<String> tokens) {
    List<String> lines = makeLines(continuationIndent, tokens);
    for (String line : lines)
      printStream.println(line);
  }

  private List<String> makeLines(String continuationIndent, List<String> tokens) {
    List<String> result = new ArrayList<>();
    StringBuilder line = new StringBuilder();
    int i = 0;
    while (i < tokens.size()) {
      String token = tokens.get(i);
      boolean fresh = line.length() == 0;
      if (!fresh && token.length() + line.length() + 1 > maxLineWidth) {
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
    result.add("[options...]");
    result.add("<path>");
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
    Map<String, Option> result = new HashMap<>(Option.values().length);
    result.put("--verbosity", Option.VERBOSITY);
    return result;
  }

  private static Map<Option, OptionParser> optionParsers() {
    Map<Option, OptionParser> parsers = new EnumMap<>(Option.class);
    parsers.put(Option.VERBOSITY, new RegularOptionParser(Option.VERBOSITY));
    return parsers;
  }

  private static ParamParser[] paramParsers() {
    ParamParser[] parsers = new ParamParser[1];
    parsers[0] = new RegularParamParser();
    return parsers;
  }

  private static RuntimeException missingRequired(String name, List<String> names) {
    return new RuntimeException("Missing required: " + name +
      (names.isEmpty() ? "" : " (" + String.join(", ", names) + ")"));
  }

  private static class StatefulParser {
    boolean endOfOptionParsing;

    Map<Option, OptionParser> optionParsers = optionParsers();

    ParamParser[] paramParsers = paramParsers();

    MyCommand parse(Iterator<String> it) {
      int position = 0;
      while (it.hasNext()) {
        String token = it.next();
        if (!endOfOptionParsing && "--".equals(token)) {
          endOfOptionParsing = true;
          continue;
        }
        if (tryParseOption(token, it))
          continue;
        if (!endOfOptionParsing && token.startsWith("-"))
          throw new RuntimeException("Invalid option: " + token);
        if (position == 1)
          throw new RuntimeException("Excess param: " + token);
        paramParsers[position].read(token);
        position++;
      }
      return build();
    }

    MyCommand build() {
      return new MyCommandImpl(
          paramParsers[0].stream().map(Paths::get).findAny().orElseThrow(() -> missingRequired("PATH", Collections.emptyList())),
          optionParsers.get(Option.VERBOSITY).stream().map(Integer::valueOf).findAny());
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
      return OPTIONS_BY_NAME.get(token.substring(0, index < 0 ? token.length() : index));
    }
  }

  private enum Option {
    PATH("A {@code @Param} is a positional parameter.",
    "This particular param is in the first position,",
    "since there are no other params in lower positions."),

    VERBOSITY("An {@code @Option} is a named option.");

    String[] description;

    Option(String... description) {
      this.description = description;
    }
  }

  private static class MyCommandImpl extends MyCommand {
    Path path;

    OptionalInt verbosity;

    MyCommandImpl(Path path, Optional<Integer> verbosity) {
      this.path = path;
      this.verbosity = verbosity.isPresent() ? OptionalInt.of(verbosity.get()) : OptionalInt.empty();
    }

    Path path() {
      return path;
    }

    OptionalInt verbosity() {
      return verbosity;
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

  private abstract static class ParamParser {
    abstract void read(String token);

    abstract Stream<String> stream();
  }

  private static class RegularParamParser extends ParamParser {
    String value;

    void read(String token) {
      value = token;
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
    private final MyCommand result;

    private ParsingSuccess(MyCommand result) {
      this.result = result;
    }

    MyCommand getResult() {
      return result;
    }
  }

  static final class HelpRequested extends ParseResult {
  }
}

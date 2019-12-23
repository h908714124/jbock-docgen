package com.example.hello;

import java.io.PrintStream;
import java.lang.Character;
import java.lang.Integer;
import java.lang.RuntimeException;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.System;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.ResourceBundle;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Generated by <a href="https://github.com/h908714124/jbock">jbock 3.4.004</a>
 */
final class MyArguments_Parser {
  private PrintStream out = System.out;

  private PrintStream err = System.err;

  private int maxLineWidth = 80;

  private Consumer<ParseResult> runBeforeExit = r -> {};

  private Map<String, String> messages = Collections.emptyMap();

  ParseResult parse(String[] args) {
    if (args.length >= 1 && "--help".equals(args[0]))
      return new HelpRequested();
    try {
      return new ParsingSuccess(parse(Arrays.asList(args).iterator()));
    }
    catch (RuntimeException e) {
      return new ParsingFailed(e);
    }
  }

  MyArguments_Parser maxLineWidth(int chars) {
    this.maxLineWidth = chars;
    return this;
  }

  MyArguments_Parser withMessages(Map<String, String> map) {
    this.messages = map;
    return this;
  }

  MyArguments_Parser withResourceBundle(ResourceBundle bundle) {
    return withMessages(Collections.list(bundle.getKeys()).stream()
      .collect(Collectors.toMap(Function.identity(), bundle::getString)));
  }

  MyArguments_Parser runBeforeExit(Consumer<ParseResult> runBeforeExit) {
    this.runBeforeExit = runBeforeExit;
    return this;
  }

  MyArguments_Parser withErrorStream(PrintStream err) {
    this.err = err;
    return this;
  }

  MyArguments_Parser withHelpStream(PrintStream out) {
    this.out = out;
    return this;
  }

  MyArguments parseOrExit(String[] args) {
    ParseResult result = parse(args);
    if (result instanceof ParsingSuccess)
      return ((ParsingSuccess) result).getResult();
    if (result instanceof HelpRequested) {
      printOnlineHelp(out);
      out.flush();
      runBeforeExit.accept(result);
      System.exit(0);
    }
    ((ParsingFailed) result).getError().printStackTrace(err);
    err.println("Error: " + ((ParsingFailed) result).getError().getMessage());
    err.println("Try '--help' for more information.");
    err.flush();
    runBeforeExit.accept(result);
    System.exit(1);
    throw new RuntimeException();
  }

  List<Map.Entry<String, String>> buildRows() {
    return Arrays.stream(Option.values()).map(option -> {
      String message = messages.getOrDefault(option.bundleKey, String.join(" ", option.description)).trim();
      return new AbstractMap.SimpleImmutableEntry<String, String>(option.shape, message);
    }).collect(Collectors.toList());
  }

  void printOnlineHelp(PrintStream printStream) {
    printWrap(printStream, 8, "", "Usage: " + synopsis());
    printStream.println();
    for (Map.Entry<String, String> row : buildRows()) {
      String key = String.format("  %1$-27s", row.getKey());
      printWrap(printStream, 29, key, row.getValue());
    }
  }

  void printWrap(PrintStream printStream, int continuationIndent, String init, String input) {
    if (input.isEmpty()) {
      String trim = init.trim();
      printStream.println(init.substring(0, init.indexOf(trim)) + trim);
      return;
    }
    String[] tokens = input.split("\\s+", -1);
    StringBuilder sb = new StringBuilder(init);
    for (String token : tokens) {
      if (token.length() + sb.length() + 1 > maxLineWidth) {
        if (sb.toString().isEmpty()) {
          printStream.println(token);
        }
        else {
          printStream.println(sb);
          sb.setLength(0);
          for (int i = 0; i < continuationIndent; i++)
             sb.append(' ');
          sb.append(token);
        }
      }
      else {
        if (sb.length() > 0 && !Character.isWhitespace(sb.charAt(sb.length() - 1)))
          sb.append(' ');
        sb.append(token);
      }
    }
    if (sb.length() > 0)
      printStream.println(sb);
  }

  String synopsis() {
    return new StringJoiner(" ").add("my-arguments").add("[options...]").add("<path>").toString();
  }

  private MyArguments parse(Iterator<String> it) {
    int position = 0;
    ParserState state = new ParserState();
    while (it.hasNext()) {
      String token = it.next();
      if ("--".equals(token)) {
        while (it.hasNext()) {
          token = it.next();
          if (position >= state.paramParsers.size())
            throw new RuntimeException("Excess param: " + token);
          position += state.paramParsers.get(position).read(token);
        }
        return state.build();
      }
      Option option = state.tryReadOption(token);
      if (option != null) {
        state.optionParsers.get(option).read(option, token, it);
        continue;
      }
      if (!token.isEmpty() && token.charAt(0) == '-')
        throw new RuntimeException("Invalid option: " + token);
      if (position >= state.paramParsers.size())
        throw new RuntimeException("Excess param: " + token);
      position += state.paramParsers.get(position).read(token);
    }
    return state.build();
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

  private static class ParserState {
    Map<String, Option> optionNames = Option.optionNames();

    Map<Option, OptionParser> optionParsers = Option.optionParsers();

    List<ParamParser> paramParsers = Option.paramParsers();

    MyArguments build() {
      return new MyArgumentsImpl(
          paramParsers.get(0).values.stream().map(Paths::get).findAny().orElseThrow(Option.PATH.missingRequired()),
          optionParsers.get(Option.VERBOSITY).values.stream().map(Integer::valueOf).findAny());
    }

    Option tryReadOption(String token) {
      if (token.length() <= 1 || token.charAt(0) != '-')
        return null;
      if (token.charAt(1) != '-')
        return optionNames.get(token.substring(0, 2));
      int index = token.indexOf('=');
      return optionNames.get(token.substring(0, index < 0 ? token.length() : index));
    }
  }

  private static class MyArgumentsImpl extends MyArguments {
    Path path;

    OptionalInt verbosity;

    MyArgumentsImpl(Path path, Optional<Integer> verbosity) {
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

  private enum Option {
    PATH(Collections.emptyList(), null, Collections.singletonList("A \"param\" is a positional parameter."), "path"),

    VERBOSITY(Arrays.asList("-v", "--verbosity"), "verbosity", Arrays.asList(
        "This javadoc will show up when \"--help\" is passed.",
        "Alternatively you can define the help text in a resource bundle."), "-v, --verbosity VERBOSITY");

    List<String> names;

    String bundleKey;

    List<String> description;

    String shape;

    Option(List<String> names, String bundleKey, List<String> description, String shape) {
      this.names = names;
      this.bundleKey = bundleKey;
      this.description = description;
      this.shape = shape;
    }

    Supplier<RuntimeException> missingRequired() {
      return () -> new RuntimeException("Missing required: " + (names.isEmpty() ? name() :
          String.format("%s (%s)", name(), String.join(", ", names))));
    }

    static Map<String, Option> optionNames() {
      Map<String, Option> result = new HashMap<>(Option.values().length);
      for (Option option : Option.values())
        option.names.forEach(name -> result.put(name, option));
      return result;
    }

    static Map<Option, OptionParser> optionParsers() {
      return Collections.singletonMap(VERBOSITY, new RegularOptionParser());
    }

    static List<ParamParser> paramParsers() {
      return Collections.singletonList(new RegularParamParser());
    }
  }

  private static class OptionParser {
    List<String> values = new ArrayList<>();

    void read(Option option, String token, Iterator<String> it) {
      values.add(readOptionArgument(token, it));
    }
  }

  private static class FlagParser extends OptionParser {
    void read(Option option, String token, Iterator<String> it) {
      if (token.charAt(1) != '-' && token.length() > 2 || token.contains("="))
        throw new RuntimeException("Invalid token: " + token);
      if (!values.isEmpty())
        throw new RuntimeException(String.format("Option %s (%s) is not repeatable", option,
            String.join(", ", option.names)));
      values.add("");
    }
  }

  private static class RegularOptionParser extends OptionParser {
    void read(Option option, String token, Iterator<String> it) {
      if (!values.isEmpty())
        throw new RuntimeException(String.format("Option %s (%s) is not repeatable", option,
            String.join(", ", option.names)));
      super.read(option, token, it);
    }
  }

  private static class ParamParser {
    List<String> values = new ArrayList<>();

    int read(String value) {
      values.add(value);
      return 0;
    }
  }

  private static class RegularParamParser extends ParamParser {
    int read(String value) {
      values.add(value);
      return 1;
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
    private final MyArguments result;

    private ParsingSuccess(MyArguments result) {
      this.result = result;
    }

    MyArguments getResult() {
      return result;
    }
  }

  static final class HelpRequested extends ParseResult {
  }
}

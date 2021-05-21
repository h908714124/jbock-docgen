package com.example.hello;

import java.io.PrintStream;
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
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * <h3>Generated by <a href="https://github.com/h908714124/jbock">jbock 4.3.001</a></h3>
 */
final class DeleteCommand_Parser {
  private PrintStream err = System.err;

  private int terminalWidth = 80;

  private Consumer<ParseResult> exitHook = result ->
    System.exit(result instanceof HelpRequested ? 0 : 1);

  ParseResult parse(String[] args) {
    if (args.length == 0)
      return new HelpRequested();
    if (args.length == 1 && ("--help".equals(args[0]) || "-h".equals(args[0])))
      return new HelpRequested();
    StatefulParser state = new StatefulParser();
    Iterator<String> it = Arrays.asList(args).iterator();
    try {
      DeleteCommand result = state.parse(it).build();
      return new ParsingSuccess(result);
    }
    catch (RuntimeException e) {
      return new ParsingFailed(e);
    }
  }

  DeleteCommand parseOrExit(String[] args) {
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
    err.println("Try 'delete-command --help' for more information.");
    err.flush();
    exitHook.accept(result);
    throw new RuntimeException("parsing error");
  }

  DeleteCommand_Parser withTerminalWidth(int width) {
    this.terminalWidth = width == 0 ? this.terminalWidth : width;
    return this;
  }

  DeleteCommand_Parser withMessages(Map<String, String> map) {
    return this;
  }

  DeleteCommand_Parser withExitHook(Consumer<ParseResult> exitHook) {
    this.exitHook = exitHook;
    return this;
  }

  DeleteCommand_Parser withErrorStream(PrintStream err) {
    this.err = err;
    return this;
  }

  void printOnlineHelp() {
    err.println("USAGE");
    printTokens("        ", usage());
    err.println();
    err.println("PARAMETERS");
    printOption(Option.PATH, "  PATH ");
    err.println();
    err.println("OPTIONS");
    printOption(Option.VERBOSITY, "  --verbosity VERBOSITY ");
  }

  private void printOption(Option option, String names) {
    List<String> tokens = new ArrayList<>();
    tokens.add(names);
    Arrays.stream(option.description)
          .map(s -> s.split("\\s+", -1))
          .flatMap(Arrays::stream)
          .forEach(tokens::add);
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
    result.add("delete-command");
    result.add("[OPTION]...");
    result.add("PATH");
    return result;
  }

  private static String readOptionName(String token) {
    if (token.length() <= 1 || token.charAt(0) != '-')
      return null;
    if (token.charAt(1) != '-')
      return token.substring(0, 2);
    int index = token.indexOf('=');
    if (index < 0)
      return token;
    return token.substring(0, index);
  }

  private static String readOptionArgument(String token, Iterator<String> it) {
    if (token.charAt(1) == '-' && token.indexOf('=') >= 0)
      return token.substring(token.indexOf('=') + 1);
    if (token.charAt(1) != '-' && token.length() >= 3)
      return token.substring(2);
    if (!it.hasNext())
      throw new RuntimeException("Missing argument after token: " + token);
    return it.next();
  }

  private static RuntimeException missingRequired(String name) {
    return new RuntimeException("Missing required: " + name);
  }

  private static class StatefulParser {
    Pattern suspicious = Pattern.compile("-[a-zA-Z0-9]+|--[a-zA-Z0-9-]+");

    Map<String, Option> optionNames = new HashMap<>(1);

    Map<Option, OptionParser> optionParsers = new EnumMap<>(Option.class);

    String[] params = new String[1];

    StatefulParser() {
      optionNames.put("--verbosity", Option.VERBOSITY);
      optionParsers.put(Option.VERBOSITY, new RegularOptionParser(Option.VERBOSITY));
    }

    StatefulParser parse(Iterator<String> it) {
      int position = 0;
      boolean endOfOptionParsing = false;
      while (it.hasNext()) {
        String token = it.next();
        if (!endOfOptionParsing && "--".equals(token)) {
          endOfOptionParsing = true;
          continue;
        }
        if (!endOfOptionParsing && tryParseOption(token, it)) {
          continue;
        }
        if (!endOfOptionParsing) {
          if (suspicious.matcher(token).matches())
            throw new RuntimeException("Invalid option: " + token);
        }
        if (position == 1)
          throw new RuntimeException("Excess param: " + token);
        params[position++] = token;
      }
      return this;
    }

    boolean tryParseOption(String token, Iterator<String> it) {
      Option option = optionNames.get(readOptionName(token));
      if (option == null)
        return false;
      optionParsers.get(option).read(token, it);
      return true;
    }

    DeleteCommand build() {
      return new DeleteCommandImpl(
          optionParsers.get(Option.VERBOSITY).stream()
            .map(Integer::valueOf)
            .findAny(),
          Optional.ofNullable(params[0])
            .map(Paths::get)
            .orElseThrow(() -> missingRequired("PATH")));
    }
  }

  private enum Option {
    VERBOSITY("A named option."),

    PATH("A positional parameter.");

    String[] description;

    Option(String... description) {
      this.description = description;
    }
  }

  private static class DeleteCommandImpl extends DeleteCommand {
    OptionalInt verbosity;

    Path path;

    DeleteCommandImpl(Optional<Integer> verbosity, Path path) {
      this.verbosity = verbosity.isPresent() ? OptionalInt.of(verbosity.get()) : OptionalInt.empty();
      this.path = path;
    }

    OptionalInt verbosity() {
      return verbosity;
    }

    Path path() {
      return path;
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
    private final DeleteCommand result;

    private ParsingSuccess(DeleteCommand result) {
      this.result = result;
    }

    DeleteCommand getResult() {
      return result;
    }
  }

  static final class HelpRequested extends ParseResult {
  }
}

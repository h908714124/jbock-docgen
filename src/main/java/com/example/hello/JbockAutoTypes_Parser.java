package com.example.hello;

import java.io.File;
import java.io.PrintStream;
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
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * <h3>Generated by <a href="https://github.com/h908714124/jbock">jbock 4.4.000</a></h3>
 */
final class JbockAutoTypes_Parser {
  private PrintStream err = System.err;

  private int terminalWidth = 80;

  private Consumer<ParseResult> exitHook = result ->
    System.exit(result instanceof HelpRequested ? 0 : 1);

  ParseResult parse(String[] args) {
    if (args.length == 0)
      return new HelpRequested();
    if (args.length == 1 && "--help".equals(args[0]))
      return new HelpRequested();
    StatefulParser statefulParser = new StatefulParser();
    Iterator<String> it = Arrays.asList(args).iterator();
    try {
      JbockAutoTypes result = statefulParser.parse(it).build();
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
      printUsageDocumentation();
      err.flush();
      exitHook.accept(result);
      throw new RuntimeException("help requested");
    }
    err.println("\u001b[31;1mERROR\u001b[m " + ((ParsingFailed) result).getError().getMessage());
    err.println(String.join(" ", usage("Usage:")));
    err.println("Type \u001b[1mjbock-auto-types --help\u001b[m for more information.");
    err.flush();
    exitHook.accept(result);
    throw new RuntimeException("parsing error");
  }

  JbockAutoTypes_Parser withTerminalWidth(int width) {
    this.terminalWidth = width == 0 ? this.terminalWidth : width;
    return this;
  }

  JbockAutoTypes_Parser withMessages(Map<String, String> map) {
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

  void printUsageDocumentation() {
    List<String> description = new ArrayList<>();
    Collections.addAll(description, "<p>This class contains all \"auto types\"".split("\\s+", -1));
    Collections.addAll(description, "that can be used without a custom converter in jbock 4.4.000:</p>".split("\\s+", -1));
    Collections.addAll(description, "<ul>".split("\\s+", -1));
    Collections.addAll(description, "<li>java.io.File</li>".split("\\s+", -1));
    Collections.addAll(description, "<li>java.math.BigDecimal</li>".split("\\s+", -1));
    Collections.addAll(description, "<li>java.math.BigInteger</li>".split("\\s+", -1));
    Collections.addAll(description, "<li>java.net.URI</li>".split("\\s+", -1));
    Collections.addAll(description, "<li>java.nio.file.Path</li>".split("\\s+", -1));
    Collections.addAll(description, "<li>java.time.LocalDate</li>".split("\\s+", -1));
    Collections.addAll(description, "<li>java.util.regex.Pattern</li>".split("\\s+", -1));
    Collections.addAll(description, "</ul>".split("\\s+", -1));
    Collections.addAll(description, "<p>Primitives and boxed primitives are also auto types, except the booleans.".split("\\s+", -1));
    Collections.addAll(description, "All enums are auto types. They are converted via their static {@code valueOf} method.".split("\\s+", -1));
    Collections.addAll(description, "Special rules apply for boolean, java.util.List and java.util.Optional.</p>".split("\\s+", -1));
    makeLines("", description).forEach(err::println);
    err.println();
    err.println("\u001b[1mUSAGE\u001b[m");
    makeLines("        ", usage(" ")).forEach(err::println);
    err.println();
    err.println("\u001b[1mOPTIONS\u001b[m");
    String indent_o = "                           ";
    printItemDocumentation(Item.FILE, "  --file FILE             ", indent_o);
    printItemDocumentation(Item.BIG_DECIMAL, "  --bigdecimal BIGDECIMAL ", indent_o);
    printItemDocumentation(Item.BIG_INTEGER, "  --biginteger BIGINTEGER ", indent_o);
    printItemDocumentation(Item.U_RI, "  --uri URI               ", indent_o);
    printItemDocumentation(Item.PATH, "  --path PATH             ", indent_o);
    printItemDocumentation(Item.LOCAL_DATE, "  --localdate LOCALDATE   ", indent_o);
    printItemDocumentation(Item.PATTERN, "  --pattern PATTERN       ", indent_o);
  }

  private void printItemDocumentation(Item item, String names, String indent) {
    List<String> tokens = new ArrayList<>();
    tokens.add(names);
    Arrays.stream(item.description)
          .map(s -> s.split("\\s+", -1))
          .flatMap(Arrays::stream)
          .forEach(tokens::add);
    makeLines(indent, tokens).forEach(err::println);
  }

  private List<String> makeLines(String indent, List<String> tokens) {
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
        line.append(fresh ? indent : " ");
      }
      line.append(token);
      i++;
    }
    if (line.length() > 0) {
      result.add(line.toString());
    }
    return result;
  }

  private List<String> usage(String prefix) {
    List<String> result = new ArrayList<>();
    result.add(prefix);
    result.add("jbock-auto-types");
    result.add(String.format("%s %s", "--file", "FILE"));
    result.add(String.format("%s %s", "--bigdecimal", "BIGDECIMAL"));
    result.add(String.format("%s %s", "--biginteger", "BIGINTEGER"));
    result.add(String.format("%s %s", "--uri", "URI"));
    result.add(String.format("%s %s", "--path", "PATH"));
    result.add(String.format("%s %s", "--localdate", "LOCALDATE"));
    result.add(String.format("%s %s", "--pattern", "PATTERN"));
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

  private static class StatefulParser {
    Pattern suspicious = Pattern.compile("-[a-zA-Z0-9]+|--[a-zA-Z0-9-]+");

    Map<String, Item> optionNames = new HashMap<>(7);

    Map<Item, OptionParser> optionParsers = new EnumMap<>(Item.class);

    StatefulParser() {
      optionNames.put("--file", Item.FILE);
      optionParsers.put(Item.FILE, new RegularOptionParser(Item.FILE));
      optionNames.put("--bigdecimal", Item.BIG_DECIMAL);
      optionParsers.put(Item.BIG_DECIMAL, new RegularOptionParser(Item.BIG_DECIMAL));
      optionNames.put("--biginteger", Item.BIG_INTEGER);
      optionParsers.put(Item.BIG_INTEGER, new RegularOptionParser(Item.BIG_INTEGER));
      optionNames.put("--uri", Item.U_RI);
      optionParsers.put(Item.U_RI, new RegularOptionParser(Item.U_RI));
      optionNames.put("--path", Item.PATH);
      optionParsers.put(Item.PATH, new RegularOptionParser(Item.PATH));
      optionNames.put("--localdate", Item.LOCAL_DATE);
      optionParsers.put(Item.LOCAL_DATE, new RegularOptionParser(Item.LOCAL_DATE));
      optionNames.put("--pattern", Item.PATTERN);
      optionParsers.put(Item.PATTERN, new RegularOptionParser(Item.PATTERN));
    }

    StatefulParser parse(Iterator<String> it) {
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
        throw new RuntimeException("Excess param: " + token);
      }
      return this;
    }

    boolean tryParseOption(String token, Iterator<String> it) {
      Item option = optionNames.get(readOptionName(token));
      if (option == null)
        return false;
      optionParsers.get(option).read(token, it);
      return true;
    }

    JbockAutoTypes build() {
      File _file = this.optionParsers.get(Item.FILE).stream()
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
            .orElseThrow(() -> new RuntimeException("Missing required option: FILE (\u001b[1m--file\u001b[m)"));
      BigDecimal _big_decimal = this.optionParsers.get(Item.BIG_DECIMAL).stream()
            .map(BigDecimal::new)
            .findAny()
            .orElseThrow(() -> new RuntimeException("Missing required option: BIGDECIMAL (\u001b[1m--bigdecimal\u001b[m)"));
      BigInteger _big_integer = this.optionParsers.get(Item.BIG_INTEGER).stream()
            .map(BigInteger::new)
            .findAny()
            .orElseThrow(() -> new RuntimeException("Missing required option: BIGINTEGER (\u001b[1m--biginteger\u001b[m)"));
      URI _u_ri = this.optionParsers.get(Item.U_RI).stream()
            .map(URI::create)
            .findAny()
            .orElseThrow(() -> new RuntimeException("Missing required option: URI (\u001b[1m--uri\u001b[m)"));
      Path _path = this.optionParsers.get(Item.PATH).stream()
            .map(Paths::get)
            .findAny()
            .orElseThrow(() -> new RuntimeException("Missing required option: PATH (\u001b[1m--path\u001b[m)"));
      LocalDate _local_date = this.optionParsers.get(Item.LOCAL_DATE).stream()
            .map(LocalDate::parse)
            .findAny()
            .orElseThrow(() -> new RuntimeException("Missing required option: LOCALDATE (\u001b[1m--localdate\u001b[m)"));
      Pattern _pattern = this.optionParsers.get(Item.PATTERN).stream()
            .map(Pattern::compile)
            .findAny()
            .orElseThrow(() -> new RuntimeException("Missing required option: PATTERN (\u001b[1m--pattern\u001b[m)"));
      return new JbockAutoTypesImpl(_file, _big_decimal, _big_integer, _u_ri, _path, _local_date,
          _pattern);
    }
  }

  private enum Item {
    FILE("converter: <pre>{@code s -> {",
    "java.io.File f = new java.io.File(s);",
    "if (!f.exists()) {",
    "throw new java.lang.IllegalStateException(\"File does not exist: \" + s);",
    "}",
    "if (!f.isFile()) {",
    "throw new java.lang.IllegalStateException(\"Not a file: \" + s);",
    "}",
    "return f;",
    "}}</pre>"),

    BIG_DECIMAL("converter: java.math.BigDecimal::new"),

    BIG_INTEGER("converter: java.math.BigInteger::new"),

    U_RI("converter: java.net.URI::create"),

    PATH("converter: java.nio.file.Paths::get"),

    LOCAL_DATE("converter: java.time.LocalDate::parse"),

    PATTERN("converter: java.util.regex.Pattern::compile");

    String[] description;

    Item(String... description) {
      this.description = description;
    }
  }

  private static class JbockAutoTypesImpl extends JbockAutoTypes {
    File _file;

    BigDecimal _big_decimal;

    BigInteger _big_integer;

    URI _u_ri;

    Path _path;

    LocalDate _local_date;

    Pattern _pattern;

    JbockAutoTypesImpl(File _file, BigDecimal _big_decimal, BigInteger _big_integer, URI _u_ri,
        Path _path, LocalDate _local_date, Pattern _pattern) {
      this._file = _file;
      this._big_decimal = _big_decimal;
      this._big_integer = _big_integer;
      this._u_ri = _u_ri;
      this._path = _path;
      this._local_date = _local_date;
      this._pattern = _pattern;
    }

    File file() {
      return _file;
    }

    BigDecimal bigDecimal() {
      return _big_decimal;
    }

    BigInteger bigInteger() {
      return _big_integer;
    }

    URI uRI() {
      return _u_ri;
    }

    Path path() {
      return _path;
    }

    LocalDate localDate() {
      return _local_date;
    }

    Pattern pattern() {
      return _pattern;
    }
  }

  private abstract static class OptionParser {
    final Item option;

    OptionParser(Item option) {
      this.option = option;
    }

    abstract void read(String token, Iterator<String> it);

    abstract Stream<String> stream();
  }

  private static class RegularOptionParser extends OptionParser {
    String value;

    RegularOptionParser(Item option) {
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

package net.jbock.compiler;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.runners.model.Statement;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static com.google.testing.compile.Compilation.Status.SUCCESS;
import static com.google.testing.compile.Compiler.javac;

public final class EvaluatingProcessor extends AbstractProcessor {

  private final ContextRunnable base;

  private Throwable thrown;

  public interface ContextRunnable {
    void run(Elements elements, Types types) throws IllegalAccessException, NoSuchFieldException, IOException, NoSuchMethodException, InstantiationException, InvocationTargetException;
  }

  public static class Builder {

    private final String[] source;

    private Builder(String[] source) {
      this.source = source;
    }

    public void run(ContextRunnable base) {
      EvaluatingProcessor evaluatingProcessor = new EvaluatingProcessor(base);
      Compilation compilation = javac().withProcessors(evaluatingProcessor).compile(
          JavaFileObjects.forSourceLines("Dummy", source));
      checkState(compilation.status().equals(SUCCESS), compilation);
      evaluatingProcessor.throwIfStatementThrew();
    }
  }

  public static Builder source(String... source) {
    return new Builder(source);
  }

  private EvaluatingProcessor(ContextRunnable base) {
    this.base = base;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latest();
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Collections.singleton("*");
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    // just run the test on the last round after compilation is over
    if (roundEnv.processingOver()) {
      try {
        TypeTool.init(processingEnv.getElementUtils(), processingEnv.getTypeUtils());
        base.run(processingEnv.getElementUtils(), processingEnv.getTypeUtils());
      } catch (Throwable e) {
        thrown = e;
      } finally {
        TypeTool.unset();
      }
    }
    return false;
  }

  /**
   * Throws what the base {@link Statement} threw, if anything.
   */
  private void throwIfStatementThrew() {
    if (thrown != null) {
      throw new RuntimeException(thrown);
    }
  }
}

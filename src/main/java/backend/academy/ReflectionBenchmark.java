package backend.academy;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import lombok.SneakyThrows;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

@State(Scope.Thread)
public class ReflectionBenchmark {
    private Student student;
    private Method method;
    private MethodHandle methodHandle;
    private Function<Student, String> function;
    private MethodHandles.Lookup lookup;

    @SuppressWarnings({"MagicNumber", "UncommentedMain"})
    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
            .include(ReflectionBenchmark.class.getSimpleName())
            .shouldFailOnError(true)
            .shouldDoGC(true)
            .mode(Mode.AverageTime)
            .timeUnit(TimeUnit.NANOSECONDS)
            .forks(1)
            .warmupForks(1)
            .warmupIterations(1)
            .warmupTime(TimeValue.seconds(5))
            .measurementIterations(1)
            .measurementTime(TimeValue.seconds(5))
            .build();
        new Runner(options).run();
    }

    @SuppressWarnings("MultipleStringLiterals")
    @SneakyThrows @Setup
    public void setup() {
        student = new Student("Dmitry", "Daymidzenko");
        method = Student.class.getMethod("name");
        lookup = MethodHandles.lookup();
        methodHandle = lookup.findVirtual(Student.class, "name", MethodType.methodType(String.class));
        function = student -> {
            try {
                return (String) methodHandle.invokeExact(student);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Benchmark
    public void directAccess(Blackhole bh) {
        bh.consume(student.name());
    }

    @Benchmark
    public void lambdaMetaFactory(Blackhole bh) {
        bh.consume(function.apply(student));
    }

    @SneakyThrows @Benchmark
    public void methodHandles(Blackhole bh) {
        bh.consume(methodHandle.invoke(student));
    }

    @SneakyThrows @Benchmark
    public void reflection(Blackhole bh) {
        bh.consume(method.invoke(student));
    }
}

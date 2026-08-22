package jdk.aprismate.invoke;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FastReflectionTest {

    static class Widget {
        private long id = 42L;
        int count;
        public static String flavor = "vanilla";

        public String greet(String who) {
            return "hi " + who;
        }

        public double twice(double v) {
            return v * 2;
        }

        public int add3(int a, int b, int c) {
            return a + b + c;
        }

        public void run() {
            throw new IllegalStateException("boom-run");
        }

        private String secret() {
            return "s3cr3t";
        }

        public static int combine(int a, long b) {
            return (int) (a + b);
        }
    }

    @Test
    void instanceMethodWithArgs() throws Throwable {
        Method m = Widget.class.getMethod("greet", String.class);
        DirectInvoker inv = FastReflection.invoker(m);
        assertThat(inv.invoke(new Widget(), new Object[]{"aprism"})).isEqualTo("hi aprism");
    }

    @Test
    void primitiveArgsAndReturn() throws Throwable {
        Method m = Widget.class.getMethod("twice", double.class);
        DirectInvoker inv = FastReflection.invoker(m);
        assertThat(inv.invoke(new Widget(), new Object[]{2.5d})).isEqualTo(5.0d);

        Method m2 = Widget.class.getMethod("add3", int.class, int.class, int.class);
        assertThat(FastReflection.invoker(m2).invoke(new Widget(), new Object[]{1, 2, 3})).isEqualTo(6);
    }

    @Test
    void zeroArgMethodAndNullArgs() throws Throwable {
        Method m = Widget.class.getMethod("add3", int.class, int.class, int.class);
        DirectInvoker inv = FastReflection.invoker(m);
        assertThatThrownBy(() -> inv.invoke(new Widget(), null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void exceptionsPropagateUnwrapped() throws Throwable {
        Method m = Widget.class.getMethod("run");
        DirectInvoker inv = FastReflection.invoker(m);
        assertThatThrownBy(() -> inv.invoke(new Widget(), new Object[0]))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("boom-run");
    }

    @Test
    void privateMethodAccessible() throws Throwable {
        Method m = Widget.class.getDeclaredMethod("secret");
        DirectInvoker inv = FastReflection.invoker(m);
        assertThat(inv.invoke(new Widget(), new Object[0])).isEqualTo("s3cr3t");
    }

    @Test
    void staticMethodIgnoresTarget() throws Throwable {
        Method m = Widget.class.getMethod("combine", int.class, long.class);
        DirectInvoker inv = FastReflection.invoker(m);
        assertThat(inv.invoke(null, new Object[]{40, 7L})).isEqualTo(47);
        assertThat(inv.invoke(new Widget(), new Object[]{1, 1L})).isEqualTo(2);
    }

    @Test
    void wrongArityRejected() throws Throwable {
        Method m = Widget.class.getMethod("greet", String.class);
        assertThatThrownBy(() -> FastReflection.invoker(m).invoke(new Widget(),
                new Object[]{"a", "b"})).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void fieldGetSetInstance() throws Throwable {
        Field f = Widget.class.getDeclaredField("id");
        DirectFieldAccessor acc = FastReflection.fieldAccessor(f);
        Widget w = new Widget();
        assertThat(acc.get(w)).isEqualTo(42L);
        acc.set(w, 99L);
        assertThat(acc.get(w)).isEqualTo(99L);

        Field c = Widget.class.getDeclaredField("count");
        c.setAccessible(true);
        DirectFieldAccessor cacc = FastReflection.fieldAccessor(c);
        cacc.set(w, 7);
        assertThat(cacc.get(w)).isEqualTo(7);
    }

    @Test
    void staticFieldAccess() throws Throwable {
        Field f = Widget.class.getField("flavor");
        DirectFieldAccessor acc = FastReflection.fieldAccessor(f);
        try {
            assertThat(acc.get(null)).isEqualTo("vanilla");
            acc.set(null, "cherry");
            assertThat(Widget.flavor).isEqualTo("cherry");
        } finally {
            acc.set(null, "vanilla");
        }
    }

    @Test
    void strategyIsMethodHandleTierHere() throws Exception {
        Method m = Widget.class.getMethod("greet", String.class);
        assertThat(FastReflection.invoker(m).strategy())
                .isIn(Strategy.METHOD_HANDLE, Strategy.METHOD_HANDLE_SET_ACCESSIBLE,
                        Strategy.PLAIN_REFLECTIVE);
    }

    @Test
    void createAndInvokeHappyPath() throws Throwable {
        Method greet = Widget.class.getMethod("greet", String.class);
        Object r = FastReflection.createAndInvoke(Widget.class, greet, new Object[]{"x"});
        assertThat(r).isEqualTo("hi x");
    }

    @Test
    void nullArgumentsRejected() throws Exception {
        assertThatThrownBy(() -> FastReflection.invoker(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> FastReflection.fieldAccessor((Field) null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

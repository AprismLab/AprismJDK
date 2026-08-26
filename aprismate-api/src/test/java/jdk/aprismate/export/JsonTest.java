package jdk.aprismate.export;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JsonTest {

    @Test
    void emptyObject() {
        assertThat(new Json().startObject().endObject().toString()).isEqualTo("{}");
    }

    @Test
    void simpleKeyValue() {
        var j = new Json().startObject()
                .key("name").value("test")
                .key("count").value(42L)
                .key("active").value(true)
                .key("ratio").value(0.5)
                .key("missing").nullValue()
                .endObject();
        assertThat(j.toString()).isEqualTo(
                "{\"name\":\"test\",\"count\":42,\"active\":true,\"ratio\":0.5,\"missing\":null}");
    }

    @Test
    void nestedObjects() {
        var j = new Json().startObject()
                .key("outer").startObject()
                .key("inner").value(1)
                .endObject()
                .endObject();
        assertThat(j.toString()).isEqualTo("{\"outer\":{\"inner\":1}}");
    }

    @Test
    void arrays() {
        var j = new Json().startObject()
                .key("items").startArray()
                .startObject().key("id").value(1).endObject()
                .startObject().key("id").value(2).endObject()
                .endArray()
                .endObject();
        assertThat(j.toString()).isEqualTo("{\"items\":[{\"id\":1},{\"id\":2}]}");
    }

    @Test
    void stringEscaping() {
        var j = new Json().startObject()
                .key("path").value("C:\\Users\\test\nnewline\ttab\"quoted\"")
                .endObject();
        String out = j.toString();
        assertThat(out).contains("\\\\");
        assertThat(out).contains("\\n");
        assertThat(out).contains("\\t");
        assertThat(out).contains("\\\"");
    }

    @Test
    void controlCharactersEscaped() {
        var j = new Json().startObject()
                .key("bin").value("\u0001\u0002\u001f")
                .endObject();
        assertThat(j.toString()).contains("\\u0001");
        assertThat(j.toString()).contains("\\u0002");
        assertThat(j.toString()).contains("\\u001f");
    }

    @Test
    void nanAndInfinityBecomeNull() {
        var j = new Json().startObject()
                .key("nan").value(Double.NaN)
                .key("inf").value(Double.POSITIVE_INFINITY)
                .key("ok").value(1.5)
                .endObject();
        assertThat(j.toString()).isEqualTo("{\"nan\":null,\"inf\":null,\"ok\":1.5}");
    }

    @Test
    void rawFragment() {
        var j = new Json().startObject()
                .key("data").raw("[1,2,3]")
                .endObject();
        assertThat(j.toString()).isEqualTo("{\"data\":[1,2,3]}");
    }
}

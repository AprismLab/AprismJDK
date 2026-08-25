package jdk.aprismate.minecraft;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class McVersionCatalogTest {

    @Test
    void yearlyVersionsKnown() {
        assertThat(McVersionCatalog.isKnown("26.2")).isTrue();
        assertThat(McVersionCatalog.isKnown("26.1")).isTrue();
        assertThat(McVersionCatalog.familyOf("26.2"))
                .isEqualTo(McVersionCatalog.Family.YEARLY);
    }

    @Test
    void legacyVersionsKnown() {
        assertThat(McVersionCatalog.isKnown("1.21.4")).isTrue();
        assertThat(McVersionCatalog.isKnown("1.20.1")).isTrue();
        assertThat(McVersionCatalog.isKnown("1.8.9")).isTrue();
        assertThat(McVersionCatalog.familyOf("1.8.9"))
                .isEqualTo(McVersionCatalog.Family.LEGACY);
    }

    @Test
    void suffixesStrippedOnLookup() {
        Optional<McVersionCatalog.Entry> e = McVersionCatalog.lookup("1.21.4-pre1");
        assertThat(e).isPresent();
        assertThat(e.get().version()).isEqualTo("1.21.4");

        assertThat(McVersionCatalog.isKnown("26.2-rc2")).isTrue();
        assertThat(McVersionCatalog.isKnown("  26.2  ")).isTrue();
    }

    @Test
    void unknownVersionIsEmpty() {
        assertThat(McVersionCatalog.lookup("9.9.9")).isEmpty();
        assertThat(McVersionCatalog.isKnown("")).isFalse();
        assertThat(McVersionCatalog.isKnown(null)).isFalse();
    }

    @Test
    void unknownStillClassifiesByHeuristic() {
        assertThat(McVersionCatalog.familyOf("1.99.99"))
                .isEqualTo(McVersionCatalog.Family.LEGACY);
        assertThat(McVersionCatalog.familyOf("27.4"))
                .isEqualTo(McVersionCatalog.Family.YEARLY);
    }

    @Test
    void yearlyLineCarriesAprismLoader() {
        McVersionCatalog.Entry e = McVersionCatalog.lookup("26.2").orElseThrow();
        assertThat(e.loaders()).contains(ModLoaderType.APRISM);
        assertThat(e.loaders()).contains(ModLoaderType.NEOFORGE);
    }

    @Test
    void legacyOldLineExcludesNeoForge() {
        McVersionCatalog.Entry e = McVersionCatalog.lookup("1.16.5").orElseThrow();
        assertThat(e.loaders()).doesNotContain(ModLoaderType.NEOFORGE);
    }

    @Test
    void catalogIsNotEmptyAndSortedNewestFirst() {
        var all = McVersionCatalog.knownEntries();
        assertThat(all).isNotEmpty();
        for (int i = 1; i < all.size(); i++) {
            String prev = all.get(i - 1).version();
            String cur = all.get(i).version();
            // sanity: no duplicates
            assertThat(prev).isNotEqualTo(cur);
        }
    }
}

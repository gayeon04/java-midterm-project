package com.hotmail.kalebmarc.textfighter.inventory;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Inventory - 제네릭 인벤토리 테스트")
class InventoryTest {

    // ── 테스트용 더미 아이템 ──────────────────────────────────────────────

    static class DummyItem implements Item {
        private final String name;
        private int quantity;

        DummyItem(String name, int quantity) {
            this.name     = name;
            this.quantity = quantity;
        }

        @Override public String  getName()        { return name;     }
        @Override public String  getDescription() { return name;     }
        @Override public int     getQuantity()    { return quantity; }

        @Override
        public boolean use() {
            if (quantity <= 0) return false;
            quantity--;
            return true;
        }
    }

    private Inventory<DummyItem> inv;

    @BeforeEach
    void setUp() {
        inv = new Inventory<>();
    }

    // ── 아이템 추가 ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("아이템 추가 시")
    class WhenAdding {

        @Test
        @DisplayName("아이템을 추가하면 size가 1 증가한다")
        void size_increases_after_add() {
            // Arrange
            DummyItem item = new DummyItem("포션", 1);

            // Act
            inv.add(item);

            // Assert
            assertThat(inv.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("같은 아이템을 두 번 추가하면 size가 2가 된다")
        void duplicate_items_allowed() {
            // Arrange
            DummyItem item = new DummyItem("포션", 1);

            // Act
            inv.add(item);
            inv.add(item);

            // Assert
            assertThat(inv.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("아이템 추가 후 isEmpty()는 false를 반환한다")
        void not_empty_after_add() {
            // Act
            inv.add(new DummyItem("포션", 1));

            // Assert
            assertThat(inv.isEmpty()).isFalse();
        }
    }

    // ── 아이템 제거 ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("아이템 제거 시")
    class WhenRemoving {

        @Test
        @DisplayName("존재하는 아이템 제거 후 size가 감소한다")
        void size_decreases_after_remove() {
            // Arrange
            DummyItem item = new DummyItem("포션", 1);
            inv.add(item);

            // Act
            inv.remove(item);

            // Assert
            assertThat(inv.size()).isZero();
        }

        @Test
        @DisplayName("없는 아이템 제거 시 false를 반환한다")
        void removing_nonexistent_item_returns_false() {
            // Arrange
            DummyItem item = new DummyItem("존재하지 않는 아이템", 1);

            // Act
            boolean result = inv.remove(item);

            // Assert
            assertThat(result).isFalse();
        }
    }

    // ── 빈 인벤토리 ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("빈 인벤토리 조회 시")
    class WhenEmpty {

        @Test
        @DisplayName("getAll()은 빈 리스트를 반환한다")
        void returns_empty_list() {
            // Act & Assert
            assertThat(inv.getAll()).isEmpty();
        }

        @Test
        @DisplayName("findFirst()는 Optional.empty를 반환한다")
        void find_first_returns_empty() {
            // Act
            var result = inv.findFirst(i -> true);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("getTotalQuantity()는 0을 반환한다")
        void total_quantity_is_zero() {
            // Act & Assert
            assertThat(inv.getTotalQuantity()).isZero();
        }
    }

    // ── findFirst / 정렬 ─────────────────────────────────────────────────

    @Test
    @DisplayName("findFirst는 조건에 맞는 첫 번째 아이템을 Optional로 반환한다")
    void find_first_returns_matching_item() {
        // Arrange
        inv.add(new DummyItem("검", 1));
        inv.add(new DummyItem("포션", 3));

        // Act
        var result = inv.findFirst(i -> i.getName().equals("포션"));

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("포션");
    }

    @Test
    @DisplayName("조건에 맞는 아이템이 없으면 Optional.empty를 반환한다")
    void find_first_returns_empty_when_no_match() {
        // Arrange
        inv.add(new DummyItem("검", 1));

        // Act
        var result = inv.findFirst(i -> i.getName().equals("전설의 검"));

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getSortedByQuantity는 수량 내림차순으로 정렬된 리스트를 반환한다")
    void sorted_by_quantity_descending() {
        // Arrange
        inv.add(new DummyItem("포션",  1));
        inv.add(new DummyItem("키트",  5));
        inv.add(new DummyItem("엘릭서", 3));

        // Act
        List<DummyItem> sorted = inv.getSortedByQuantity();

        // Assert
        assertThat(sorted).extracting(DummyItem::getQuantity)
            .containsExactly(5, 3, 1);
    }
}

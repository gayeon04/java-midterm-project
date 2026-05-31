package com.hotmail.kalebmarc.textfighter.main;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class HelpTest {

    @Test
    public void testView() {
        try (MockedStatic<Ui> mockedUi = Mockito.mockStatic(Ui.class)) {
            // Help.view()는 while(true) — 9(Back)를 반환해 루프 탈출
            // cls/println/print는 MockedStatic 기본값(no-op)으로 처리
            mockedUi.when(Ui::getValidInt).thenReturn(9);
            assertDoesNotThrow(Help::view);
        }
    }
}

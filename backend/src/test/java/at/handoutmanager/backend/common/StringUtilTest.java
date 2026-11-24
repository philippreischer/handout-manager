package at.handoutmanager.backend.common;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class StringUtilTest {

    private static final String EMPTY = "";
    private static final String BLANK = " \t ";
    private static final String VALUE_WITH_SURROUNDING_SPACES = "  Strahd von Zarovich  ";

    @Test
    void shouldReturnNull_whenValueIsNull(){
        String result = StringUtil.blankToNull(null);

        assertThat(result).isNull();
    }

    @Test
    void shouldReturnNull_whenValueIsEmpty(){
        String result = StringUtil.blankToNull(EMPTY);

        assertThat(result).isNull();
    }

    @Test
    void shouldReturnNull_whenValueIsBlank(){
        String result = StringUtil.blankToNull(BLANK);

        assertThat(result).isNull();
    }

    @Test
    void shouldKeepValueUnchanged_whenValueHasSurroundingSpaces(){
        String result = StringUtil.blankToNull(VALUE_WITH_SURROUNDING_SPACES);

        assertThat(result).isEqualTo(VALUE_WITH_SURROUNDING_SPACES);
    }
}

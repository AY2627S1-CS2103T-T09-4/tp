package seedu.address.model.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class TagTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Tag(null));
    }

    @Test
    public void constructor_invalidTagName_throwsIllegalArgumentException() {
        String invalidTagName = "";
        assertThrows(IllegalArgumentException.class, () -> new Tag(invalidTagName));
    }

    @Test
    public void isValidTagName() {
        // null tag name
        assertThrows(NullPointerException.class, () -> Tag.isValidTagName(null));
    }

    @Test
    public void equals_differentLetterCase_returnsTrue() {
        Tag tag = new Tag("followup");

        assertEquals(tag, new Tag("FOLLOWUP"));
        assertEquals(tag, new Tag("FollowUp"));
        assertEquals(tag.hashCode(), new Tag("FOLLOWUP").hashCode());
        assertNotEquals(tag, new Tag("followup2"));
        assertFalse(tag.equals(null));
        assertFalse(tag.equals("followup"));
    }

    @Test
    public void toString_keepsOriginalSpelling() {
        assertEquals("[FollowUp]", new Tag("FollowUp").toString());
        assertEquals("FollowUp", new Tag("FollowUp").tagName);
    }

    @Test
    public void hashSet_caseVariants_keepsFirstSpellingOnly() {
        Set<Tag> tags = new LinkedHashSet<>();
        assertTrue(tags.add(new Tag("FollowUp")));
        assertFalse(tags.add(new Tag("followup")));

        assertEquals(1, tags.size());
        assertEquals("FollowUp", tags.iterator().next().tagName);
    }
}

package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.NextCommand;
import seedu.address.logic.commands.PrevCommand;
import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.commands.ViewCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.MatchesAllCriteriaPredicate;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.VisitDate;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PersonUtil;

public class AddressBookParserTest {

    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parseCommand_add() throws Exception {
        Person person = new PersonBuilder().build();
        AddCommand command = (AddCommand) parser.parseCommand(PersonUtil.getAddCommand(person));
        assertEquals(new AddCommand(person), command);
    }

    @Test
    public void parseCommand_clear() throws Exception {
        assertTrue(parser.parseCommand(ClearCommand.COMMAND_WORD) instanceof ClearCommand);
        assertTrue(parser.parseCommand(ClearCommand.COMMAND_WORD + " 3") instanceof ClearCommand);
    }

    @Test
    public void parseCommand_delete() throws Exception {
        DeleteCommand command = (DeleteCommand) parser.parseCommand(
                DeleteCommand.COMMAND_WORD + " " + INDEX_FIRST_PERSON.getOneBased());
        assertEquals(new DeleteCommand(INDEX_FIRST_PERSON), command);
    }

    @Test
    public void parseCommand_edit() throws Exception {
        Person person = new PersonBuilder().build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(person).build();
        EditCommand command = (EditCommand) parser.parseCommand(EditCommand.COMMAND_WORD + " "
                + INDEX_FIRST_PERSON.getOneBased() + " " + PersonUtil.getEditPersonDescriptorDetails(descriptor));
        assertEquals(new EditCommand(INDEX_FIRST_PERSON, descriptor), command);
    }

    @Test
    public void parseCommand_exit() throws Exception {
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD) instanceof ExitCommand);
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD + " 3") instanceof ExitCommand);
    }

    @Test
    public void parseCommand_find() throws Exception {
        List<String> keywords = List.of("foo", "bar", "baz");
        FindCommand command = (FindCommand) parser.parseCommand(
                FindCommand.COMMAND_WORD + " n/" + keywords.stream().collect(Collectors.joining(" ")));
        assertEquals(new FindCommand(new MatchesAllCriteriaPredicate(
                List.of(new NameContainsKeywordsPredicate(keywords)))), command);
    }

    @Test
    public void parseCommand_help() throws Exception {
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD) instanceof HelpCommand);
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD + " 3") instanceof HelpCommand);
    }

    @Test
    public void parseCommand_list() throws Exception {
        assertTrue(parser.parseCommand(ListCommand.COMMAND_WORD) instanceof ListCommand);
        assertTrue(parser.parseCommand(ListCommand.COMMAND_WORD + " 3") instanceof ListCommand);
    }

    @Test
    public void parseCommand_next() throws Exception {
        // EP: Valid dispatch preserves arguments while accepting surrounding and inter-token whitespace.
        NextCommand expectedCommand = new NextCommand(INDEX_FIRST_PERSON, new VisitDate("1/11/2026 0930"));
        assertEquals(expectedCommand, parser.parseCommand("next 1 1/11/2026 0930"));
        assertEquals(expectedCommand, parser.parseCommand(" \tnext\t1  01/11/2026\t0930  "));
    }

    @Test
    public void parseCommand_bareNext_usesNextUsage() {
        // Boundary: A recognized command with zero arguments must retain that command's usage.
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, NextCommand.MESSAGE_USAGE);
        assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand("next"));
    }

    @Test
    public void parseCommand_oversizedNextIndex_usesSpecificErrorAndNextUsage() {
        // Boundary: The decimal string immediately above Integer.MAX_VALUE survives dispatch to the Next parser.
        String expectedMessage = NextCommandParser.MESSAGE_INDEX_TOO_LARGE + "\n" + NextCommand.MESSAGE_USAGE;
        assertThrows(ParseException.class,
                expectedMessage, () -> parser.parseCommand("next 2147483648 2/11/2026 1000"));
    }

    @Test
    public void parseCommand_nextCapitalized_preservesCommandCasePolicy() {
        // EP: Command words are case-sensitive even though valid arguments allow whitespace normalization.
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("Next 1 1/11/2026 0930"));
    }

    @Test
    public void parseCommand_prev() throws Exception {
        PrevCommand expectedCommand = new PrevCommand(INDEX_FIRST_PERSON);
        assertEquals(expectedCommand, parser.parseCommand("prev 1"));
        assertEquals(expectedCommand, parser.parseCommand(" \tprev\t1  "));
    }

    @Test
    public void parseCommand_barePrev_usesPrevUsage() {
        // Boundary: The zero-argument Prev case remains a Prev usage error after dispatch.
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, PrevCommand.MESSAGE_USAGE);
        assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand("prev"));
    }

    @Test
    public void parseCommand_oversizedPrevIndex_usesSpecificErrorAndPrevUsage() {
        // Boundary: The first overflowing positive index must keep the Prev-specific usage context.
        String expectedMessage = ParserUtil.MESSAGE_INDEX_TOO_LARGE + "\n" + PrevCommand.MESSAGE_USAGE;
        assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand("prev 2147483648"));
    }

    @Test
    public void parseCommand_sort() throws Exception {
        assertTrue(parser.parseCommand("sort date") instanceof SortCommand);
        assertTrue(parser.parseCommand("  sort   date  ") instanceof SortCommand);
        assertEquals(new SortCommand(LocalDate.of(2026, 9, 18)),
                parser.parseCommand("sort date 18/9/2026"));
    }

    @Test
    public void parseCommand_view() throws Exception {
        ViewCommand command = (ViewCommand) parser.parseCommand(
                ViewCommand.COMMAND_WORD + " " + INDEX_FIRST_PERSON.getOneBased());
        assertEquals(new ViewCommand(INDEX_FIRST_PERSON), command);
    }

    @Test
    public void parseCommand_unrecognisedInput_throwsParseException() {
        assertThrows(ParseException.class, String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE), ()
            -> parser.parseCommand(""));
    }

    @Test
    public void parseCommand_unknownCommand_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("unknownCommand"));
    }
}

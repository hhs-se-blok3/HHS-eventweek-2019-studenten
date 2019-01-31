package nl.quintor.solitaire.ui.cli;

import nl.quintor.solitaire.models.deck.Deck;
import nl.quintor.solitaire.models.state.GameState;

import java.util.Collection;

/**
 * {@link GameState} parser for terminal printing. The class is not instantiable, all constructors are private.
 */
class GameStateParser {
    private final static int COLUMN_WIDTH = 8; // 8 columns in 64 char width (80 char width is Windows default)
    private final static int FIRST_COLUMN_WIDTH = 3;

    protected GameStateParser(){}

    /**
     * Parses {@link GameState} to a String representation for terminal printing.
     *
     * <pre>{@code
     * Example:
     *
     * 0 moves played in 00:29 for 0 points
     *
     *     O                      SA      SB      SC      SD
     *    ♤ 9                     _ _     _ _     _ _     _ _
     *
     *     A       B       C       D       E       F       G
     *  0 ♦ 6     ? ?     ? ?     ? ?     ? ?     ? ?     ? ?
     *  1         ♤ 8     ? ?     ? ?     ? ?     ? ?     ? ?
     *  2                 ♦ 7     ? ?     ? ?     ? ?     ? ?
     *  3                         ♤ 6     ? ?     ? ?     ? ?
     *  4                                 ♤ K     ? ?     ? ?
     *  5                                         ♧ 2     ? ?
     *  6                                                 ♥ 6
     *  7
     *  }</pre>
     *
     *  @param gameState a representation of the current state of the game
     *  @return a visual representation of the gameState (for monospace terminal printing)
     */
    static String parseGameState(GameState gameState){
        StringBuilder builder = new StringBuilder();

        // row 1: empty first column, score and game time
        builder.append(gameState + "\n\n");

        // row 2: empty column 1, stock header, empty column 3-4, 4x capital letter stack header
        padNAdd(builder, "", FIRST_COLUMN_WIDTH);
        padNAdd(builder, "O (" + (gameState.getStock().size() + gameState.getWaste().size()) + ")", COLUMN_WIDTH);
        for (int i = 0; i<2; i++) padNAdd(builder, "", COLUMN_WIDTH);
        gameState.getStackPiles().keySet().forEach(header -> padNAdd(builder, header, COLUMN_WIDTH));
        builder.append("\n");

        // row 3: empty first column, stock with card counter, two empty columns, 4x stack
        padNAdd(builder, "", FIRST_COLUMN_WIDTH);
        String topStockCardString = getCardStringOrNull(gameState.getStock(), gameState.getStock().size()-1);
        padNAdd(builder, topStockCardString != null ? topStockCardString : "_ _", COLUMN_WIDTH);
        padNAdd(builder, "", COLUMN_WIDTH);
        padNAdd(builder, "", COLUMN_WIDTH);
        gameState.getStackPiles().values().stream()
            .map(stack -> getCardStringOrNull(stack, stack.size() - 1))
            .forEach(cardString -> padNAdd(builder, cardString != null ? cardString : "_ _", COLUMN_WIDTH));

        // row 4: blank
        builder.append("\n\n");

        // row 5: empty first column, 7x capital letter column header
        padNAdd(builder, "", FIRST_COLUMN_WIDTH);
        gameState.getColumns().keySet().forEach(header -> padNAdd(builder, header, COLUMN_WIDTH));
        builder.append("\n");

        // row 6-n: row header int, card in row for each column in the gamestate
        for (int row = 0; true; row++) {
            padNAdd(builder, String.format("%" + (FIRST_COLUMN_WIDTH-1) + "s", row), FIRST_COLUMN_WIDTH);
            if (!printRow(builder, gameState.getColumns().values(), row)) break;
            builder.append("\n");
        }

        builder.append("\n");

        return builder.toString();
    }

    /**
     * Add a String representation of the requested row of all provided columns to the provided StringBuilder. If the
     * requested row did not contain any cards, return false, else true.
     * This method uses the padAndAdd @see{{@link #padNAdd(StringBuilder, String, int)}}
     * Invisible cards should be printed as "? ?"
     *
     * @param builder contains the visualization of the game state
     * @param columns the columns of which the row is printed
     * @param row the row of the columns to be printed
     * @return did the row contain any cards
     */
    protected static boolean printRow(StringBuilder builder, Collection<Deck> columns, int row){
        boolean cardsLeft = false;
        for(Deck column : columns){
            String cardString = row < column.getInvisibleCards() ? "? ?" : getCardStringOrNull(column, row);
            if (cardString != null) cardsLeft = true;
            padNAdd(builder, cardString != null ? cardString : "", COLUMN_WIDTH);
        }
        return cardsLeft;
    }

    /**
     * Attempts to get the specified card from the deck, and returns null if the requested index is out of bounds.
     *
     * @param deck deck to get the card from
     * @param index index of the card to get
     * @return the requested card or null
     */
    protected static String getCardStringOrNull(Deck deck, int index){
        try{
            return deck.get(index).toShortString();
        } catch (IndexOutOfBoundsException e){
            return null;
        }
    }

    /**
     * Add a space to the left of the string if it is of length 1, then add spaces to the right until it is of size
     * totalLength. Append the result to the StringBuilder.
     *
     * @param builder StringBuilder to append the result to
     * @param string String to pad and append
     * @param totalLength The total length that the String must become
     */
    protected static void padNAdd(StringBuilder builder, String string, int totalLength){
        builder.append(String.format("%-" + totalLength + "s", (string.length() == 1) ? " " + string : string));
    }
}

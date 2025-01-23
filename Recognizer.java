import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Recognizer{
    //global variables to make life a little easier <3
    private static List<Common.Token> tokens;
    private static int currentTokenIndex;
    private static PrintWriter writer;

    public static void main(String[] args){
        if (args.length < 2) {
            System.out.println("I require both an input and an output file path in order to work my magic!");
            return;
        }

        File inputFile = new File(args[0]); //output file from tokenizer
        File outputFile = new File(args[1]);

        if (!inputFile.exists()) {
            System.out.println("Something's amiss with the input file...");
            return;
        }

        //prepare for writing to the output file
        try{
            writer = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));
        } catch (IOException e){
            System.out.println("File writing machine broke!");
            return;
        }

        tokens = new ArrayList<>(); //stores the tokens collected from the tokenizer
        currentTokenIndex = 0; //index tracker

        //extract the tokens from the input file
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))){
            String line;
            while ((line = reader.readLine()) != null){ //while there are lines to parse
                String[] lexComponents = line.split(" "); //separate the lexeme from the token
                Common.Token tokenType = Common.Token.valueOf(lexComponents[0]); //extract the token
                tokens.add(tokenType); //collect the token
            }
        } catch (IOException e){
            System.out.println("File reading machine broke!");
            return;
        }

        function(); //run the recursive descent parser

        writer.flush(); //make the writer finish writing
        writer.close(); //tell it to go back to sleep
    }

    //ALL NECESSARY FUNCTIONS FOR PARSING:

    //returns the current token without consuming it
    private static Common.Token peekToken(){
        if (currentTokenIndex < tokens.size()){ //if there are still tokens to be examined
            return tokens.get(currentTokenIndex); //return the current token
        }
        else{
            return null;
        }
    }

    //returns the current token, consumes it, and moves to the next token
    private static Common.Token consumeToken(){
        if (currentTokenIndex < tokens.size()){ //if there are still tokens to be examined
            return tokens.get(currentTokenIndex++); //return the current token then increment the index tracker
        }
        else{
            return null;
        }
    }

    //checks to see if the current token matches the expected token
    private static void expectedVsActual(Common.Token expectedToken, String rule){
        Common.Token actualToken = peekToken(); //examine the current token
        if (actualToken == expectedToken){
            consumeToken(); //consume if there's a match
        }
        else{ //yell about it if there isn't and brick the program
            writer.println("Error: In grammar rule " + rule + ", expected token #" + (currentTokenIndex + 1) + " to be " + expectedToken + " but was " + actualToken);
            writer.flush();
            System.exit(0);
        }
    }

    private static void function(){
        if (peekToken() == Common.Token.VARTYPE){ //ensure the header actually exists
            header();
        }
        else{
            writer.println("Error: In grammar rule function, expected a valid header non-terminal to be present but was not");
            writer.flush();
            System.exit(0);
        }

        if (peekToken() == Common.Token.LEFT_BRACKET){ //ensure the body actually exists
            body();
        }
        else{
            writer.println("Error: In grammar rule function, expected a valid body non-terminal to be present but was not");
            writer.flush();
            System.exit(0);
        }

        if (currentTokenIndex != tokens.size()){ //if not all tokens were consumed
            writer.println("Error: Only consumed " + currentTokenIndex + " of the " + tokens.size() + " given tokens");
            writer.flush();
            System.exit(0);
        }

        writer.println("PARSED!!!"); //only occurs if all tokens were consumed
        writer.flush();
    }

    private static void header(){
        String rule = "header"; //rule name for error message

        expectedVsActual(Common.Token.VARTYPE, rule);

        expectedVsActual(Common.Token.IDENTIFIER, rule);

        expectedVsActual(Common.Token.LEFT_PARENTHESIS, rule);

        if (peekToken() == Common.Token.VARTYPE){ //check for optional argument declaration
            argDecl();
        }

        expectedVsActual(Common.Token.RIGHT_PARENTHESIS, rule);
    }

    private static void argDecl(){
        String rule = "arg-decl";

        //match and consume function return type
        expectedVsActual(Common.Token.VARTYPE, rule);

        //match and consume function name
        expectedVsActual(Common.Token.IDENTIFIER, rule);

        //handle potential additional arguments
        while (peekToken() == Common.Token.COMMA){
            consumeToken(); //consume the comma
            expectedVsActual(Common.Token.VARTYPE, rule);
            expectedVsActual(Common.Token.IDENTIFIER, rule);
        }
    }

    private static void body(){
        String rule = "body";

        expectedVsActual(Common.Token.LEFT_BRACKET, rule);

        //check for optional statement-list
        if (peekToken() == Common.Token.WHILE_KEYWORD || peekToken() == Common.Token.RETURN_KEYWORD || peekToken() == Common.Token.IDENTIFIER){
            statementList();
        }

        expectedVsActual(Common.Token.RIGHT_BRACKET, rule);
    }

    private static void statementList(){
        String rule = "statement-list";

        if (peekToken() == Common.Token.WHILE_KEYWORD || peekToken() == Common.Token.RETURN_KEYWORD || peekToken() == Common.Token.IDENTIFIER){ //ensure a statement actually exists
            statement();
        }
        else{
            writer.println("In grammar rule statement-list, expected a valid statement non-terminal to be present but was not");
            writer.flush();
            System.exit(0);
        }

        //check for optional additional statements
        while (peekToken() != null && (peekToken() == Common.Token.WHILE_KEYWORD || peekToken() == Common.Token.RETURN_KEYWORD || peekToken() == Common.Token.IDENTIFIER)){
            statement();
        }
    }

    private static void statement(){
        String rule = "statement";

        Common.Token currentToken = peekToken();

        //ensure that either a while-loop, return, or identifier are present
        if (currentToken == Common.Token.WHILE_KEYWORD){
            whileLoop();
        }
        else if (currentToken == Common.Token.RETURN_KEYWORD){
            returnRule();
        }
        else if (currentToken == Common.Token.IDENTIFIER){
            assignment();
        }
        else{
            writer.println("Error: In grammar rule statement, expected a valid while-loop or return or assignment non-terminal to be present but was not");
            writer.flush();
            System.exit(0);
        }
    }

    private static void whileLoop(){
        String rule = "while-loop";

        expectedVsActual(Common.Token.WHILE_KEYWORD, rule);

        expectedVsActual(Common.Token.LEFT_PARENTHESIS, rule);

        if (peekToken() == Common.Token.IDENTIFIER || peekToken() == Common.Token.NUMBER || peekToken() == Common.Token.LEFT_PARENTHESIS){ //ensure an expression actually exists
            expression();
        }
        else{
            writer.println("Error: In grammar rule while-loop, expected a valid expression non-terminal to be present but was not");
            writer.flush();
            System.exit(0);
        }

        expectedVsActual(Common.Token.RIGHT_PARENTHESIS, rule);

        //ensure a body actually exists
        if (peekToken() == Common.Token.LEFT_BRACKET){
            body();
        }
        else{
            writer.println("Error: In grammar rule while-loop, expected a valid body non-terminal to be present but was not");
            writer.flush();
            System.exit(0);
        }
    }

    private static void returnRule(){
        String rule = "return";

        expectedVsActual(Common.Token.RETURN_KEYWORD, rule);

        if (peekToken() == Common.Token.IDENTIFIER || peekToken() == Common.Token.NUMBER || peekToken() == Common.Token.LEFT_PARENTHESIS){ //ensure an expression actually exists
            expression();
        }
        else{
            writer.println("Error: In grammar rule return, expected a valid expression non-terminal to be present but was not");
            writer.flush();
            System.exit(0);
        }

        expectedVsActual(Common.Token.EOL, rule);
    }

    private static void assignment(){
        String rule = "assignment";

        expectedVsActual(Common.Token.IDENTIFIER, rule);

        expectedVsActual(Common.Token.EQUAL, rule);

        if (peekToken() == Common.Token.IDENTIFIER || peekToken() == Common.Token.NUMBER || peekToken() == Common.Token.LEFT_PARENTHESIS){ //ensure an expression actually exists
            expression();
        }
        else{
            writer.println("Error: In grammar rule assignment, expected a valid expression non-terminal to be present but was not");
            writer.flush();
            System.exit(0);
        }

        expectedVsActual(Common.Token.EOL, rule);
    }

    private static void expression(){
        Common.Token currentToken = peekToken();

        //first valid structure for expression
        if (currentToken == Common.Token.IDENTIFIER || currentToken == Common.Token.NUMBER){
            term();
            while (peekToken() == Common.Token.BINOP){ //check for optional additional terms
                consumeToken();
                if (peekToken() == Common.Token.IDENTIFIER || peekToken() == Common.Token.NUMBER){ //ensure said terms actually exists
                    term();
                }
                else{
                    writer.println("Error: In grammar rule expression, expected a valid term non-terminal to be present but was not");
                    writer.flush();
                    System.exit(0);
                }
            }
        }

        //second valid structure for expression
        else if (currentToken == Common.Token.LEFT_PARENTHESIS){
            consumeToken();

            if (peekToken() == Common.Token.IDENTIFIER || peekToken() == Common.Token.NUMBER || peekToken() == Common.Token.LEFT_PARENTHESIS){ //ensure an expression actually exists
                expression();
            }
            else{
                writer.println("Error: In grammar rule expression, expected a valid expression non-terminal to be present but was not");
                writer.flush();
                System.exit(0);
            }

            if (peekToken() == Common.Token.RIGHT_PARENTHESIS){
                consumeToken();
            }
            else{
                writer.println("Error: In grammar rule expression, expected token #" + (currentTokenIndex + 1) + " to be RIGHT_PARENTHESIS but was " + peekToken());
                writer.flush();
                System.exit(0);
            }
        }

        //this error message should be more expressive in order to convey that neither a term nor a {expression} structure was present,
        //but this could not be properly accomplished due to the bounds of the expectations of the testing software. this current implementation
        //satisfies those expectations, though.  
        else{
            writer.println("Error: In grammar rule expression, expected a valid term non-terminal to be present but was not");
            writer.flush();
            System.exit(0);
        }
    }

    private static void term(){
        //check current token
        Common.Token currentToken = peekToken();
        if (currentToken == Common.Token.IDENTIFIER || currentToken == Common.Token.NUMBER){
            consumeToken();
        }
        else{
            //another situation where the error message isn't as expressive as it should be but could not be rectified.
            writer.println("Error: In grammar rule term, expected token #" + (currentTokenIndex + 1) + " to be IDENTIFIER or NUMBER but was " + currentToken);
            writer.flush();
            System.exit(0);
        }
    }
}

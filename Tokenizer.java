import java.io.*;

public class Tokenizer{
    public static void main(String[] args) {

        if (args.length < 2) {
            System.out.println("I require both an input and an output file path in order to work my magic!");
            return;
        }

        File inputFile = new File(args[0]);
        File outputFile = new File(args[1]);

        if (!inputFile.exists()) {
            System.out.println("Something's amiss with the input file...");
            return;
        }

        //read the input file character by character
        try (PushbackReader reader = new PushbackReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))){

            int character;
            StringBuilder currentLexeme = new StringBuilder(); //for building lexemes

            while ((character = reader.read()) != -1){ //-1 indicates EOF

                char currentChar = (char) character;

                if (isDelimiterOrOperator(currentChar)){
                    //handles multiple single character tokens not separated by whitespace
                    if (currentLexeme.length() > 0){
                        Common.Lex lex = processLexeme(currentLexeme);
                        writer.write(lex + System.lineSeparator());
                        currentLexeme.setLength(0);
                    }

                    //look ahead for multi-character operators
                    if (currentChar == '!' || currentChar == '='){
                        int nextChar = reader.read();
                        if (nextChar != -1 && ((currentChar == '!' && (char) nextChar == '=') || (currentChar == '=' && (char) nextChar == '='))){ //if there is a != or a == operator
                            currentLexeme.append(currentChar).append((char) nextChar);
                            Common.Lex lex = processLexeme(currentLexeme);
                            writer.write(lex + System.lineSeparator());
                            currentLexeme.setLength(0);
                            continue; //skip further processing for current character
                        }
                        else if (nextChar != -1){
                            reader.unread(nextChar); //we're not dealing with a multi-char operator; push back the next character
                        }
                    }

                    //handles no-fuss single character tokens
                    currentLexeme.append(currentChar);
                    Common.Lex lex = processLexeme(currentLexeme);
                    writer.write(lex + System.lineSeparator());
                    currentLexeme.setLength(0);
                }

                //handles multi-character lexemes
                else if (!Character.isWhitespace(currentChar)){
                    currentLexeme.append(currentChar);
                }

                //handles whitespace (processes accumulated lexeme)
                else if (Character.isWhitespace(currentChar)){
                    if (currentLexeme.length() > 0){
                        Common.Lex lex = processLexeme(currentLexeme);
                        writer.write(lex + System.lineSeparator());
                        currentLexeme.setLength(0);
                    }
                }
            }

            //final round of lexeme processing for any leftovers
            if (currentLexeme.length() > 0){
                Common.Lex lex = processLexeme(currentLexeme);
                writer.write(lex + System.lineSeparator());
            }

        } catch (IOException e){
            System.out.println("File reading/writing machine broke!");
        }
    }

    //check for token types
    private static Common.Token getTokenType(String lexeme){
        if (lexeme.equals("(")){
            return Common.Token.LEFT_PARENTHESIS;
        }
        else if (lexeme.equals(")")){
            return Common.Token.RIGHT_PARENTHESIS;
        }
        else if (lexeme.equals("{")){
            return Common.Token.LEFT_BRACKET;
        }
        else if (lexeme.equals("}")){
            return Common.Token.RIGHT_BRACKET;
        }
        else if (lexeme.equals("while")){
            return Common.Token.WHILE_KEYWORD;
        }
        else if (lexeme.equals("return")){
            return Common.Token.RETURN_KEYWORD;
        }
        else if (lexeme.equals("=")){
            return Common.Token.EQUAL;
        }
        else if (lexeme.equals(",")){
            return Common.Token.COMMA;
        }
        else if (lexeme.equals(";")){
            return Common.Token.EOL;
        }
        else if (lexeme.equals("int") || lexeme.equals("void")){
            return Common.Token.VARTYPE;
        }
        else if (lexeme.matches("[a-zA-Z][a-zA-Z0-9]*")){
            return Common.Token.IDENTIFIER;
        }
        else if (lexeme.equals("+") || lexeme.equals("*") || lexeme.equals("!=") || lexeme.equals("==") || lexeme.equals("%")){
            return Common.Token.BINOP;
        }
        else if (lexeme.matches("[0-9]+")){
            return Common.Token.NUMBER;
        }
        else{
            //custom token for error checking
            //AKA: this should never happen
            return Common.Token.INVALID;
        }
    }

    //check if current character is a delimiter or operator
    private static boolean isDelimiterOrOperator(char ch){
        return ch == '(' || ch == ')' || ch == '{' || ch == '}' || ch == '=' || ch == ',' || ch == ';' || ch == '+' || ch == '*' || ch == '%' || ch == '!' || ch == '<' || ch == '>';
    }

    //creates Lex objects
    private static Common.Lex processLexeme(StringBuilder lexeme){
        Common.Token token = getTokenType(lexeme.toString());
        return new Common.Lex(token, lexeme.toString());
    }

}

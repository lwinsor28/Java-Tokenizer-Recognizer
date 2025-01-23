public class Common{

    public enum Token {
        LEFT_PARENTHESIS,
        RIGHT_PARENTHESIS,
        LEFT_BRACKET,
        RIGHT_BRACKET,
        WHILE_KEYWORD,
        RETURN_KEYWORD,
        EQUAL,
        COMMA,
        EOL,
        VARTYPE,
        IDENTIFIER,
        BINOP,
        NUMBER,
        INVALID //custom added token for error checking
    }

    public static class Lex{
        private Token token;
        private String lexeme;

        public Lex(Token aToken, String aLexeme){
            token = aToken;
            lexeme = aLexeme;
        }

        //NOTE: forgoing the use of setters to enforce immutability

        public Token getToken() {
            return token;
        }

        public String getLexeme(){
            return lexeme;
        }

        public String toString(){
            return token + " " + lexeme;
        }
    }

}

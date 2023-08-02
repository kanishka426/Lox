package lox;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;



class Lox{

    public static boolean hadError = false;
    public static boolean hadRuntimeError = false;

    public static void main(String args[]) throws IOException {
        if(args.length > 2){
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 2) { 
            if(args[0].equals("-i") == true){
                run(args[1]);
                runPrompt();
            } else { 
                System.out.println("Usage: jlox [script]");
                System.exit(64);
            }
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }


    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        List<Statement> program = parser.parse(); 
        Interpreter interpreter = new Interpreter();
        Resolver resolver = new Resolver(interpreter);

        if(!hadError) {
            resolver.performResolution(program);
            if(!hadError) {
                try {
                    interpreter.interpret(program);
                } catch(InterpreterError error) {}
            }        
        }  
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        if(hadError) System.exit(65);
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        while(true) {
            System.out.print(":> ");
            String line = reader.readLine();
            if(line.equals("")) break;
            run(line);
            hadError = false;
            hadRuntimeError = false;
        }

    }

    public static void error(int line, String message) {
        report(line, "", message, 65);
    }

    public static void runtimeError(int line, String message) {
        report(line, "", message, 70); 
    }


    private static void report(int line, String where, String message, int errorIndex) {
        System.err.println("line [" + line + "]: Error " + where + ": " + message);
        
        switch(errorIndex) {
            case 65: 
                hadError = true;
                break;
            case 70:
                hadRuntimeError = true;
                break;
        }
        
    }






};
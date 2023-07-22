import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;





public class generateInheritanceStructure {

    private static int indentation = 0;

    public static void main(String args[]) throws IOException {
        if(args.length != 1)  {
            System.out.println("Usage: generateInheritanceStructure <output_file>");
            System.exit(64);
        } else {
            String output_file = args[0]; 
            generateStructure(output_file);
        }
    }

    private static void generateStructure(String output_file) throws IOException {
        FileWriter writer = new FileWriter(output_file); 
        String baseName = "Expression";
        List<String> types = new ArrayList<>(); 
        types.add("Grouping : Expression expr"); 
        types.add("Unary : Token operator, Expression expr");
        types.add("Binary : Expression left_expr, Token operator, Expression right_expr");
        types.add("Ternary : Expression first_expr, Token operator1_2, Expression sec_expr, Token operator2_3, Expression third_expr"); 
        types.add("Literal : Object value"); 
        writeStructure(writer, baseName, types); 
    }

    private static void writeStructure(FileWriter writer, String baseName, List<String> types) throws IOException {
        writer.write(getIndentation() + "abstract class " + baseName + " {\n");
        indentation += 1;
        writer.write(getIndentation() + "abstract <T> T accept(Visitor<T> visitor);\n");
        indentation -= 1;
        writer.write(getIndentation() + "}\n"); 

        writeVisitor(writer, types);



        for(String type: types) {
            String className = type.split(":")[0].trim(); 
            String dataMembers = type.split(":")[1].trim(); 
            writeClass(writer, className, baseName, dataMembers.split(", "));
        }



        writer.close(); 

    }

    private static void writeVisitor(FileWriter writer, List<String> types) throws IOException {
        writer.write(getIndentation() + "interface Visitor<T> { \n"); 
        indentation += 1;
        for(String type: types) {
            String className = type.split(":")[0].trim(); 
            writer.write(getIndentation() + "T visit" + className + "(" + className + " expression" + ");\n");
        }
        indentation -= 1;
        writer.write(getIndentation() + "}\n"); 
    }


    private static void writeClass(FileWriter writer, String className, String baseName, String[] dataMembers) throws IOException {
        writer.write("\n"); 
        writer.write(getIndentation() + "class " + className + " extends " + baseName + " {\n"); 
        indentation += 1; 
        for(String member: dataMembers) {
            writer.write(getIndentation() + "final " + member.trim() + ";\n");
        }
        writer.write("\n");


        String constructor = "";
        constructor = constructor + getIndentation() + className + "("; 
        for(int i = 0; i < dataMembers.length; i++) {
            constructor += dataMembers[i]; 
            if(i < dataMembers.length - 1) {
                constructor += ", ";
            } 
        }

        constructor += ") {\n"; 
        indentation += 1;
        for(String member: dataMembers) {
            constructor += getIndentation() + "this." + member.split(" ")[1] + " = " + member.split(" ")[1] + ";\n"; 
        }

        indentation -= 1;
        constructor += getIndentation() + "}\n";
        constructor += getIndentation() + "@Override\n";
        constructor += getIndentation() + "<T> T accept(Visitor<T> visitor) {\n";
        indentation += 1;
        constructor += getIndentation() + "return visitor.visit" + className + "(this);\n";
        indentation -= 1;
        constructor += getIndentation() + "}\n";
        indentation -= 1;
        constructor += getIndentation() + "}\n"; 
        writer.write(constructor); 

    }

    private static String getIndentation() {
        String indent = ""; 
        for(int i = 0 ; i < indentation; i++) {
            indent += "\t"; 
        }
        return indent;
    }

}

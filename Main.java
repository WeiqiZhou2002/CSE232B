import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.w3c.dom.Node;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Usage: java Main '<XPath query>'");
            System.err.println("Example: java Main 'doc(\"test.xml\")/library/book'");
            System.exit(1);
        }
        String query = args[0];

        CharStream input = CharStreams.fromString(query);
        XPathLexer lexer = new XPathLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        XPathParser parser = new XPathParser(tokens);
        ParseTree tree = parser.ap();

        XPathEngine engine = new XPathEngine();
        List<Node> result = engine.visit(tree);

        serialize(result);
    }

    private static void serialize(List<Node> nodes) throws Exception{
        if (nodes == null || nodes.isEmpty()) {
            System.out.println("(empty result)");
            return;
        }
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        for(Node n:nodes){
            switch (n.getNodeType()) {
                case Node.TEXT_NODE:
                    System.out.println(n.getTextContent());
                    break;
                case Node.ATTRIBUTE_NODE:
                    System.out.println("@" + n.getNodeName() + "=\"" + n.getNodeValue() + "\"");
                    break;
                case Node.DOCUMENT_NODE:
                    System.out.println("[document]");
                    break;
                default:
                    StringWriter sw = new StringWriter();
                    transformer.transform(new DOMSource(n), new StreamResult(sw));
                    System.out.println(sw.toString().trim());
            }
        }
    }
}

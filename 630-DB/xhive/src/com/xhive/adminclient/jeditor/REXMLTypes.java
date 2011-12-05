package com.xhive.adminclient.jeditor;

/**
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Regular expressions for syntax highlighting XML
 *
 * Based on work by Claude Duguay
 */

import com.xhive.util.jeditor.EditorDocument;
import com.xhive.util.jeditor.RETypes;

import javax.swing.text.Document;
import javax.swing.text.StyledEditorKit;
import java.awt.*;

public class REXMLTypes extends RETypes {

    private static final String TAG = "tag";
    private static final String TEXT = "text";
    private static final String COMMENT = "comment";
    private static final String PROCESSING_INSTRUCTION = "processing_instruction";
    private static final String CDATA = "cdata_section";

    private static final Color myGreen = new Color(10, 150, 10);
    private static final Color myOrange = new Color(255, 100, 10);

    public REXMLTypes() {
        addXMLTokenTypes();
    }

    /**
     * These are always used
     */
    private void addXMLTokenTypes() {
        addTokenType(COMMENT, "<!--.*?-->", myGreen);
        addTokenType(CDATA, "<!\\[CDATA\\[.*?\\]\\]>", myGreen);
        addTokenType(PROCESSING_INSTRUCTION, "<\\?.*?\\?>", myGreen);
        addTokenType(TAG, "<[^\\s>]*|>", Color.blue);
        //    addTokenType(TEXT, "\"(?:\\\\.|[^\"\\\\])*\"", Color.red);
    }

    /**
     * For XQuery only
     */
    protected void addXQueryTokenTypes() {
        // Override element constructor color
        addTokenType(TAG, "<[^\\s>]*|>", myOrange);

        addTokenType(TEXT, "\"[^\"]*\"", Color.red);
        addTokenType(TEXT + "2", "'[^']*'", Color.red);

        addTokenType("xquerycomment", "\\(:[^\\}]*:\\)", myGreen);
        addTokenType("for", "(?<!\\p{L})for(?=[\\s]*\\$)", Color.blue);
        addKeywordType("let");
        addKeywordType("where");
        addKeywordType("return");
        addKeywordType("in");
        addKeywordType("sort by");
        addKeywordType("order by");
        addKeywordType("define");
        addKeywordType("declare");
        addKeywordType("namespace");
        addKeywordType("function");
        addKeywordType("if");
        addKeywordType("then");
        addKeywordType("else");
        addKeywordType("item");
        addAxisType("child");
        addAxisType("descendant");
        addAxisType("ancestor");
        addAxisType("parent");
        addAxisType("following-sibling");
        addAxisType("preceding-sibling");
        addAxisType("following");
        addAxisType("preceding");
        addAxisType("attribute");
        addAxisType("self");
        addAxisType("descendent-or-self");
        addAxisType("ancestor-or-self");
        addTokenType("{", "\\{", Color.gray);
        addTokenType("}", "\\}", Color.gray);
        addTokenType("(", "\\(", Color.gray);
        addTokenType(")", "\\)", Color.gray);
    }

    private void addAxisType(String axis) {
        String regExp = axis + "(?=::)";
        addTokenType(axis, regExp, Color.blue);
    }

    private void addKeywordType(String keyword) {
        String regExp = "(?<![\\p{L}:\\[/])" + keyword + "(?![\\p{L}])";
        addTokenType(keyword, regExp, Color.blue);
    }

    public static class XhiveStyledEditorKit extends StyledEditorKit {
        EditorDocument defaultDocument;

        public EditorDocument getDefaultDocument() {
            return defaultDocument;
        }
    }


    public static class XMLKit extends XhiveStyledEditorKit {
        public Document createDefaultDocument() {
            defaultDocument = new EditorDocument(new REXMLTypes());
            return defaultDocument;
        }
    }

    public static class XQueryKit extends XhiveStyledEditorKit {
        public Document createDefaultDocument() {
            REXMLTypes types = new REXMLTypes();
            types.addXQueryTokenTypes();
            defaultDocument = new EditorDocument(types);
            return defaultDocument;
        }
    }
}


package samples.manual;

import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.Node;

/**
 *
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 *
 * [DESCRIPTION]
 * Very simple DOMErrorHandler implementation that simply prints errors reported
 * Used in the DOMValidation sample
 *
 */
public class SimpleDOMErrorPrinter implements DOMErrorHandler {

    public boolean handleError(DOMError error) {
        String message = error.getMessage();
        Node errorNode = error.getLocation().getRelatedNode();
        short errorType = error.getSeverity();

        // Print out error information
        switch (errorType) {
        case DOMError.SEVERITY_WARNING:
            System.out.print("-->Warning: ");
            break;
        case DOMError.SEVERITY_ERROR:
            System.out.print("-->Error: ");
            break;
        case DOMError.SEVERITY_FATAL_ERROR:
            System.out.print("-->Fatal Error: ");
            break;
        }
        System.out.println(message);

        return true;
    }

}

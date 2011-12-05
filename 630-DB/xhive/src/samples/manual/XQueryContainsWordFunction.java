package samples.manual;

import com.xhive.query.interfaces.XhiveXQueryValueIf;
import com.xhive.query.interfaces.XhiveXQueryExtensionFunctionIf;

import java.util.Iterator;

/**
 *
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 *
 * [DESCRIPTION]
 * This sample XQuery extension function definitions is used in XQueryExtensions
 *
 */
public class XQueryContainsWordFunction implements XhiveXQueryExtensionFunctionIf {

    public Object[] call(Iterator[] args) {
        boolean contains = false;
        String searchTerm = ((XhiveXQueryValueIf)args[1].next()).asString();
        Iterator iterator = args[0];
        while (iterator.hasNext() && (!contains)) {
            XhiveXQueryValueIf value = (XhiveXQueryValueIf)iterator.next();
            String stringValue = value.asString();
            if (stringValue.indexOf(searchTerm) != -1) {
                contains = true;
            }
        }
        return new Object[] { Boolean.valueOf(contains) };
    }
}

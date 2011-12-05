//*****************************************************************************************************
//
//	Project		: Association Mining using XQuery
//  Authors		: Balaji Ganesan (balaji*)
//				  Richard Nikhil Martin (rnmartin*)
//			 	  Satya Narayan Thiruvallur Selvakumar (satya*)
//	Date   		: October 12th 2005
//	Program		: Apriori
//	Description : This program when given a transaction xml generates the largeitemsets and stores
//				  them in the large.xml. It then generates the Association rules for them.
//
//*****************************************************************************************************

// Used only for the ease of compiling using xhive-ant provided with xhive.
package samples;

import java.util.Iterator;

import com.xhive.XhiveDriverFactory;
import com.xhive.core.interfaces.XhiveDatabaseIf;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.core.interfaces.XhiveDriverIf;
import com.xhive.dom.interfaces.XhiveDocumentIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;
import com.xhive.query.interfaces.XhiveXQueryValueIf;
import com.xhive.core.interfaces.XhiveFederationIf;

import org.w3c.dom.Node;
import java.io.*;


import org.w3c.dom.Document;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;

import java.io.File;
import java.lang.String;

/**
 * [DEPENDENCIES / PRE-REQUISITES]
 * - a database must be present, this database is generated
 *   in sample CreateDatabase.java
 *
 * [RUNNING]
 * When using ant, start sample with command:
 * xhive-ant run-sample -Dname=manual.XQuery
 *
 */

public class apriori {

private static BufferedReader stdin =
new BufferedReader( new InputStreamReader( System.in ) );

    public static void main(String[] args) {


		// Input code starts here

		// Default Values
		double minsup = 0.3;
		double minconf = 1.0;


		int read = 0;
		char c[] = new char[4096];
		PrintWriter pw = new PrintWriter(System.out, true);
		FileReader fr1 = null;
		FileReader fr2 = null;

		try
		{
			fr1 = new FileReader("../data/support.txt");
			System.out.println("The Value of support is");
		    read = fr1.read(c);
		    pw.write(c, 0, read);
			pw.flush();
		}
		catch(IOException io)
		{
			io.printStackTrace();
		}

		String sup = new String(c);
        minsup = Double.parseDouble(sup);

        try
        {
			fr2 = new FileReader("../data/confidence.txt");
			System.out.println("The Value of confidence is");
            read = fr2.read(c);
            pw.write(c,0,read);
			pw.flush();
	    }
	   	catch(IOException io)
	   	{
	   		io.printStackTrace();
		}

        String conf = new String(c);
        minconf = Double.parseDouble(conf);


		// Input code ends here

        // the name of the database
		String databaseName = "630";

		String superUserName = "superuser";
		String superUserPassword = "abc123";
		String administratorName = "Administrator";
		String administratorPassword = "northsea";

		File file = new File("../data/large.xml");
		PrintWriter myOut = null;
		try
		{
		  myOut = new PrintWriter(new FileOutputStream(file));
		} catch (IOException io) {
		  io.printStackTrace();
		}



		// create a session
		XhiveDriverIf driver = XhiveDriverFactory.getDriver();
		driver.init(1024);
		XhiveSessionIf session = driver.createSession();


		try {

					// Only the superuser can create databases
					// The databasename parameter should be null, or empty
					session.connect(superUserName, superUserPassword, null);

					// begin the trans
					session.begin();


					// The federation object is used to manage databases
					XhiveFederationIf federation = session.getFederation();

					if (federation.hasDatabase(databaseName)) {
						System.out.println("\n#Database already exists: " + databaseName);
						federation.deleteDatabase(databaseName);
						System.out.println("\n#Deleting Database: " + databaseName);
					}


					System.out.println("\n#Create new database with name " + databaseName);

					// create the database
					// (null configuration file (means default configuration),
					// print debug output to console)
					federation.createDatabase(databaseName, administratorPassword, null, System.out);


					session.commit();

				} catch (Exception e) {

					System.out.println("CreateDatabase sample failed: ");
					e.printStackTrace();

				} finally {

					// disconnect and remove the session
					if (session.isOpen()) {
						session.rollback();
					}
					if (session.isConnected()) {
						session.disconnect();
					}

		}


        String fileName = "../data/transactions.xml";

		try {

				// open a connection to the database
				session.connect(administratorName, administratorPassword, databaseName);


				// begin the transaction
				session.begin();

				// get a handle to the database
				XhiveDatabaseIf united_nations_db = session.getDatabase();

				// get a handle to the root library
				XhiveLibraryIf rootLibrary = united_nations_db.getRoot();


				//create a DOMBuilder
				LSParser builder = rootLibrary.createLSParser();


				// parse a new document
				Document firstDocument = builder.parseURI(new File(fileName).toURL().toString());

				String firstDocumentName = "transactions";


				// if it doesn't exist yet: store it
				if (!(rootLibrary.nameExists(firstDocumentName))) {
					// add the new document to the "UN Charter" library
					rootLibrary.appendChild(firstDocument);


					// give the new document a name
					((XhiveDocumentIf) firstDocument).setName(firstDocumentName);
				} else {
					firstDocument = (Document) rootLibrary.get(firstDocumentName);

				}




				session.commit();

			} catch (Exception e) {

				System.out.println("StoreDocuments sample failed: ");
				e.printStackTrace();

			} finally {

				// disconnect and remove the session
				if (session.isOpen()) {
					session.rollback();
				}
				if (session.isConnected()) {
					session.disconnect();
				}

			}
        try {

            // open a connection to the database
            session.connect(administratorName, administratorPassword, databaseName);

            // begin the transaction
            session.begin();

            // get a handle to the database
            XhiveDatabaseIf united_nations_db = session.getDatabase();

            // get a handle to the root library
            XhiveLibraryIf rootLibrary = united_nations_db.getRoot();


			String userFunctions = "declare function getLargeItemsets($C as element(),$minsup as element(),$total as element(),$src as element()) as element()* \n { \n \t for $items in $C \n \t let $trans := (for $tran in $src \n \t \t where every $item1 in $items/* satisfies \n \t \t \t some $item2 in $tran/* \n \t \t \t satisfies $item1 = $item2 \n \t \t return $tran) \n \t let $sup := (count($trans) * 1.00) div $total \n \t where $sup >= $minsup \n \t return <largeItemset> {$items} \n \t \t <support> {$sup} </support> \n \t </largeItemset> \n }; \n \n declare function join($X as element(),$Y as element()) as element()* \n { \n let $items := ( \n \t for $item in $Y \n \t \t where every $i in $X satisfies \n \t \t \t $i != $item \n \t \t return $item) \n \t return $X union $items \n }; \n \n declare function commonIts($X as element(),$Y as element()) as element()* \n { \n \t for $item in $X \n \t where some $i in $Y satisfies $i = $item \n \t return $item \n }; \n \n declare function removeIts($X as element(),$Y as  element()) as element()* \n { \n \t for $item in $X \n \t where every $i in $Y satisfies $i != $item \n \t return $item \n }; \n \n declare function candidateGen($l as element()) as element()* \n { \n \t for $freqSet1 in $l \n \t let $items1 := $freqSet1//items/* \n \t \t for $freqSet2 in $l \n \t \t let $items2 := $freqSet2//items/* \n \t \t where $freqSet2 >> $freqSet1 and \n \t \t \t count($items1)+1 = \n \t \t \t \t count(join($items1,$items2)) \n \t \t \t and prune(join($items1,$items2),$l) \n \t return <items> \n \t \t \t {join($items1,$items2)} \n \t \t </items> \n }; \n \n declare function prune($X as element(),$Y as element()) as xs:boolean \n { \n \t every $item in $X satisfies \n \t some $items in $Y//items satisfies \n \t count(commonIts(removeIts($X,$item),$items/*)) \n \t = count($X) - 1 \n}; \n \n declare function removeDuplicate($C as element()) as element()* \n { \n \t for $itemset1 in $C \n \t let $items1 := $itemset1/* \n \t let $items :=(for $itemset2 in $C \n \t \t let $items2 := $itemset2/* \n \t \t where $itemset2>>$itemset1 and \n \t \t count($items1) = \n \t \t count(commonIts($items1, $items2)) \n \t \t return $items2) \n \t where count($items) = 0 \n \t \t return $itemset1 \n}; \n \n declare function apriori($l as element() ,$L as element(), $minsup as element(), $total as element(), $src as element()) as element()* \n { \n let $C := removeDuplicate(candidateGen($l)) \n let $l := getLargeItemsets($C, $minsup, $total, $src) \n let $L := $l union $L \n return \t if (empty($l)) then \n \t $L \n else \n \t apriori($l, $L, $minsup, $total, $src) \n }; \n \n let $src := document('/')//transactions/transaction/items \n let $minsup := xs:double(" + minsup + ") \n let $total := xs:double(count($src) * 1.00) \n let $C := distinct-values($src/*) \n let $l :=(for $itemset in $C \n \t let $items := (for $item in $src/* \n \t \t where $itemset = $item \n \t \t return $item) \n \t let $sup := (count($items) * 1.00) div $total \n \t where $sup >= $minsup \n \t return <largeItemset> \n \t \t <items> <item> {$itemset} </item> </items> \n \t \t <support> {$sup} </support> \n \t \t </largeItemset>) \n let $L := $l \n return <largeItemsets> { apriori($l, $L,$minsup, $total, $src) } \n \t </largeItemsets>";


			System.out.println("\nExecuting XQuery fucntion for generating Large Itemsets:\n\n");
//			System.out.println("#calling function :\n" + userFunctions + "\n\n\n");
			Iterator result = rootLibrary.executeXQuery(userFunctions);
			System.out.println("Large Itemsets: \n\n\n");


            if (myOut != null)
            {
            	// Process the results
            	while (result.hasNext())
            	{
					// Get the next value from the result sequence
					XhiveXQueryValueIf value = (XhiveXQueryValueIf)result.next();

					// Print this value
					System.out.println(value.toString());
					myOut.println(value.toString());
					myOut.flush();
            	}
            }
            myOut.close();


            session.commit();

        } catch (Exception e) {

            System.err.println("MyXQuery sample failed: ");
            e.printStackTrace();

        } finally {

            // disconnect and remove the session
            if (session.isOpen()) {
                session.rollback();
            }
            if (session.isConnected()) {
                session.disconnect();
            }

            //driver.close();
        }

		String fileName1 = "../data/large.xml";

		try {

				// open a connection to the database
				session.connect(administratorName, administratorPassword, databaseName);


				// begin the transaction
				session.begin();

				// get a handle to the database
				XhiveDatabaseIf united_nations_db = session.getDatabase();

				// get a handle to the root library
				XhiveLibraryIf rootLibrary = united_nations_db.getRoot();


				//create a DOMBuilder
				LSParser builder = rootLibrary.createLSParser();

				// parse a new document
				Document firstDocument = builder.parseURI(new File(fileName1).toURL().toString());

				String firstDocumentName = "large";

				// if it doesn't exist yet: store it
				if (!(rootLibrary.nameExists(firstDocumentName))) {
					// add the new document to the "UN Charter" library
					rootLibrary.appendChild(firstDocument);


					// give the new document a name
					((XhiveDocumentIf) firstDocument).setName(firstDocumentName);
				} else {
					firstDocument = (Document) rootLibrary.get(firstDocumentName);

				}



				session.commit();

			} catch (Exception e) {

				System.out.println("StoreDocuments sample failed: ");
				e.printStackTrace();

			} finally {

				// disconnect and remove the session
				if (session.isOpen()) {
					session.rollback();
				}
				if (session.isConnected()) {
					session.disconnect();
				}

				//driver.close();
        }

		try {

				// open a connection to the database
				session.connect(administratorName, administratorPassword, databaseName);

				// begin the transaction
				session.begin();

				// get a handle to the database
				XhiveDatabaseIf united_nations_db = session.getDatabase();

				// get a handle to the root library
				XhiveLibraryIf rootLibrary = united_nations_db.getRoot();


				String userFunctions1 = "declare function commonIts($X as element(),$Y as element()) as element()* \n { \n \t for $item in $X \n \t where some $i in $Y satisfies $i = $item \n \t return $item \n }; \n \n declare function removeIts($X as element(),$Y as  element()) as element()* \n { \n \t for $item in $X \n \t where every $i in $Y satisfies $i != $item \n \t return $item \n }; \n \n let $minconf := xs:double(" + minconf + ") \n let $src := document('/')//largeItemsets/largeItemset \n for $itemset1 in $src \n let $items1 := $itemset1/items/* \n \t for $itemset2 in $src \n \t let $items2 := $itemset2/items/* \n \t where count($items1) > count($items2) and \n \t \t count(commonIts($items1, $items2)) = \n \t \t count($items2) and $itemset1/support div \n \t \t  $itemset2/support >= $minconf \n \t return <rule support = '{$itemset1/support}' \n \t  confidence = '{($itemset1/support*1.0) div \n \t \t \t ($itemset2/support*1.0)}'> \n \t \t <antecedent> {$items2} </antecedent> \n \t \t  <consequent> \n \t \t \t {removeIts($items1,$items2)} \n \t \t </consequent> \n \t </rule>";


				System.out.println("\nExecuting XQuery fucntion for generating Association Rules:\n\n");
				Iterator result1 = rootLibrary.executeXQuery(userFunctions1);
				//System.out.println("#calling function :\n" + userFunctions1 + "\n\n\n");
				System.out.println("Association Rules \n\n\n");



				// Process the results
				while (result1.hasNext())
				{
					// Get the next value from the result sequence
					XhiveXQueryValueIf value1 = (XhiveXQueryValueIf)result1.next();

					// Print this value
					System.out.println(value1.toString());
				}


				session.commit();

			} catch (Exception e) {

				System.err.println("MyXQuery sample failed: ");
				e.printStackTrace();

			} finally {

				// disconnect and remove the session
				if (session.isOpen()) {
					session.rollback();
				}
				if (session.isConnected()) {
					session.disconnect();
				}

				driver.close();
			}

    }
}

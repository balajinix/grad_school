package samples.manual;

/**
 *
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 *
 * [DESCRIPTION]
 * This class defines the properties used in the samples
 *
 */
public class SampleProperties {

    // the name and password of the superuser of the federation
    public static String superuserName = "superuser";
    public static String superuserPassword = "northsea";

    // the name of the database
    public static String databaseName = "united_nations";

    // the name and password of the administrator of the database
    public static String administratorName = "Administrator";
    public static String administratorPassword = "northsea";

    // the name of the files to store
    public static String baseDir = "../src/samples/data/manual/";

    // the name of the files to store
    public static String baseFileName = baseDir + "un_charter_chapter";

    // the number of files parsed
    public static int numFiles = 19;
}

/*

   Derby - Class org.apache.derbyTesting.functionTests.tests.lang.NativeAuthenticationServiceTest

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to you under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

package org.apache.derbyTesting.functionTests.tests.lang;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.Properties;
import javax.sql.DataSource;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.derbyTesting.junit.ClasspathSetup;
import org.apache.derbyTesting.junit.DatabaseChangeSetup;
import org.apache.derbyTesting.junit.JDBC;
import org.apache.derbyTesting.junit.JDBCDataSource;
import org.apache.derbyTesting.junit.SecurityManagerSetup;
import org.apache.derbyTesting.junit.SupportFilesSetup;
import org.apache.derbyTesting.junit.SystemPropertyTestSetup;
import org.apache.derbyTesting.junit.TestConfiguration;

/**
 * <p>
 * Tests for the NATIVE authentication service introduced by DERBY-866.
 * </p>
 */
public class NativeAuthenticationServiceTest extends GeneratedColumnsHelper
{
    ///////////////////////////////////////////////////////////////////////////////////
    //
    // CONSTANTS
    //
    ///////////////////////////////////////////////////////////////////////////////////

    // location of credentials db
    private static  final   int FILE = 0;
    private static  final   int JAR = 1;
    private static  final   int CLASSPATH = 2;
    private static  final   int NONE = 3;

    // settings for constructor options
    private static  final   boolean NATIVE = true;
    private static  final   boolean NO_AUTH = false;
    
    private static  final   boolean LOCAL = true;
    private static  final   boolean SYSTEM_WIDE = false;
    
    private static  final   boolean DISABLE_AUTHORIZATION = true;
    private static  final   boolean DONT_DISABLE_AUTH = false;
    
    private static  final   boolean DISABLE_JAVA_SECURITY = true;
    private static  final   boolean ENABLE_JAVA_SECURITY = false;
    
    // fruits are legal users. nuts are not
    private static  final   String  DBO = "KIWI";   
    private static  final   String  APPLE_USER = "APPLE";   
    private static  final   String  PEAR_USER = "PEAR";   
    private static  final   String  ORANGE_USER = "ORANGE";   
    private static  final   String  BANANA_USER = "BANANA";   
    private static  final   String  GRAPE_USER = "GRAPE";   

    private static  final   String  WALNUT_USER = "WALNUT";

    private static  final   String  BUILTIN_USER = "PECAN";

    private static  final   String  CREDENTIALS_DB = "credDB";
    private static  final   String  SECOND_DB = "secondDB";
    private static  final   String  THIRD_DB = "thirdDB";
    private static  final   String  FOURTH_DB = "fourthDB";
    private static  final   String  FIFTH_DB = "fifthDB";
    private static  final   String  SIXTH_DB = "sixthDB";
    private static  final   String  SEVENTH_DB = "seventhDB";
    private static  final   String  EIGHTH_DB = "eighthDB";
    private static  final   String  NINTH_DB = "ninthDB";
    private static  final   String  TENTH_DB = "tenthDB";
    private static  final   String  ELEVENTH_DB = "eleventhDB";

    private static  final   String  NAST1_JAR_FILE = "nast1.jar";
    private static  final   String  NAST2_JAR_FILE = "nast2.jar";
    private static  final   String[]    SUPPORT_FILES_SOURCE =
    {
        "functionTests/tests/lang/" + NAST1_JAR_FILE,
        "functionTests/tests/lang/" + NAST1_JAR_FILE,
    };
    private static  final   String[]    SUPPORT_FILES_TARGET =
    {
        NAST1_JAR_FILE,
        NAST2_JAR_FILE,
    };

    private static  final   String  PROVIDER_PROPERTY = "derby.authentication.provider";
    private static  final   String  REQUIRE_AUTHENTICATION_PROPERTY = "derby.connection.requireAuthentication";
    private static  final   String  SQL_AUTHORIZATION_PROPERTY = "derby.database.sqlAuthorization";

    private static  final   String  CREDENTIALS_DB_DOES_NOT_EXIST = "4251I";
    private static  final   String  INVALID_AUTHENTICATION = "08004";
    private static  final   String  DBO_ONLY_OPERATION = "4251D";
    private static  final   String  INVALID_PROVIDER_CHANGE = "XCY05";
    private static  final   String  CANT_DROP_DBO = "4251F";
    private static  final   String  NO_COLUMN_PERMISSION = "42502";
    private static  final   String  PASSWORD_EXPIRING = "01J15";
    private static  final   String  BAD_PASSWORD_PROPERTY = "4251J";
    private static  final   String  BAD_PROPERTY_CHANGE = "XCY02";

    ///////////////////////////////////////////////////////////////////////////////////
    //
    // STATE
    //
    ///////////////////////////////////////////////////////////////////////////////////

    private final   int         _credentialsDBLocation;
    private final   boolean _nativeAuthentication;
    private final   boolean _localAuthentication;
    private final   boolean _turnOffAuthenticationAndAuthorization;
    private final   boolean _disableSecurityManager;

    private String  _credentialsDBPhysicalName;

    private DatabaseChangeSetup _fourthDBSetup;
    private DatabaseChangeSetup _fifthDBSetup;
    private DatabaseChangeSetup _sixthDBSetup;
    private DatabaseChangeSetup _seventhDBSetup;
    private DatabaseChangeSetup _eighthDBSetup;
    private DatabaseChangeSetup _ninthDBSetup;

    private String  _derbySystemHome;
    private String  _fullBackupDir;

    ///////////////////////////////////////////////////////////////////////////////////
    //
    // CONSTRUCTOR
    //
    ///////////////////////////////////////////////////////////////////////////////////

    public  NativeAuthenticationServiceTest
        (
         int            credentialsDBLocation,
         boolean    nativeAuthentication,
         boolean    localAuthentication,
         boolean    turnOffAuthenticationAndAuthorization,
         boolean    disableSecurityManager
         )
    {
        super( "testAll" );

        _credentialsDBLocation = credentialsDBLocation;
        _nativeAuthentication = nativeAuthentication;
        _localAuthentication = localAuthentication;
        _turnOffAuthenticationAndAuthorization = turnOffAuthenticationAndAuthorization;
        _disableSecurityManager = disableSecurityManager;
    }

    ///////////////////////////////////////////////////////////////////////////////////
    //
    // SETUP BEHAVIOR
    //
    ///////////////////////////////////////////////////////////////////////////////////

    public void setUp() throws Exception
    {
        super.setUp();
        
        _derbySystemHome = getSystemProperty( "derby.system.home" );
        _fullBackupDir = _derbySystemHome + "/backupDir";
    }

    /**
     * <p>
     * Return the system properties to be used in a particular test run.
     * </p>
     */
    private Properties  systemProperties( String physicalDatabaseName )
        throws  Exception
    {
        Properties  result = new Properties();
        String      authenticationProvider;

        switch ( _credentialsDBLocation )
        {
            case JAR:
                _credentialsDBPhysicalName = jarDBName();
                break;

            case CLASSPATH:
                _credentialsDBPhysicalName = classpathDBName();
                break;

            case FILE:
            case NONE:
                _credentialsDBPhysicalName = physicalDatabaseName;
                break;
                
            default:
                throw new Exception( "Unknown location of credentials db: " + _credentialsDBLocation );
        }

        if ( !_nativeAuthentication )
        {
            authenticationProvider = "NONE";
        }
        else
        {
            authenticationProvider = "NATIVE:" + _credentialsDBPhysicalName;
            if ( _localAuthentication ) { authenticationProvider = authenticationProvider + ":LOCAL"; }
        }

        result.put( PROVIDER_PROPERTY, authenticationProvider );

        //
        // This is to verify that NATIVE authentication trumps attempts
        // to disable these features at the system level.
        //
        if ( _turnOffAuthenticationAndAuthorization )
        {
            result.put( REQUIRE_AUTHENTICATION_PROPERTY, "false" );
            result.put( SQL_AUTHORIZATION_PROPERTY, "false" );
        }

        return result;
    }

    /**
     * <p>
     * Return true if the credentials db is accessed via a jar or classpath subprotocol.
     * </p>
     */
    private boolean credentialsViaSubprotocol()
    {
        switch ( _credentialsDBLocation )
        {
            case JAR:
            case CLASSPATH:
                return true;

            default:
                return false;
        }
    }

    /**
     * <p>
     * Construct the name of this test (useful for error messages).
     * </p>
     */
    private String  nameOfTest()
    {
        String  dbLocation = "";
        switch ( _credentialsDBLocation )
        {
            case JAR:
                dbLocation = "JAR, ";
                break;

            case CLASSPATH:
                dbLocation = "CLASSPATH, ";
                break;

            case FILE:
            case NONE:
                dbLocation = "FILE, ";
                break;
                
            default:
                dbLocation = "UNKNOWN, ";
                break;
        }

        
        String  authType = _nativeAuthentication ?
            "NATIVE authentication on, " :
            "Authentication off, ";
        String  local = _localAuthentication ?
            "LOCAL authentication ON, " :
            "LOCAL authentication OFF, ";
        String  authOverrides = _turnOffAuthenticationAndAuthorization ?
            "Authentication/Authorization turned OFF, " :
            "Authentication/Authorization DEFAULT, ";
        String  securityManager = _disableSecurityManager ?
            "SecurityManager OFF, " :
            "SecurityManager ON, ";
        String  embedded = isEmbedded() ?
            "Embedded" :
            "Client/Server";

        return "[ " + dbLocation + authType + local + authOverrides + securityManager + embedded + " ]";
    }

    /** Return true if the test is running embedded */
    public  boolean isEmbedded() { return getTestConfiguration().getJDBCClient().isEmbedded(); }
    
    ///////////////////////////////////////////////////////////////////////////////////
    //
    // JUnit BEHAVIOR
    //
    ///////////////////////////////////////////////////////////////////////////////////


    /**
     * Construct top level suite in this JUnit test
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite();

        //
        // Special version of the test which disables the security manager in order
        // to use the classpath subprotocol. We may be able to remove this special
        // case after DERBY-5615 is fixed.
        //
        suite.addTest
            (
             (
              new NativeAuthenticationServiceTest
              ( NONE, NO_AUTH, SYSTEM_WIDE, DONT_DISABLE_AUTH, DISABLE_JAVA_SECURITY )
              ).decorate( false )
             );
        
        suite.addTest( allConfigurations( false ) );
        if ( !JDBC.vmSupportsJSR169() ) { suite.addTest( allConfigurations( true ) ); }

        return suite;
    }
    private static  boolean onWindows()
    {
        return getSystemProperty("os.name").startsWith("Windows");
    }

    /**
     * <p>
     * Create a suite of all test configurations.
     * </p>
     */
    private static  Test   allConfigurations( boolean clientServer )
    {
        TestSuite suite = new TestSuite();

        //
        // No authentication
        //
        suite.addTest
            (
             (
              new NativeAuthenticationServiceTest
              ( NONE, NO_AUTH, SYSTEM_WIDE, DONT_DISABLE_AUTH, ENABLE_JAVA_SECURITY )
              ).decorate( clientServer )
             );

        //
        // NATIVE/LOCAL authentication with credentials in read/write dbs
        //
        suite.addTest
            (
             (
              new NativeAuthenticationServiceTest
              ( FILE, NATIVE, LOCAL, DISABLE_AUTHORIZATION, ENABLE_JAVA_SECURITY )
              ).decorate( clientServer )
             );
        suite.addTest
            (
             (
              new NativeAuthenticationServiceTest
              ( FILE, NATIVE, LOCAL, DONT_DISABLE_AUTH, ENABLE_JAVA_SECURITY )
              ).decorate( clientServer )
             );

        //
        // NATIVE system-wide authentication with credentials in read/write dbs
        //
        suite.addTest
            (
             (
              new NativeAuthenticationServiceTest
              ( FILE, NATIVE, SYSTEM_WIDE, DISABLE_AUTHORIZATION, ENABLE_JAVA_SECURITY )
              ).decorate( clientServer )
             );
        suite.addTest
            (
             (
              new NativeAuthenticationServiceTest
              ( FILE, NATIVE, SYSTEM_WIDE, DONT_DISABLE_AUTH, ENABLE_JAVA_SECURITY )
              ).decorate( clientServer )
             );
        
        //
        // For testing subprotocols. Cleanup of support files is blocked by DERBY-5618.
        //
        if ( !onWindows() )
        {
            //
            // NATIVE authentication with credentials in read-only databases accessed via jar subprotocol
            //
            suite.addTest
                (
                 (
                  new NativeAuthenticationServiceTest
                  ( JAR, NATIVE, SYSTEM_WIDE, DONT_DISABLE_AUTH, ENABLE_JAVA_SECURITY )
                  ).decorate( clientServer )
                 );
            suite.addTest
                (
                 (
                  new NativeAuthenticationServiceTest
                  ( JAR, NATIVE, LOCAL, DONT_DISABLE_AUTH, ENABLE_JAVA_SECURITY )
                  ).decorate( clientServer )
                 );

            //
            // NATIVE authentication with credentials in read-only databases accessed via classpath subprotocol
            //
            suite.addTest
                (
                 (
                  new NativeAuthenticationServiceTest
                  ( CLASSPATH, NATIVE, SYSTEM_WIDE, DONT_DISABLE_AUTH, DISABLE_JAVA_SECURITY )
                  ).decorate( clientServer )
                 );
            suite.addTest
                (
                 (
                  new NativeAuthenticationServiceTest
                  ( CLASSPATH, NATIVE, LOCAL, DONT_DISABLE_AUTH, DISABLE_JAVA_SECURITY )
                  ).decorate( clientServer )
                 );
        }   // end if !onWindows()

        return suite;
    }

    /**
     * <p>
     * Wrap base test with standard decorators in order to setup system
     * properties and allow for the creation of multiple databases with
     * stored properties that can't be removed at tearDown time.
     * </p>
     */
    private Test    decorate( boolean clientServer )
    {
        String      credentialsDBPhysicalName = TestConfiguration.generateUniqueDatabaseName();
        
        Test        result = this;

        if ( _disableSecurityManager ) { result = SecurityManagerSetup.noSecurityManager( result ); }

        //
        // Putting the clientServer decorator on the inside allows the server-side
        // embedded driver to be re-registered after engine shutdown. If you put
        // this decorator outside the SystemProperty decorator, then engine shutdown
        // unregisters the server-side embedded driver and it can't be found by
        // the next test.
        //
        if ( clientServer ) { result = TestConfiguration.clientServerDecorator( result ); }
        
        //
        // Turn on the property which enables NATIVE authentication. This will trigger
        // an engine shutdown at the end of the test. We want to shutdown the engine
        // before deleting the physical databases. This is because we need one of the
        // databases (the credentials db) in order to authenticate engine shutdown.
        //
        Properties  systemProperties = null;
        try {
            systemProperties = systemProperties( credentialsDBPhysicalName );
        } catch (Exception e) { printStackTrace( e ); }

        println( nameOfTest() );
        println( "    NativeAuthenticationServiceTest.decorate() systemProperties = " + systemProperties );
        result = new SystemPropertyTestSetup( result, systemProperties, true );
        
        // DERBY-5580: We should also shut down the engine before deleting
        // the database if we don't set any system properties.
        //result = new TestSetup(result) {
        //        protected void tearDown() {
        //            TestConfiguration.getCurrent().shutdownEngine();
        //        }
        //    };
        
        //
        // For testing subprotocols. Cleanup of support files is blocked by DERBY-5618.
        //
        if ( !onWindows() )
        {
            // Add the jar files needed for testing jar and classpath subprotocols.
            result = new SupportFilesSetup( result, SUPPORT_FILES_SOURCE, null, SUPPORT_FILES_TARGET, null );

            //
            // Add a jar file to the classpath so that we can test the classpath subprotocol.
            //
            File        currentDirectory = currentDirectory();
            File        readOnlyDirectory = new File( currentDirectory, SupportFilesSetup.EXTIN );
            File        nast2Jar = new File( readOnlyDirectory, NAST2_JAR_FILE );

            try {
                result = new ClasspathSetup( result, nast2Jar.toURL() );
            }
            catch (Exception e) { printStackTrace( e ); }
        }
        
        //
        // Register temporary databases, where the test will do its work.
        // We can't use the default, re-usable database because NATIVE authentication stores
        // persistent properties which cannot be turned off.
        //
        result = TestConfiguration.additionalDatabaseDecoratorNoShutdown
            ( result, CREDENTIALS_DB, credentialsDBPhysicalName );
        result = TestConfiguration.additionalDatabaseDecoratorNoShutdown( result, SECOND_DB );
        result = TestConfiguration.additionalDatabaseDecoratorNoShutdown( result, THIRD_DB );
        result = _fourthDBSetup = TestConfiguration.additionalDatabaseDecoratorNoShutdown( result, FOURTH_DB, true );
        result = _fifthDBSetup = TestConfiguration.additionalDatabaseDecoratorNoShutdown( result, FIFTH_DB, true );
        result = _sixthDBSetup = TestConfiguration.additionalDatabaseDecoratorNoShutdown( result, SIXTH_DB, true );
        result = _seventhDBSetup = TestConfiguration.additionalDatabaseDecoratorNoShutdown( result, SEVENTH_DB, true );
        result = _eighthDBSetup = TestConfiguration.additionalDatabaseDecoratorNoShutdown( result, EIGHTH_DB, true );
        result = _ninthDBSetup = TestConfiguration.additionalDatabaseDecoratorNoShutdown( result, NINTH_DB, true );
        result = TestConfiguration.additionalDatabaseDecoratorNoShutdown( result, TENTH_DB );
        result = TestConfiguration.additionalDatabaseDecoratorNoShutdown( result, ELEVENTH_DB );

        result = TestConfiguration.changeUserDecorator( result, DBO, getPassword( DBO ) );
        
        return result;
    }
    
    ///////////////////////////////////////////////////////////////////////////////////
    //
    // TESTS
    //
    ///////////////////////////////////////////////////////////////////////////////////

    /**
     * <p>
     * Entry point for tests.
     * </p>
     */
    public  void    testAll()   throws Exception
    {
        println( nameOfTest() );
        println( "Credentials DB physical name = " + _credentialsDBPhysicalName );
        println( PROVIDER_PROPERTY + " = " + getSystemProperty( PROVIDER_PROPERTY ) );

        if ( credentialsViaSubprotocol() )
        {
            vetCredentialsViaSubprotocol();
        }
        else
        {
            vetCoreBehavior();
            vetSystemWideOperations();

            if ( !_nativeAuthentication ) { vetProviderChanges(); }

            // only run this for local authentication so that we don't have to shutdown
            // the system-wide credentials db. also only run this embedded so that we
            // don't have to deal with the problems of shutting down a database
            // across the network.
            if ( _localAuthentication && isEmbedded() ) { vetPasswordLifetime(); }
        }
    }

    /**
     * <p>
     * Verify that credentials work when they are stored in a db accessed via
     * the jar or classpath subprotocols.
     * </p>
     */
    private void    vetCredentialsViaSubprotocol()   throws Exception
    {
        // create a new database
        Connection  grapeConn = openConnection( TENTH_DB, GRAPE_USER, true );
        String[][]  legalUsers = _localAuthentication ?
            new String[][] { { GRAPE_USER } } : new String[][] {};
        assertResults
            (
             grapeConn,
             "select username from sys.sysusers order by username",
             legalUsers,
             false
             );

        // Databases can't be created by users who don't have credentials stored in the credentials database
        Connection  walnutConn = getConnection
            ( true, true, ELEVENTH_DB, WALNUT_USER, INVALID_AUTHENTICATION );
    }
    
    /**
     * <p>
     * Verify the core behavior of NATIVE authentication.
     * </p>
     */
    private void    vetCoreBehavior()   throws Exception
    {
        // can't create any database until the credentials db has been created
        Connection  secondDBConn = getConnection
            ( _nativeAuthentication, true, SECOND_DB, APPLE_USER, CREDENTIALS_DB_DOES_NOT_EXIST );

        // create the credentials database
        Connection  sysadminConn = openConnection( CREDENTIALS_DB, DBO, true );

        // add another legal user
        addUser( sysadminConn, APPLE_USER );
        addUser( sysadminConn, BANANA_USER );

        //
        // Creating the credentials db should have stored the following information in it:
        //
        // 1) The DBO's credentials should have been stored in SYSUSERS.
        // 2) The authentication provider should have been set to NATIVE::LOCAL
        //
        String[][]  legalUsers = _nativeAuthentication ?
            new String[][] { { APPLE_USER }, { BANANA_USER } , { DBO } } :
            new String[][] {  { APPLE_USER }, { BANANA_USER } };
        assertResults
            (
             sysadminConn,
             "select username from sys.sysusers order by username",
             legalUsers,
             false
             );
        String[][]  authenticationProvider = _nativeAuthentication ? new String[][] { { "NATIVE::LOCAL" } } : new String[][] { { null } };
        assertResults
            (
             sysadminConn,
             "values ( syscs_util.syscs_get_database_property( 'derby.authentication.provider' ) )",
             authenticationProvider,
             false
             );

        // there should be no need to explicitly set the sql authorization property
        String[][]  sqlAuthorization = new String[][] { { null } };
        assertResults
            (
             sysadminConn,
             "values ( syscs_util.syscs_get_database_property( 'derby.database.sqlAuthorization' ) )",
             sqlAuthorization,
             false
             );
        vetSQLAuthorizationOn();

        // Sanity-check that the creator of the credentials db is the DBO
        String[][]   dboName = new String[][] { { DBO } };
        assertResults
            (
             sysadminConn,
             "select authorizationID from sys.sysschemas where schemaName = 'SYS'",
             dboName,
             false
             );

        // Databases can't be created by users who don't have credentials stored in the credentials database
        Connection  thirdDBConn = getConnection
            ( _nativeAuthentication, true, THIRD_DB, WALNUT_USER, INVALID_AUTHENTICATION );

        // Now let the other valid user create a database
        if ( secondDBConn == null )
        {
            secondDBConn = getConnection( false, true, SECOND_DB, APPLE_USER, null );
        }

        // verify that the other valid user is the dbo in the database he just created
        assertResults
            (
             secondDBConn,
             "select authorizationID from sys.sysschemas where schemaName = 'SYS'",
             new String[][] { { APPLE_USER } },
             false
             );

        // NATIVE authentication turns on SQL authorization in the second database
        assertResults
            (
             secondDBConn,
             "values ( syscs_util.syscs_get_database_property( 'derby.database.sqlAuthorization' ) )",
             sqlAuthorization,
             false
             );

        //
        // If LOCAL authentication was specified...
        //
        // 1) It will be turned on in the second database too.
        // 2) The other legal user's credentials (as the database dbo) will be stored.
        //
        authenticationProvider = _localAuthentication ? new String[][] { { "NATIVE::LOCAL" } } : new String[][] { { null } };
        assertResults
            (
             secondDBConn,
             "values ( syscs_util.syscs_get_database_property( 'derby.authentication.provider' ) )",
             authenticationProvider,
             false
             );
        legalUsers = _localAuthentication ? new String[][] { { APPLE_USER } } : new String[][] {};
        assertResults
            (
             secondDBConn,
             "select username from sys.sysusers order by username",
             legalUsers,
             false
             );

        ///////////////////////////////////////////////////////////////////////////////////
        //
        // You can't turn off authentication or SQL authorization when NATIVE authentication
        // is set in the database. If NATIVE authentication is set at the system level,
        // you can still override it with database only properties.
        //
        ///////////////////////////////////////////////////////////////////////////////////

        Connection  seventhDBOConn = openConnection( SEVENTH_DB, DBO, true );

        addUser( seventhDBOConn, APPLE_USER );
        goodStatement( seventhDBOConn, "create table t1( a int )" );
        goodStatement( seventhDBOConn, "insert into t1( a ) values ( 10 )" );

        // at this point, you can't even force SQL authorization off on disk
        setDatabaseProperty
            ( _nativeAuthentication, seventhDBOConn, "derby.database.sqlAuthorization", "false", BAD_PROPERTY_CHANGE );

        // you can change these properties, but the change will be overridden by NATIVE authentication
        goodStatement
            ( seventhDBOConn, "call syscs_util.syscs_set_database_property( 'derby.connection.requireAuthentication', 'false' )" );
        goodStatement
            ( seventhDBOConn, "call syscs_util.syscs_set_database_property( 'derby.database.propertiesOnly', 'true' )" );

        //  now bring down the database so that the new property settings take effect
        _seventhDBSetup.getTestConfiguration().shutdownDatabase();

        // if NATIVE authentication is set in the database, then sql authorization prevents this legal user from viewing private data
        Connection  seventhAppleConn = openConnection( SEVENTH_DB, APPLE_USER, true );
        vetStatement( _localAuthentication, seventhAppleConn, "select * from " + DBO + ".t1", NO_COLUMN_PERMISSION );

        // if NATIVE authentication is set in the database, then authentication still prevents this user from logging in
        Connection  seventhWalnutConn = getConnection
            ( _localAuthentication, true, SEVENTH_DB, WALNUT_USER, INVALID_AUTHENTICATION );
        
        ///////////////////////////////////////////////////////////////////////////////////
        //
        // Using database only properties, you can override system-specified NATIVE
        // authentication as long as the NATIVE authentication is not LOCAL, i.e., not set
        // at the database level.
        //
        ///////////////////////////////////////////////////////////////////////////////////

        Connection  eighthDBOConn = openConnection( EIGHTH_DB, DBO, true );

        addUser( eighthDBOConn, APPLE_USER );

        // create BUILTIN credentials and set BULTIN authentication via database only properties
        setDatabaseProperty
            ( _localAuthentication, eighthDBOConn, "derby.authentication.provider", "BUILTIN", INVALID_PROVIDER_CHANGE );
        setDatabaseProperty( false, eighthDBOConn, "derby.connection.requireAuthentication", "true", null );
        setDatabaseProperty( false, eighthDBOConn, "derby.database.propertiesOnly", "true", null );
        addBuiltinUser( eighthDBOConn, DBO );
        addBuiltinUser( eighthDBOConn, BUILTIN_USER );

        //  now bring down the database so that the new property settings take effect
        _eighthDBSetup.getTestConfiguration().shutdownDatabase();

        // succeeds unless LOCAL authentication overrides database-specified BUILTIN authentication
        Connection  builtinConn = getConnection
            ( _localAuthentication, true, EIGHTH_DB, BUILTIN_USER, INVALID_AUTHENTICATION );

        // fails if we are using BUILTIN authentication because this user wasn't given BUILTIN credentials
        Connection  eightAppleConn = getConnection
            ( !_localAuthentication, true, EIGHTH_DB, APPLE_USER, INVALID_AUTHENTICATION );
        
        ///////////////////////////////////////////////////////////////////////////////////
        //
        // Vet databases accessed via subprotocols. Not run on Windows machines
        // because of DERBY-5618.
        //
        ///////////////////////////////////////////////////////////////////////////////////

        if ( !onWindows() )
        {
            // database accessed via jar subprotocol
            vetProtocol( jarDBName() );
        
            //
            // We only use the classpath subprotocol if we are not running under a security manager.
            // We may be able to remove that restriction after DERBY-5615 is fixed.
            //
            if ( _disableSecurityManager ) { vetProtocol( classpathDBName() ); }
        }
    }
    private static  String  jarDBName() throws Exception
    {
        return "jar:(" + SupportFilesSetup.getReadOnlyFileName( NAST1_JAR_FILE  ) + ")nast";
    }
    private static  String  classpathDBName()   { return "classpath:nast"; }
    
    private void    addBuiltinUser( Connection conn, String user )  throws Exception
    {
        String  key = "derby.user." + user;
        String  value = getPassword( user );
        
        setDatabaseProperty( false, conn, key, value, null );
    }

    /**
     * <p>
     * Verify that databases in jar files and on the classpath can be opened
     * and respect NATIVE/LOCAL settings.
     * </p>
     */
    private void    vetProtocol( String protocolDBName ) throws Exception
    {
        ///////////////////////////////////////////////////////////////////////////////////
        //
        // Verify that regardless of any other settings, NATIVE authentication is on in the
        // database stored in a jar file or accessed over the classpath
        //
        ///////////////////////////////////////////////////////////////////////////////////

        Connection  grapeConn = getConnection( false, false, protocolDBName, GRAPE_USER, null );

        vetStatement( true, grapeConn, "select * from " + DBO + ".t", NO_COLUMN_PERMISSION );
        
        getConnection( true, false, protocolDBName, WALNUT_USER, INVALID_AUTHENTICATION );
    }

    /**
     * <p>
     * The vetCoreBehavior() method verifies credentials-checking for the
     * following system-wide operations:
     * </p>
     *
     * <ul>
     * <li>Database creation.</li>
     * <li>Engine shutdown.</li>
     * <li>Server shutdown. The default credentials are embedded inside the NetworkServerControl
     * created by NetworkServerTestSetup.</li>
     * </ul>
     *
     * <p>
     * This method verifies credentials-checking for this additional
     * system-wide operation:
     * </p>
     *
     * <ul>
     * <li>Database restoration.</li>
     * </ul>
     */
    private void    vetSystemWideOperations()   throws Exception
    {
        // create a database which we will backup and restore
        Connection  dboConn = openConnection( SIXTH_DB, DBO, true );

        // add another user who can perform restores successfully
        addUser( dboConn, BANANA_USER );

        // add a table which we will backup and then drain. this is so that later on we can
        // verify that we really restored the database rather than just reconnected to the
        // original version.
        goodStatement( dboConn, "create table t( a int )" );
        goodStatement( dboConn, "insert into t( a ) values ( 1000 )" );
        if ( _nativeAuthentication)
        {
            goodStatement( dboConn, "grant select on table t to public" );
            goodStatement( dboConn, "grant insert on table t to public" );
        }
        goodStatement( dboConn, "call syscs_util.syscs_backup_database( '" + _fullBackupDir + "' )" );
        goodStatement( dboConn, "delete from t" );

        // this user is valid both in the system-wide credentials db and in the local db
        shutdownAndRestoreDB( true, BANANA_USER, null );

        //
        // If we are doing local authentication, then restoration will fail when we use
        // the credentials of a user who is in the system-wide SYSUSERS but not
        // in the SYSUSERS of the database being restored. Restoration involves two
        // authentication attempts: First we authenticate system-wide in order to
        // verify that it's ok to proceed with the restoration. After that, we attempt
        // to connect to the restored database. It is the authentication of the second
        // attempt which may fail here.
        //
        shutdownAndRestoreDB( !_localAuthentication, APPLE_USER, INVALID_AUTHENTICATION );
        
        // delete the backup directory
        assertDirectoryDeleted( new File( _fullBackupDir ) );
    }
    private void    shutdownAndRestoreDB( boolean shouldSucceed, String user, String expectedSQLState ) throws Exception
    {
        // shutdown the database. the restore will overwrite it.
        _sixthDBSetup.getTestConfiguration().shutdownDatabase();

        // the physical database name has some parent directories in it.
        // we need to strip these off because backup ignores them.
        String      dbName = _sixthDBSetup.physicalDatabaseName();
        int         slashIdx = dbName.lastIndexOf( "/" );
        if ( slashIdx >= 0 ) { dbName = dbName.substring( slashIdx + 1 ); }

        DataSource  ds = JDBCDataSource.getDataSourceLogical( SIXTH_DB );
        String          fullRestoreDir = _fullBackupDir + "/" + dbName;
        JDBCDataSource.setBeanProperty( ds, "connectionAttributes", "restoreFrom=" + fullRestoreDir );

        Connection  conn = null;

        try {
            conn = ds.getConnection( user, getPassword( user ) );

            if ( !shouldSucceed ) { fail( tagError( "Database restoration should have failed." ) ); }
        }
        catch (SQLException se)
        {
            if ( shouldSucceed ) { fail( tagError( "Database restoration unexpectedly failed." ) );}
            else    { assertSQLState( expectedSQLState, se ); }
        }

        if ( conn != null )
        {
            // verify that this is the version which was backed up, not the original
            assertResults
                (
                 conn,
                 "select a from " + DBO + ".t",
                 new String[][] { { "1000" } },
                 false
                 );

            // add another tuple to distinguish the database from later attempts
            // to re-initialize it from the backup
            goodStatement( conn, "insert into " + DBO + ".t( a ) values ( 2000 )" );
        }
    }
    
    /**
     * <p>
     * Try changing the value of the provider property on disk.
     * These tests are run only if authentication is turned off.
     * </p>
     */
    private void    vetProviderChanges()   throws Exception
    {
        // create an empty database without authentication turned on
        String          dbo = ORANGE_USER;
        Connection  dboConn = openConnection( FOURTH_DB, dbo, true );

        addUser( dboConn, PEAR_USER );

        // NATIVE authentication isn't on, so you can store oddball values for the authentication provider
        goodStatement
            ( dboConn, "call syscs_util.syscs_set_database_property( 'derby.authentication.provider', 'com.acme.AcmeAuthenticator' )" );

        // can't turn on NATIVE authentication until you have stored credentials for the dbo
        expectExecutionError
            (
             dboConn, INVALID_PROVIDER_CHANGE,
             "call syscs_util.syscs_set_database_property( 'derby.authentication.provider', 'NATIVE::LOCAL' )"
             );
 
        // store credentials for the DBO
        addUser( dboConn, dbo );

        // verify that you can't drop the dbo
        expectExecutionError
            (
             dboConn, CANT_DROP_DBO,
             "call syscs_util.syscs_drop_user( '" + dbo + "' )"
             );
        String[][]  legalUsers = new String[][] { { dbo }, { PEAR_USER } };
        assertResults
            (
             dboConn,
             "select username from sys.sysusers order by username",
             legalUsers,
             false
             );
        
        // NATIVE::LOCAL is the only legal value which the authentication provider property can take on disk
        expectExecutionError
            (
             dboConn, INVALID_PROVIDER_CHANGE,
             "call syscs_util.syscs_set_database_property( 'derby.authentication.provider', 'NATIVE:db:LOCAL' )"
             );
        expectExecutionError
            (
             dboConn, INVALID_PROVIDER_CHANGE,
             "call syscs_util.syscs_set_database_property( 'derby.authentication.provider', 'NATIVE:LOCAL' )"
             );
 
        // now turn on NATIVE + LOCAL authentication
        goodStatement( dboConn, "call syscs_util.syscs_set_database_property( 'derby.authentication.provider', 'NATIVE::LOCAL' )" );

        // once set, you can't unset or change it
        expectExecutionError
            (
             dboConn, INVALID_PROVIDER_CHANGE,
             "call syscs_util.syscs_set_database_property( 'derby.authentication.provider', 'NATIVE::LOCAL' )"
             );
        expectExecutionError
            (
             dboConn, INVALID_PROVIDER_CHANGE,
             "call syscs_util.syscs_set_database_property( 'derby.authentication.provider', null )"
             );
        expectExecutionError
            (
             dboConn, INVALID_PROVIDER_CHANGE,
             "call syscs_util.syscs_set_database_property( 'derby.authentication.provider', 'com.acme.AcmeAuthenticator' )"
             );

        // verify that the authentication provider property has the value we expect
        String[][]  authenticationProvider = new String[][] { { "NATIVE::LOCAL" } };
        assertResults
            (
             dboConn,
             "values ( syscs_util.syscs_get_database_property( 'derby.authentication.provider' ) )",
             authenticationProvider,
             false
             );
        
        // create a table for a simple authorization check later on
        goodStatement( dboConn, "create table t( a int )" );

        // shutdown this database so that the on-disk properties will take effect on reboot
        _fourthDBSetup.getTestConfiguration().shutdownDatabase();
        
        // can't connect to the database with credentials which aren't stored in it.
        Connection  appleConn = getConnection( true, true, FOURTH_DB, APPLE_USER, INVALID_AUTHENTICATION );

        // ...but these credentials work
        Connection  pearConn = openConnection( FOURTH_DB, PEAR_USER, true );

        // should get authorization errors trying to select from a table private to the DBO
        // and from trying to view the credentials table
        expectExecutionError( pearConn, NO_COLUMN_PERMISSION, "select * from " + dbo + ".t" );
        expectCompilationError( pearConn, DBO_ONLY_OPERATION, "select username from sys.sysusers" );
        
    }
    
    /**
     * <p>
     * Verify that password lifetimes are checked.
     * </p>
     */
    private void    vetPasswordLifetime()   throws Exception
    {
        // create another database
        Connection  dboConn = openConnection( FIFTH_DB, DBO, true );

        // add another legal user
        addUser( dboConn, APPLE_USER );

        Connection  appleConn = passwordExpiring( false, FIFTH_DB, APPLE_USER );

        // setup so that passwords are expiring after the db is rebooted.
        // shutdown the database in this test so that the new property settings take effect.
        goodStatement
            ( dboConn, "call syscs_util.syscs_set_database_property( 'derby.authentication.native.passwordLifetimeMillis', '86400000' )" );
        goodStatement
            ( dboConn, "call syscs_util.syscs_set_database_property( 'derby.authentication.native.passwordLifetimeThreshold', '2.0' )" );
        _fifthDBSetup.getTestConfiguration().shutdownDatabase();
 
        // password should be expiring
        dboConn = passwordExpiring( true, FIFTH_DB, DBO );
        appleConn = passwordExpiring( true, FIFTH_DB, APPLE_USER );
        
        // setup so that passwords have expired after we reboot the database.
        // shutdown the database so that the new property settings take effect.
        goodStatement
            ( dboConn, "call syscs_util.syscs_set_database_property( 'derby.authentication.native.passwordLifetimeMillis', '1' )" );
        _fifthDBSetup.getTestConfiguration().shutdownDatabase();

        // the DBO's password does not expire
        dboConn = openConnection( FIFTH_DB, DBO, true );

        // but the other user's password has expired
        appleConn = getConnection( true, true, FIFTH_DB, APPLE_USER, INVALID_AUTHENTICATION );
        
        // setup so that passwords don't expire after we reboot the database.
        // shutdown the database so that the new property settings take effect.
        goodStatement
            ( dboConn, "call syscs_util.syscs_set_database_property( 'derby.authentication.native.passwordLifetimeMillis', '0' )" );
        _fifthDBSetup.getTestConfiguration().shutdownDatabase();

        // passwords should NOT be expiring or expired
        dboConn = passwordExpiring( false, FIFTH_DB, DBO );
        appleConn = passwordExpiring( false, FIFTH_DB, APPLE_USER );

        // check that invalid property settings are caught
        expectExecutionError
            (
             dboConn, BAD_PASSWORD_PROPERTY,
             "call syscs_util.syscs_set_database_property( 'derby.authentication.native.passwordLifetimeMillis', 'rabbit' )"
             );
        expectExecutionError
            (
             dboConn, BAD_PASSWORD_PROPERTY,
             "call syscs_util.syscs_set_database_property( 'derby.authentication.native.passwordLifetimeThreshold', '-1' )"
             );
    }

    private void    vetSQLAuthorizationOn() throws Exception
    {
        Connection  nonDBOConn = openConnection( CREDENTIALS_DB, APPLE_USER, true );
        String          query = "select username from sys.sysusers" ;

        try {
            chattyPrepare( nonDBOConn, query );

            if ( _nativeAuthentication ) { fail( "SQL Authorization not on!" ); }
        }
        catch (SQLException se)
        {
            if ( _nativeAuthentication )
            {
                assertSQLState( DBO_ONLY_OPERATION, se );
            }
            else
            {
                fail( "Caught unexpected SQLException: " + se.getSQLState() + ": " + se.getMessage() );
            }
        }
    }
    
    private Connection  getConnection
        ( boolean shouldFail, boolean isLogicalName, String dbName, String user, String expectedSQLState )
        throws Exception
    {
        Connection  conn = null;

        reportConnectionAttempt( dbName, user, isLogicalName );

        try {
            conn = openConnection( dbName, user, isLogicalName );

            if ( shouldFail )   { fail( tagError( "Connection to " + dbName + " should have failed." ) ); }
        }
        catch (Throwable t)
        {
            if ( shouldFail && (t instanceof SQLException) )   { assertSQLState( expectedSQLState, (SQLException) t ); }
            else
            {
                printStackTrace( t );
                fail( tagError( "Connection to " + dbName + " unexpectedly failed." ) );
            }
        }

        return conn;
    }

    // connect but expect a warning that the password is about to expire
    private Connection  passwordExpiring( boolean expiring, String dbName, String user )
        throws Exception
    {
        Connection  conn = null;

        reportConnectionAttempt( dbName, user, true );

        conn = openConnection( dbName, user, true );

        SQLWarning  warning = conn.getWarnings();

        if ( expiring )
        {
            assertNotNull( tagError( "Should have seen a warning" ), warning );
            assertSQLState( PASSWORD_EXPIRING, warning );
        }
        else
        {
            assertNull( tagError( "Should not have seen a warning" ), warning );
        }


        return conn;
    }
    private void    reportConnectionAttempt( String dbName, String user, boolean isLogicalName )
    {
        String  message = user + " attempting to get connection to database " + dbName;
        if ( isLogicalName ) { message = message + " aka " + getTestConfiguration().getPhysicalDatabaseName( dbName ) ; }
        println( message );
    }

    private void    addUser( Connection conn, String user ) throws Exception
    {
        String  password = getPassword( user );
        String  statement = "call syscs_util.syscs_create_user( '" + user + "', '" + password + "' )";
        
        goodStatement( conn, statement );
    }

    private void  setDatabaseProperty( boolean shouldFail, Connection conn, String key, String value, String expectedSQLState )
        throws Exception
    {
        if ( value == null ) { value = "cast ( null as varchar( 32672 ) )"; }
        else { value = "'" + value + "'"; }
        String  command = "call syscs_util.syscs_set_database_property( '" + key + "', " + value + " )";

        try {
            goodStatement( conn, command );

            if ( shouldFail )   { fail( tagError( "Property setting should have failed." ) ); }
        }
        catch (SQLException se)
        {
            if ( shouldFail )   { assertSQLState( expectedSQLState, se ); }
            else    { fail( tagError( "Property setting unexpectedly failed." ) );}
        }
    }

    private void  vetStatement( boolean shouldFail, Connection conn, String command, String expectedSQLState )
        throws Exception
    {
        try {
            goodStatement( conn, command );

            if ( shouldFail )   { fail( tagError( "Statement should have failed." ) ); }
        }
        catch (SQLException se)
        {
            if ( shouldFail )   { assertSQLState( expectedSQLState, se ); }
            else    { fail( tagError( "Statement unexpectedly failed." ) );}
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////
    //
    // MINIONS
    //
    ///////////////////////////////////////////////////////////////////////////////////

    /** Open a connection to a database using the supplied credentials */
    private Connection  openConnection( String dbName, String user, boolean isLogicalName )
        throws SQLException
    {
        String  password = getPassword( user );
        if ( isLogicalName )
        {
            return getTestConfiguration().openConnection( dbName, user, password );
        }
        else
        {
            return getTestConfiguration().openPhysicalConnection( dbName, user, password );
        }
    }
    
    /** Get the password for a user */
    private static  String  getPassword( String user ) { return user + "_password"; }

    /** Tag an error with the name of the test configuration */
    private String  tagError( String text ) { return nameOfTest() + ": " + text; }

}

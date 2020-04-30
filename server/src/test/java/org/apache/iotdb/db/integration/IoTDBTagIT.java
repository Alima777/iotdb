/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.iotdb.db.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import org.apache.iotdb.db.utils.EnvironmentUtils;
import org.apache.iotdb.jdbc.Config;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class IoTDBTagIT {

  @Before
  public void setUp() throws Exception {
    EnvironmentUtils.closeStatMonitor();
    EnvironmentUtils.envSetUp();
  }

  @After
  public void tearDown() throws Exception {
    EnvironmentUtils.cleanEnv();
  }


  @Test
  public void createOneTimeseriesTest() throws ClassNotFoundException {
    String[] ret = {"root.turbine.d1.s1,temperature,root.turbine,FLOAT,RLE,SNAPPY,v1,v2,v1,v2"};
    String sql = "create timeseries root.turbine.d1.s1(temperature) with datatype=FLOAT, encoding=RLE, compression=SNAPPY " +
            "tags(tag1=v1, tag2=v2) attributes(attr1=v1, attr2=v2)";
    Class.forName(Config.JDBC_DRIVER_NAME);
    try (Connection connection = DriverManager
            .getConnection(Config.IOTDB_URL_PREFIX + "127.0.0.1:6667/", "root", "root");
         Statement statement = connection.createStatement()) {
      statement.execute(sql);
      boolean hasResult = statement.execute("show timeseries");
      assertTrue(hasResult);
      ResultSet resultSet = statement.getResultSet();
      int count = 0;
      while (resultSet.next()) {
        String ans = resultSet.getString("timeseries")
                + "," + resultSet.getString("alias")
                + "," + resultSet.getString("storage group")
                + "," + resultSet.getString("dataType")
                + "," + resultSet.getString("encoding")
                + "," + resultSet.getString("compression")
                + "," + resultSet.getString("attr1")
                + "," + resultSet.getString("attr2")
                + "," + resultSet.getString("tag1")
                + "," + resultSet.getString("tag2");
        assertEquals(ret[count], ans);
        count++;
      }
      assertEquals(ret.length, count);
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public void createMultiTimeseriesTest() throws ClassNotFoundException {
    String[] ret = {
            "root.turbine.d2.s1,temperature,root.turbine,FLOAT,RLE,SNAPPY,a1,a2,null,null,t1,t2,null",
            "root.turbine.d2.s2,status,root.turbine,INT32,RLE,SNAPPY,null,null,a3,a4,null,t2,t3"
    };
    String sql1 = "create timeseries root.turbine.d2.s1(temperature) with datatype=FLOAT, encoding=RLE, compression=SNAPPY " +
            "tags(tag1=t1, tag2=t2) attributes(attr1=a1, attr2=a2)";
    String sql2 = "create timeseries root.turbine.d2.s2(status) with datatype=INT32, encoding=RLE " +
            "tags(tag2=t2, tag3=t3) attributes(attr3=a3, attr4=a4)";
    Class.forName(Config.JDBC_DRIVER_NAME);
    try (Connection connection = DriverManager
            .getConnection(Config.IOTDB_URL_PREFIX + "127.0.0.1:6667/", "root", "root");
         Statement statement = connection.createStatement()) {
      statement.execute(sql1);
      statement.execute(sql2);
      boolean hasResult = statement.execute("show timeseries");
      assertTrue(hasResult);
      ResultSet resultSet = statement.getResultSet();
      int count = 0;
      while (resultSet.next()) {
        String ans = resultSet.getString("timeseries")
                + "," + resultSet.getString("alias")
                + "," + resultSet.getString("storage group")
                + "," + resultSet.getString("dataType")
                + "," + resultSet.getString("encoding")
                + "," + resultSet.getString("compression")
                + "," + resultSet.getString("attr1")
                + "," + resultSet.getString("attr2")
                + "," + resultSet.getString("attr3")
                + "," + resultSet.getString("attr4")
                + "," + resultSet.getString("tag1")
                + "," + resultSet.getString("tag2")
                + "," + resultSet.getString("tag3");

         assertEquals(ret[count], ans);
        count++;
      }
      assertEquals(ret.length, count);
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public void createDuplicateAliasTimeseriesTest1() throws ClassNotFoundException {
    String sql1 = "create timeseries root.turbine.d3.s1(temperature) with datatype=FLOAT, encoding=RLE, compression=SNAPPY " +
            "tags(tag1=t1, tag2=t2) attributes(attr1=a1, attr2=a2)";
    String sql2 = "create timeseries root.turbine.d3.s2(temperature) with datatype=INT32, encoding=RLE " +
            "tags(tag2=t2, tag3=t3) attributes(attr3=a3, attr4=a4)";
    Class.forName(Config.JDBC_DRIVER_NAME);
    try (Connection connection = DriverManager
            .getConnection(Config.IOTDB_URL_PREFIX + "127.0.0.1:6667/", "root", "root");
         Statement statement = connection.createStatement()) {
      statement.execute(sql1);
      statement.execute(sql2);
      fail();
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("Alias [temperature] for Path [root.turbine.d3.s2] already exist"));
    }
  }

  @Test
  public void createDuplicateAliasTimeseriesTest2() throws ClassNotFoundException {
    String sql1 = "create timeseries root.turbine.d4.s1(temperature) with datatype=FLOAT, encoding=RLE, compression=SNAPPY " +
            "tags(tag1=t1, tag2=t2) attributes(attr1=a1, attr2=a2)";
    String sql2 = "create timeseries root.turbine.d4.temperature with datatype=INT32, encoding=RLE " +
            "tags(tag2=t2, tag3=t3) attributes(attr3=a3, attr4=a4)";
    Class.forName(Config.JDBC_DRIVER_NAME);
    try (Connection connection = DriverManager
            .getConnection(Config.IOTDB_URL_PREFIX + "127.0.0.1:6667/", "root", "root");
         Statement statement = connection.createStatement()) {
      statement.execute(sql1);
      statement.execute(sql2);
      fail();
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("Path [root.turbine.d4.temperature] already exist"));
    }
  }

  @Test
  public void createDuplicateAliasTimeseriesTest3() throws ClassNotFoundException {
    String sql1 = "create timeseries root.turbine.d5.s1(temperature) with datatype=FLOAT, encoding=RLE, compression=SNAPPY " +
            "tags(tag1=t1, tag2=t2) attributes(attr1=a1, attr2=a2)";
    String sql2 = "create timeseries root.turbine.d5.s2(s1) with datatype=INT32, encoding=RLE " +
            "tags(tag2=t2, tag3=t3) attributes(attr3=a3, attr4=a4)";
    Class.forName(Config.JDBC_DRIVER_NAME);
    try (Connection connection = DriverManager
            .getConnection(Config.IOTDB_URL_PREFIX + "127.0.0.1:6667/", "root", "root");
         Statement statement = connection.createStatement()) {
      statement.execute(sql1);
      statement.execute(sql2);
      fail();
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("Alias [s1] for Path [root.turbine.d5.s2] already exist"));
    }
  }

  @Test
  public void queryWithAliasTest() throws ClassNotFoundException {
    String[] ret = {"root.turbine.d6.s1,temperature,root.turbine,FLOAT,RLE,SNAPPY,v1,v2,v1,v2"};
    String sql = "create timeseries root.turbine.d6.s1(temperature) with datatype=FLOAT, encoding=RLE, compression=SNAPPY " +
            "tags(tag1=v1, tag2=v2) attributes(attr1=v1, attr2=v2)";
    Class.forName(Config.JDBC_DRIVER_NAME);
    try (Connection connection = DriverManager
            .getConnection(Config.IOTDB_URL_PREFIX + "127.0.0.1:6667/", "root", "root");
         Statement statement = connection.createStatement()) {
      statement.execute(sql);
      boolean hasResult = statement.execute("show timeseries root.turbine.d6.temperature");
      assertTrue(hasResult);
      ResultSet resultSet = statement.getResultSet();
      int count = 0;
      while (resultSet.next()) {
        String ans = resultSet.getString("timeseries")
                + "," + resultSet.getString("alias")
                + "," + resultSet.getString("storage group")
                + "," + resultSet.getString("dataType")
                + "," + resultSet.getString("encoding")
                + "," + resultSet.getString("compression")
                + "," + resultSet.getString("attr1")
                + "," + resultSet.getString("attr2")
                + "," + resultSet.getString("tag1")
                + "," + resultSet.getString("tag2");
        assertEquals(ret[count], ans);
        count++;
      }
      assertEquals(ret.length, count);
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }


  @Test
  public void queryWithLimitTest() throws ClassNotFoundException {
    String[] ret = {"root.turbine.d1.s2,temperature2,root.turbine,FLOAT,RLE,SNAPPY,v1,v2,v1,v2",
        "root.turbine.d1.s3,temperature3,root.turbine,FLOAT,RLE,SNAPPY,v1,v2,v1,v2"};
    Class.forName(Config.JDBC_DRIVER_NAME);
    try (Connection connection = DriverManager
        .getConnection(Config.IOTDB_URL_PREFIX + "127.0.0.1:6667/", "root", "root");
        Statement statement = connection.createStatement()) {
      statement.execute("create timeseries root.turbine.d1.s1(temperature1) with datatype=FLOAT, encoding=RLE, compression=SNAPPY " +
          "tags(tag1=v1, tag2=v2) attributes(attr1=v1, attr2=v2)");
      statement.execute("create timeseries root.turbine.d1.s2(temperature2) with datatype=FLOAT, encoding=RLE, compression=SNAPPY " +
          "tags(tag1=v1, tag2=v2) attributes(attr1=v1, attr2=v2)");
      statement.execute("create timeseries root.turbine.d1.s3(temperature3) with datatype=FLOAT, encoding=RLE, compression=SNAPPY " +
          "tags(tag1=v1, tag2=v2) attributes(attr1=v1, attr2=v2)");

      boolean hasResult = statement.execute("show timeseries root.turbine.d1 where tag1=v1 limit 2 offset 1");
      assertTrue(hasResult);
      ResultSet resultSet = statement.getResultSet();
      int count = 0;
      while (resultSet.next()) {
        String ans = resultSet.getString("timeseries")
            + "," + resultSet.getString("alias")
            + "," + resultSet.getString("storage group")
            + "," + resultSet.getString("dataType")
            + "," + resultSet.getString("encoding")
            + "," + resultSet.getString("compression")
            + "," + resultSet.getString("attr1")
            + "," + resultSet.getString("attr2")
            + "," + resultSet.getString("tag1")
            + "," + resultSet.getString("tag2");
        assertEquals(ret[count], ans);
        count++;
      }
      assertEquals(ret.length, count);
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public void deleteTest() throws ClassNotFoundException {
    String[] ret1 = {
            "root.turbine.d7.s1,temperature,root.turbine,FLOAT,RLE,SNAPPY,a1,a2,null,null,t1,t2,null",
            "root.turbine.d7.s2,status,root.turbine,INT32,RLE,SNAPPY,null,null,a3,a4,null,t2,t3"
    };
    String[] ret2 = {"root.turbine.d7.s1,temperature,root.turbine,FLOAT,RLE,SNAPPY,a1,a2,t1,t2"};

    String sql1 = "create timeseries root.turbine.d7.s1(temperature) with datatype=FLOAT, encoding=RLE, compression=SNAPPY " +
            "tags(tag1=t1, tag2=t2) attributes(attr1=a1, attr2=a2)";
    String sql2 = "create timeseries root.turbine.d7.s2(status) with datatype=INT32, encoding=RLE " +
            "tags(tag2=t2, tag3=t3) attributes(attr3=a3, attr4=a4)";
    Class.forName(Config.JDBC_DRIVER_NAME);
    try (Connection connection = DriverManager
            .getConnection(Config.IOTDB_URL_PREFIX + "127.0.0.1:6667/", "root", "root");
         Statement statement = connection.createStatement()) {
      statement.execute(sql1);
      statement.execute(sql2);
      boolean hasResult = statement.execute("show timeseries");
      assertTrue(hasResult);
      ResultSet resultSet = statement.getResultSet();
      int count = 0;
      while (resultSet.next()) {
        String ans = resultSet.getString("timeseries")
                + "," + resultSet.getString("alias")
                + "," + resultSet.getString("storage group")
                + "," + resultSet.getString("dataType")
                + "," + resultSet.getString("encoding")
                + "," + resultSet.getString("compression")
                + "," + resultSet.getString("attr1")
                + "," + resultSet.getString("attr2")
                + "," + resultSet.getString("attr3")
                + "," + resultSet.getString("attr4")
                + "," + resultSet.getString("tag1")
                + "," + resultSet.getString("tag2")
                + "," + resultSet.getString("tag3");

        assertEquals(ret1[count], ans);
        count++;
      }
      assertEquals(ret1.length, count);

      statement.execute("delete timeseries root.turbine.d7.s2");
      hasResult = statement.execute("show timeseries");
      assertTrue(hasResult);
      resultSet = statement.getResultSet();
      count = 0;
      while (resultSet.next()) {
        String ans = resultSet.getString("timeseries")
                + "," + resultSet.getString("alias")
                + "," + resultSet.getString("storage group")
                + "," + resultSet.getString("dataType")
                + "," + resultSet.getString("encoding")
                + "," + resultSet.getString("compression")
                + "," + resultSet.getString("attr1")
                + "," + resultSet.getString("attr2")
                + "," + resultSet.getString("tag1")
                + "," + resultSet.getString("tag2");

        assertEquals(ret2[count], ans);
        count++;
      }
      assertEquals(ret2.length, count);

    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public void deleteWithAliasTest() throws ClassNotFoundException {
    String[] ret1 = {
            "root.turbine.d7.s1,temperature,root.turbine,FLOAT,RLE,SNAPPY,a1,a2,null,null,t1,t2,null",
            "root.turbine.d7.s2,status,root.turbine,INT32,RLE,SNAPPY,null,null,a3,a4,null,t2,t3"
    };
    String[] ret2 = {"root.turbine.d7.s1,temperature,root.turbine,FLOAT,RLE,SNAPPY,a1,a2,t1,t2"};

    String sql1 = "create timeseries root.turbine.d7.s1(temperature) with datatype=FLOAT, encoding=RLE, compression=SNAPPY " +
            "tags(tag1=t1, tag2=t2) attributes(attr1=a1, attr2=a2)";
    String sql2 = "create timeseries root.turbine.d7.s2(status) with datatype=INT32, encoding=RLE " +
            "tags(tag2=t2, tag3=t3) attributes(attr3=a3, attr4=a4)";
    Class.forName(Config.JDBC_DRIVER_NAME);
    try (Connection connection = DriverManager
            .getConnection(Config.IOTDB_URL_PREFIX + "127.0.0.1:6667/", "root", "root");
         Statement statement = connection.createStatement()) {
      statement.execute(sql1);
      statement.execute(sql2);
      boolean hasResult = statement.execute("show timeseries");
      assertTrue(hasResult);
      ResultSet resultSet = statement.getResultSet();
      int count = 0;
      while (resultSet.next()) {
        String ans = resultSet.getString("timeseries")
                + "," + resultSet.getString("alias")
                + "," + resultSet.getString("storage group")
                + "," + resultSet.getString("dataType")
                + "," + resultSet.getString("encoding")
                + "," + resultSet.getString("compression")
                + "," + resultSet.getString("attr1")
                + "," + resultSet.getString("attr2")
                + "," + resultSet.getString("attr3")
                + "," + resultSet.getString("attr4")
                + "," + resultSet.getString("tag1")
                + "," + resultSet.getString("tag2")
                + "," + resultSet.getString("tag3");

        assertEquals(ret1[count], ans);
        count++;
      }
      assertEquals(ret1.length, count);

      statement.execute("delete timeseries root.turbine.d7.status");
      hasResult = statement.execute("show timeseries");
      assertTrue(hasResult);
      resultSet = statement.getResultSet();
      count = 0;
      while (resultSet.next()) {
        String ans = resultSet.getString("timeseries")
                + "," + resultSet.getString("alias")
                + "," + resultSet.getString("storage group")
                + "," + resultSet.getString("dataType")
                + "," + resultSet.getString("encoding")
                + "," + resultSet.getString("compression")
                + "," + resultSet.getString("attr1")
                + "," + resultSet.getString("attr2")
                + "," + resultSet.getString("tag1")
                + "," + resultSet.getString("tag2");

        assertEquals(ret2[count], ans);
        count++;
      }
      assertEquals(ret2.length, count);

    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }


  @Test
  public void queryWithWhereTest1() throws ClassNotFoundException {
    String[] ret1 = {
            "root.turbine.d0.s0,temperature,root.turbine,FLOAT,RLE,SNAPPY,turbine this is a test1,100,50,null,null,f",
            "root.turbine.d0.s1,power,root.turbine,FLOAT,RLE,SNAPPY,turbine this is a test2,99.9,44.4,null,null,kw",
            "root.turbine.d1.s0,status,root.turbine,INT32,RLE,SNAPPY,turbine this is a test3,9,5,null,null,null",
            "root.turbine.d2.s0,temperature,root.turbine,FLOAT,RLE,SNAPPY,turbine d2 this is a test1,null,null,100,1,f",
            "root.turbine.d2.s1,power,root.turbine,FLOAT,RLE,SNAPPY,turbine d2 this is a test2,null,null,99.9,44.4,kw",
            "root.turbine.d2.s3,status,root.turbine,INT32,RLE,SNAPPY,turbine d2 this is a test3,null,null,9,5,null",
            "root.ln.d0.s0,temperature,root.ln,FLOAT,RLE,SNAPPY,ln this is a test1,1000,500,null,null,c",
            "root.ln.d0.s1,power,root.ln,FLOAT,RLE,SNAPPY,ln this is a test2,9.9,4.4,null,null,w",
            "root.ln.d1.s0,status,root.ln,INT32,RLE,SNAPPY,ln this is a test3,90,50,null,null,null",
    };

    Set<String> ret2 = new HashSet<>();
    ret2.add("root.turbine.d0.s0,temperature,root.turbine,FLOAT,RLE,SNAPPY,turbine this is a test1,100,50,null,null,f");
    ret2.add("root.turbine.d2.s0,temperature,root.turbine,FLOAT,RLE,SNAPPY,turbine d2 this is a test1,null,null,100,1,f");

    String[] sqls = {
            "create timeseries root.turbine.d0.s0(temperature) with datatype=FLOAT, encoding=RLE, compression=SNAPPY " +
                    "tags(unit=f, description='turbine this is a test1') attributes(H_Alarm=100, M_Alarm=50)",

            "create timeseries root.turbine.d0.s1(power) with datatype=FLOAT, encoding=RLE, compression=SNAPPY " +
                    "tags(unit=kw, description='turbine this is a test2') attributes(H_Alarm=99.9, M_Alarm=44.4)",

            "create timeseries root.turbine.d1.s0(status) with datatype=INT32, encoding=RLE " +
                    "tags(description='turbine this is a test3') attributes(H_Alarm=9, M_Alarm=5)",

            "create timeseries root.turbine.d2.s0(temperature) with datatype=FLOAT, encoding=RLE, compression=SNAPPY " +
                    "tags(unit=f, description='turbine d2 this is a test1') attributes(MaxValue=100, MinValue=1)",

            "create timeseries root.turbine.d2.s1(power) with datatype=FLOAT, encoding=RLE, compression=SNAPPY " +
                    "tags(unit=kw, description='turbine d2 this is a test2') attributes(MaxValue=99.9, MinValue=44.4)",

            "create timeseries root.turbine.d2.s3(status) with datatype=INT32, encoding=RLE " +
                    "tags(description='turbine d2 this is a test3') attributes(MaxValue=9, MinValue=5)",

            "create timeseries root.ln.d0.s0(temperature) with datatype=FLOAT, encoding=RLE, compression=SNAPPY " +
                    "tags(unit=c, description='ln this is a test1') attributes(H_Alarm=1000, M_Alarm=500)",

            "create timeseries root.ln.d0.s1(power) with datatype=FLOAT, encoding=RLE, compression=SNAPPY " +
                    "tags(unit=w, description='ln this is a test2') attributes(H_Alarm=9.9, M_Alarm=4.4)",

            "create timeseries root.ln.d1.s0(status) with datatype=INT32, encoding=RLE " +
                    "tags(description='ln this is a test3') attributes(H_Alarm=90, M_Alarm=50)",
    };
    Class.forName(Config.JDBC_DRIVER_NAME);
    try (Connection connection = DriverManager
            .getConnection(Config.IOTDB_URL_PREFIX + "127.0.0.1:6667/", "root", "root");
         Statement statement = connection.createStatement()) {
      for (String sql : sqls) {
        statement.execute(sql);
      }
      boolean hasResult = statement.execute("show timeseries");
      assertTrue(hasResult);
      ResultSet resultSet = statement.getResultSet();
      int count = 0;
      while (resultSet.next()) {
        String ans = resultSet.getString("timeseries")
                + "," + resultSet.getString("alias")
                + "," + resultSet.getString("storage group")
                + "," + resultSet.getString("dataType")
                + "," + resultSet.getString("encoding")
                + "," + resultSet.getString("compression")
                + "," + resultSet.getString("description")
                + "," + resultSet.getString("H_Alarm")
                + "," + resultSet.getString("M_Alarm")
                + "," + resultSet.getString("MaxValue")
                + "," + resultSet.getString("MinValue")
                + "," + resultSet.getString("unit");

        assertEquals(ret1[count], ans);
        count++;
      }
      assertEquals(ret1.length, count);


      hasResult = statement.execute("show timeseries where unit=f");
      assertTrue(hasResult);
      resultSet = statement.getResultSet();
      count = 0;
      Set<String> res = new HashSet<>();
      while (resultSet.next()) {
        String ans =
            resultSet.getString("timeseries")
                + "," + resultSet.getString("alias")
                + "," + resultSet.getString("storage group")
                + "," + resultSet.getString("dataType")
                + "," + resultSet.getString("encoding")
                + "," + resultSet.getString("compression")
                + "," + resultSet.getString("description")
                + "," + resultSet.getString("H_Alarm")
                + "," + resultSet.getString("M_Alarm")
                + "," + resultSet.getString("MaxValue")
                + "," + resultSet.getString("MinValue")
                + "," + resultSet.getString("unit");
        res.add(ans);
        count++;
      }
      assertEquals(ret2, res);
      assertEquals(ret2.size(), count);
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public void queryWithWhereTest2() throws ClassNotFoundException {
    Set<String> ret = new HashSet<>();
    ret.add("root.turbine.d0.s0,temperature,root.turbine,FLOAT,RLE,SNAPPY,turbine this is a test1,100,50,null,null,f");
    ret.add("root.turbine.d2.s0,temperature,root.turbine,FLOAT,RLE,SNAPPY,turbine d2 this is a test1,null,null,100,1,f");

    String[] sqls = {
            "create timeseries root.turbine.d0.s0(temperature) with datatype=FLOAT, encoding=RLE, compression=SNAPPY " +
                    "tags(unit=f, description='turbine this is a test1') attributes(H_Alarm=100, M_Alarm=50)",

            "create timeseries root.turbine.d0.s1(power) with datatype=FLOAT, encoding=RLE, compression=SNAPPY " +
                    "tags(unit=kw, description='turbine this is a test2') attributes(H_Alarm=99.9, M_Alarm=44.4)",

            "create timeseries root.turbine.d1.s0(status) with datatype=INT32, encoding=RLE " +
                    "tags(description='turbine this is a test3') attributes(H_Alarm=9, M_Alarm=5)",

            "create timeseries root.turbine.d2.s0(temperature) with datatype=FLOAT, encoding=RLE, compression=SNAPPY " +
                    "tags(unit=f, description='turbine d2 this is a test1') attributes(MaxValue=100, MinValue=1)",

            "create timeseries root.turbine.d2.s1(power) with datatype=FLOAT, encoding=RLE, compression=SNAPPY " +
                    "tags(unit=kw, description='turbine d2 this is a test2') attributes(MaxValue=99.9, MinValue=44.4)",

            "create timeseries root.turbine.d2.s3(status) with datatype=INT32, encoding=RLE " +
                    "tags(description='turbine d2 this is a test3') attributes(MaxValue=9, MinValue=5)",

            "create timeseries root.ln.d0.s0(temperature) with datatype=FLOAT, encoding=RLE, compression=SNAPPY " +
                    "tags(unit=f, description='ln this is a test1') attributes(H_Alarm=1000, M_Alarm=500)",

            "create timeseries root.ln.d0.s1(power) with datatype=FLOAT, encoding=RLE, compression=SNAPPY " +
                    "tags(unit=w, description='ln this is a test2') attributes(H_Alarm=9.9, M_Alarm=4.4)",

            "create timeseries root.ln.d1.s0(status) with datatype=INT32, encoding=RLE " +
                    "tags(description='ln this is a test3') attributes(H_Alarm=90, M_Alarm=50)",
    };
    Class.forName(Config.JDBC_DRIVER_NAME);
    try (Connection connection = DriverManager
            .getConnection(Config.IOTDB_URL_PREFIX + "127.0.0.1:6667/", "root", "root");
         Statement statement = connection.createStatement()) {
      for (String sql : sqls) {
        statement.execute(sql);
      }

      // with *
      boolean hasResult = statement.execute("show timeseries root.turbine.* where unit=f");
      assertTrue(hasResult);
      ResultSet resultSet = statement.getResultSet();
      int count = 0;
      Set<String> res = new HashSet<>();
      while (resultSet.next()) {
        String ans = resultSet.getString("timeseries")
                + "," + resultSet.getString("alias")
                + "," + resultSet.getString("storage group")
                + "," + resultSet.getString("dataType")
                + "," + resultSet.getString("encoding")
                + "," + resultSet.getString("compression")
                + "," + resultSet.getString("description")
                + "," + resultSet.getString("H_Alarm")
                + "," + resultSet.getString("M_Alarm")
                + "," + resultSet.getString("MaxValue")
                + "," + resultSet.getString("MinValue")
                + "," + resultSet.getString("unit");

        res.add(ans);
        count++;
      }
      assertEquals(ret, res);
      assertEquals(ret.size(), count);

      // no *
      hasResult = statement.execute("show timeseries root.turbine where unit=f");
      assertTrue(hasResult);
      resultSet = statement.getResultSet();
      count = 0;
      res.clear();
      while (resultSet.next()) {
        String ans = resultSet.getString("timeseries")
                + "," + resultSet.getString("alias")
                + "," + resultSet.getString("storage group")
                + "," + resultSet.getString("dataType")
                + "," + resultSet.getString("encoding")
                + "," + resultSet.getString("compression")
                + "," + resultSet.getString("description")
                + "," + resultSet.getString("H_Alarm")
                + "," + resultSet.getString("M_Alarm")
                + "," + resultSet.getString("MaxValue")
                + "," + resultSet.getString("MinValue")
                + "," + resultSet.getString("unit");

        res.add(ans);
        count++;
      }
      assertEquals(ret, res);
      assertEquals(ret.size(), count);

      statement.execute("show timeseries root.turbine where unit=c");
      resultSet = statement.getResultSet();
      count = 0;
      while (resultSet.next()) {
        count++;
      }
      assertEquals(0, count);

    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public void queryWithWhereAndDeleteTest() throws ClassNotFoundException {
    Set<String> ret = new HashSet<>();
    ret.add("root.turbine.d0.s0,temperature,root.turbine,FLOAT,RLE,SNAPPY,turbine this is a test1,100,50,f");
    ret.add("root.ln.d0.s0,temperature,root.ln,FLOAT,RLE,SNAPPY,ln this is a test1,1000,500,f");

    String[] sqls = {
            "create timeseries root.turbine.d0.s0(temperature) with datatype=FLOAT, encoding=RLE, compression=SNAPPY " +
                    "tags(unit=f, description='turbine this is a test1') attributes(H_Alarm=100, M_Alarm=50)",

            "create timeseries root.turbine.d0.s1(power) with datatype=FLOAT, encoding=RLE, compression=SNAPPY " +
                    "tags(unit=kw, description='turbine this is a test2') attributes(H_Alarm=99.9, M_Alarm=44.4)",

            "create timeseries root.turbine.d1.s0(status) with datatype=INT32, encoding=RLE " +
                    "tags(description='turbine this is a test3') attributes(H_Alarm=9, M_Alarm=5)",

            "create timeseries root.turbine.d2.s0(temperature) with datatype=FLOAT, encoding=RLE, compression=SNAPPY " +
                    "tags(unit=f, description='turbine d2 this is a test1') attributes(MaxValue=100, MinValue=1)",

            "create timeseries root.turbine.d2.s1(power) with datatype=FLOAT, encoding=RLE, compression=SNAPPY " +
                    "tags(unit=kw, description='turbine d2 this is a test2') attributes(MaxValue=99.9, MinValue=44.4)",

            "create timeseries root.turbine.d2.s3(status) with datatype=INT32, encoding=RLE " +
                    "tags(description='turbine d2 this is a test3') attributes(MaxValue=9, MinValue=5)",

            "create timeseries root.ln.d0.s0(temperature) with datatype=FLOAT, encoding=RLE, compression=SNAPPY " +
                    "tags(unit=f, description='ln this is a test1') attributes(H_Alarm=1000, M_Alarm=500)",

            "create timeseries root.ln.d0.s1(power) with datatype=FLOAT, encoding=RLE, compression=SNAPPY " +
                    "tags(unit=w, description='ln this is a test2') attributes(H_Alarm=9.9, M_Alarm=4.4)",

            "create timeseries root.ln.d1.s0(status) with datatype=INT32, encoding=RLE " +
                    "tags(description='ln this is a test3') attributes(H_Alarm=90, M_Alarm=50)",
    };
    Class.forName(Config.JDBC_DRIVER_NAME);
    try (Connection connection = DriverManager
            .getConnection(Config.IOTDB_URL_PREFIX + "127.0.0.1:6667/", "root", "root");
         Statement statement = connection.createStatement()) {
      for (String sql : sqls) {
        statement.execute(sql);
      }

      statement.execute("delete timeseries root.turbine.d2.s0");

      // with *
      boolean hasResult = statement.execute("show timeseries where unit=f");
      assertTrue(hasResult);
      ResultSet resultSet = statement.getResultSet();
      int count = 0;
      Set<String> res = new HashSet<>();
      while (resultSet.next()) {
        String ans = resultSet.getString("timeseries")
                + "," + resultSet.getString("alias")
                + "," + resultSet.getString("storage group")
                + "," + resultSet.getString("dataType")
                + "," + resultSet.getString("encoding")
                + "," + resultSet.getString("compression")
                + "," + resultSet.getString("description")
                + "," + resultSet.getString("H_Alarm")
                + "," + resultSet.getString("M_Alarm")
                + "," + resultSet.getString("unit");

        res.add(ans);
        count++;
      }
      assertEquals(ret, res);
      assertEquals(ret.size(), count);

    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public void queryWithWhereContainsTest() throws ClassNotFoundException {
    Set<String> ret = new HashSet<>();
    ret.add("root.turbine.d0.s0,temperature,root.turbine,FLOAT,RLE,SNAPPY,turbine this is a test1,100,50,null,null,f");
    ret.add("root.turbine.d2.s0,temperature,root.turbine,FLOAT,RLE,SNAPPY,turbine d2 this is a test1,null,null,100,1,f");
    ret.add("root.ln.d0.s0,temperature,root.ln,FLOAT,RLE,SNAPPY,ln this is a test1,1000,500,null,null,f");

    Set<String> ret2 = new HashSet<>();
    ret2.add("root.ln.d0.s0,temperature,root.ln,FLOAT,RLE,SNAPPY,ln this is a test1,1000,500,f");

    String[] sqls = {
            "create timeseries root.turbine.d0.s0(temperature) with datatype=FLOAT, encoding=RLE, compression=SNAPPY " +
                    "tags(unit=f, description='turbine this is a test1') attributes(H_Alarm=100, M_Alarm=50)",

            "create timeseries root.turbine.d0.s1(power) with datatype=FLOAT, encoding=RLE, compression=SNAPPY " +
                    "tags(unit=kw, description='turbine this is a test2') attributes(H_Alarm=99.9, M_Alarm=44.4)",

            "create timeseries root.turbine.d1.s0(status) with datatype=INT32, encoding=RLE " +
                    "tags(description='turbine this is a test3') attributes(H_Alarm=9, M_Alarm=5)",

            "create timeseries root.turbine.d2.s0(temperature) with datatype=FLOAT, encoding=RLE, compression=SNAPPY " +
                    "tags(unit=f, description='turbine d2 this is a test1') attributes(MaxValue=100, MinValue=1)",

            "create timeseries root.turbine.d2.s1(power) with datatype=FLOAT, encoding=RLE, compression=SNAPPY " +
                    "tags(unit=kw, description='turbine d2 this is a test2') attributes(MaxValue=99.9, MinValue=44.4)",

            "create timeseries root.turbine.d2.s3(status) with datatype=INT32, encoding=RLE " +
                    "tags(description='turbine d2 this is a test3') attributes(MaxValue=9, MinValue=5)",

            "create timeseries root.ln.d0.s0(temperature) with datatype=FLOAT, encoding=RLE, compression=SNAPPY " +
                    "tags(unit=f, description='ln this is a test1') attributes(H_Alarm=1000, M_Alarm=500)",

            "create timeseries root.ln.d0.s1(power) with datatype=FLOAT, encoding=RLE, compression=SNAPPY " +
                    "tags(unit=w, description='ln this is a test2') attributes(H_Alarm=9.9, M_Alarm=4.4)",

            "create timeseries root.ln.d1.s0(status) with datatype=INT32, encoding=RLE " +
                    "tags(description='ln this is a test3') attributes(H_Alarm=90, M_Alarm=50)",
    };
    Class.forName(Config.JDBC_DRIVER_NAME);
    try (Connection connection = DriverManager
            .getConnection(Config.IOTDB_URL_PREFIX + "127.0.0.1:6667/", "root", "root");
         Statement statement = connection.createStatement()) {
      for (String sql : sqls) {
        statement.execute(sql);
      }

      boolean hasResult = statement.execute("show timeseries where description contains 'test1'");
      assertTrue(hasResult);
      ResultSet resultSet = statement.getResultSet();
      int count = 0;
      Set<String> res = new HashSet<>();
      while (resultSet.next()) {
        String ans = resultSet.getString("timeseries")
                + "," + resultSet.getString("alias")
                + "," + resultSet.getString("storage group")
                + "," + resultSet.getString("dataType")
                + "," + resultSet.getString("encoding")
                + "," + resultSet.getString("compression")
                + "," + resultSet.getString("description")
                + "," + resultSet.getString("H_Alarm")
                + "," + resultSet.getString("M_Alarm")
                + "," + resultSet.getString("MaxValue")
                + "," + resultSet.getString("MinValue")
                + "," + resultSet.getString("unit");

        System.out.println(ans);
        res.add(ans);
        count++;
      }
      assertEquals(ret, res);
      assertEquals(ret.size(), count);

      hasResult = statement.execute("show timeseries root.ln where description contains 'test1'");
      assertTrue(hasResult);
      resultSet = statement.getResultSet();
      count = 0;
      res.clear();
      while (resultSet.next()) {
        String ans = resultSet.getString("timeseries")
                + "," + resultSet.getString("alias")
                + "," + resultSet.getString("storage group")
                + "," + resultSet.getString("dataType")
                + "," + resultSet.getString("encoding")
                + "," + resultSet.getString("compression")
                + "," + resultSet.getString("description")
                + "," + resultSet.getString("H_Alarm")
                + "," + resultSet.getString("M_Alarm")
                + "," + resultSet.getString("unit");

        res.add(ans);
        count++;
      }
      assertEquals(ret2, res);
      assertEquals(ret2.size(), count);

    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public void queryWithWhereOnNoneTagTest() throws ClassNotFoundException {
    String[] sqls = {
            "create timeseries root.turbine.d0.s0(temperature) with datatype=FLOAT, encoding=RLE, compression=SNAPPY " +
                    "tags(unit=f, description='turbine this is a test1') attributes(H_Alarm=100, M_Alarm=50)",

            "create timeseries root.turbine.d0.s1(power) with datatype=FLOAT, encoding=RLE, compression=SNAPPY " +
                    "tags(unit=kw, description='turbine this is a test2') attributes(H_Alarm=99.9, M_Alarm=44.4)",

            "create timeseries root.turbine.d1.s0(status) with datatype=INT32, encoding=RLE " +
                    "tags(description='turbine this is a test3') attributes(H_Alarm=9, M_Alarm=5)",

            "create timeseries root.turbine.d2.s0(temperature) with datatype=FLOAT, encoding=RLE, compression=SNAPPY " +
                    "tags(unit=f, description='turbine d2 this is a test1') attributes(MaxValue=100, MinValue=1)",

            "create timeseries root.turbine.d2.s1(power) with datatype=FLOAT, encoding=RLE, compression=SNAPPY " +
                    "tags(unit=kw, description='turbine d2 this is a test2') attributes(MaxValue=99.9, MinValue=44.4)",

            "create timeseries root.turbine.d2.s3(status) with datatype=INT32, encoding=RLE " +
                    "tags(description='turbine d2 this is a test3') attributes(MaxValue=9, MinValue=5)",

            "create timeseries root.ln.d0.s0(temperature) with datatype=FLOAT, encoding=RLE, compression=SNAPPY " +
                    "tags(unit=f, description='ln this is a test1') attributes(H_Alarm=1000, M_Alarm=500)",

            "create timeseries root.ln.d0.s1(power) with datatype=FLOAT, encoding=RLE, compression=SNAPPY " +
                    "tags(unit=w, description='ln this is a test2') attributes(H_Alarm=9.9, M_Alarm=4.4)",

            "create timeseries root.ln.d1.s0(status) with datatype=INT32, encoding=RLE " +
                    "tags(description='ln this is a test3') attributes(H_Alarm=90, M_Alarm=50)",
    };
    Class.forName(Config.JDBC_DRIVER_NAME);
    try (Connection connection = DriverManager
            .getConnection(Config.IOTDB_URL_PREFIX + "127.0.0.1:6667/", "root", "root");
         Statement statement = connection.createStatement()) {
      for (String sql : sqls) {
        statement.execute(sql);
      }

      statement.execute("show timeseries where H_Alarm=90");
      fail();
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("The key H_Alarm is not a tag"));
    }
  }

  @Test
  public void sameNameTest() throws ClassNotFoundException {
    String sql = "create timeseries root.turbine.d1.s1(temperature) with datatype=FLOAT, encoding=RLE, compression=SNAPPY " +
            "tags(tag1=v1, tag2=v2) attributes(tag1=v1, attr2=v2)";
    Class.forName(Config.JDBC_DRIVER_NAME);
    try (Connection connection = DriverManager
            .getConnection(Config.IOTDB_URL_PREFIX + "127.0.0.1:6667/", "root", "root");
         Statement statement = connection.createStatement()) {
      statement.execute(sql);
      fail();
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("Tag and attribute shouldn't have the same property key"));
    }
  }

  @Test
  public void deleteStorageGroupTest() throws ClassNotFoundException {
    String[] ret = {"root.turbine.d1.s1,temperature,root.turbine,FLOAT,RLE,SNAPPY,v1,v2,v1,v2"};

    String sql = "create timeseries root.turbine.d1.s1(temperature) with datatype=FLOAT, encoding=RLE, compression=SNAPPY " +
        "tags(tag1=v1, tag2=v2) attributes(attr1=v1, attr2=v2)";
    Class.forName(Config.JDBC_DRIVER_NAME);
    try (Connection connection = DriverManager
        .getConnection(Config.IOTDB_URL_PREFIX + "127.0.0.1:6667/", "root", "root");
        Statement statement = connection.createStatement()) {
      statement.execute(sql);
      boolean hasResult = statement.execute("show timeseries");
      assertTrue(hasResult);
      ResultSet resultSet = statement.getResultSet();
      int count = 0;
      while (resultSet.next()) {
        String ans = resultSet.getString("timeseries")
            + "," + resultSet.getString("alias")
            + "," + resultSet.getString("storage group")
            + "," + resultSet.getString("dataType")
            + "," + resultSet.getString("encoding")
            + "," + resultSet.getString("compression")
            + "," + resultSet.getString("attr1")
            + "," + resultSet.getString("attr2")
            + "," + resultSet.getString("tag1")
            + "," + resultSet.getString("tag2");
        assertEquals(ret[count], ans);
        count++;
      }
      assertEquals(ret.length, count);

      statement.execute("delete storage group root.turbine");
      statement.execute("show timeseries where tag1=v1");
      fail();
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("The key tag1 is not a tag"));
    }
  }
}

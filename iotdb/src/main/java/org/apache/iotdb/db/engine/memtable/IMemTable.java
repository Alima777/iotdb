/**
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
package org.apache.iotdb.db.engine.memtable;

import java.util.Map;
import org.apache.iotdb.db.engine.modification.Deletion;
import org.apache.iotdb.db.engine.querycontext.ReadOnlyMemChunk;
import org.apache.iotdb.db.qp.physical.crud.InsertPlan;
import org.apache.iotdb.db.utils.MemUtils;
import org.apache.iotdb.tsfile.file.metadata.enums.TSDataType;
import org.apache.iotdb.tsfile.utils.Binary;
import org.apache.iotdb.tsfile.write.record.TSRecord;
import org.apache.iotdb.tsfile.write.record.datapoint.DataPoint;

/**
 * IMemTable is designed to store data points which are not flushed into TsFile yet. An instance of
 * IMemTable maintains all series belonging to one StorageGroup,
 * corresponding to one FileNodeProcessor.<br> The concurrent control of IMemTable
 * is based on the concurrent control of FileNodeProcessor, i.e., Writing and
 * querying operations must already have gotten writeLock and readLock respectively.<br>
 */
public interface IMemTable {

  Map<String, Map<String, IWritableMemChunk>> getMemTableMap();

  void write(String deviceId, String measurement, TSDataType dataType,
      long insertTime, String insertValue);

  void write(String deviceId, String measurement, TSDataType dataType,
      long insertTime, Object value);

  /**
   * @return the number of points
   */
  long size();

  /**
   * @return memory usage
   */
  long memSize();

  void insert(InsertPlan insertPlan);

  ReadOnlyMemChunk query(String deviceId, String measurement, TSDataType dataType,
      Map<String, String> props);

  /**
   * putBack all the memory resources.
   */
  void clear();

  boolean isEmpty();

  /**
   * Delete data in it whose timestamp <= 'timestamp' and belonging to timeseries
   * deviceId.measurementId. Only called for non-flushing MemTable.
   *
   * @param deviceId the deviceId of the timeseries to be deleted.
   * @param measurementId the measurementId of the timeseries to be deleted.
   * @param timestamp the upper-bound of deletion time.
   * @return true if there is data that been deleted. otherwise false.
   */
  boolean delete(String deviceId, String measurementId, long timestamp);

  /**
   * Delete data in it whose timestamp <= 'timestamp' and belonging to timeseries
   * deviceId.measurementId. Only called for flushing MemTable.
   *
   * @param deletion and object representing this deletion
   * @return true if there is data that been deleted. otherwise false.
   */
  boolean delete(Deletion deletion);

  /**
   * Make a copy of this MemTable.
   *
   * @return a MemTable with the same data as this one.
   */
  IMemTable copy();

  boolean containSeries(String deviceId, String measurementId);

  boolean isManagedByMemPool();

  long getVersion();

  void setVersion(long version);
}

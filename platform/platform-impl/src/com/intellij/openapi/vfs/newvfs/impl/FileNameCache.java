/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.openapi.vfs.newvfs.impl;

import com.intellij.openapi.vfs.newvfs.persistent.FSRecords;
import com.intellij.util.IntSLRUCache;
import com.intellij.util.containers.IntObjectLinkedMap;
import com.intellij.util.text.ByteArrayCharSequence;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author peter
 */
public class FileNameCache {
  @SuppressWarnings("unchecked") private final IntSLRUCache<IntObjectLinkedMap.MapEntry<CharSequence>>[] ourNameCache =
    new IntSLRUCache[16];

  private final FSRecords myFSRecords;

  public FileNameCache(final FSRecords records) {
    myFSRecords = records;
    final int protectedSize = 40000 / ourNameCache.length;
    final int probationalSize = 20000 / ourNameCache.length;
    for (int i = 0; i < ourNameCache.length; ++i) {
      ourNameCache[i] = new IntSLRUCache<>(protectedSize, probationalSize);
    }
  }

  public int storeName(@NotNull String name) {
    final int idx = myFSRecords.getNameId(name);
    cacheData(name, idx, calcStripeIdFromNameId(idx));
    return idx;
  }

  @NotNull
  private IntObjectLinkedMap.MapEntry<CharSequence> cacheData(String name, int id, int stripe) {
    if (name == null) {
      myFSRecords.getNames().markCorrupted();
      throw new RuntimeException("VFS name enumerator corrupted");
    }

    CharSequence rawName = ByteArrayCharSequence.convertToBytesIfAsciiString(name);
    IntObjectLinkedMap.MapEntry<CharSequence> entry = new IntObjectLinkedMap.MapEntry<>(id, rawName);
    IntSLRUCache<IntObjectLinkedMap.MapEntry<CharSequence>> cache = ourNameCache[stripe];
    //noinspection SynchronizationOnLocalVariableOrMethodParameter
    synchronized (cache) {
      return cache.cacheEntry(entry);
    }
  }

  private int calcStripeIdFromNameId(int id) {
    int h = id;
    h -= h << 6;
    h ^= h >> 17;
    h -= h << 9;
    h ^= h << 4;
    h -= h << 3;
    h ^= h << 10;
    h ^= h >> 15;
    return h % ourNameCache.length;
  }

  private static final boolean ourTrackStats = false;
  private static final int ourLOneSize = 1024;
  private final IntObjectLinkedMap.MapEntry<CharSequence>[] ourArrayCache = new IntObjectLinkedMap.MapEntry[ourLOneSize];

  private final AtomicInteger ourQueries = new AtomicInteger();
  private final AtomicInteger ourMisses = new AtomicInteger();


  @FunctionalInterface
  public interface NameComputer {
    String compute(int id) throws IOException;
  }

  @NotNull
  public CharSequence getVFileName(int nameId, @NotNull NameComputer computeName) throws IOException {
    assert nameId > 0;

    if (ourTrackStats) {
      int frequency = 10000000;
      int queryCount = ourQueries.incrementAndGet();
      if (queryCount >= frequency && ourQueries.compareAndSet(queryCount, 0)) {
        double misses = ourMisses.getAndSet(0);
        //noinspection UseOfSystemOutOrSystemErr
        System.out.println("Misses: " + (misses / frequency));
        ourQueries.set(0);
      }
    }

    int l1 = nameId % ourLOneSize;
    IntObjectLinkedMap.MapEntry<CharSequence> entry = ourArrayCache[l1];
    if (entry != null && entry.key == nameId) {
      return entry.value;
    }

    if (ourTrackStats) {
      ourMisses.incrementAndGet();
    }

    final int stripe = calcStripeIdFromNameId(nameId);
    IntSLRUCache<IntObjectLinkedMap.MapEntry<CharSequence>> cache = ourNameCache[stripe];
    //noinspection SynchronizationOnLocalVariableOrMethodParameter
    synchronized (cache) {
      entry = cache.getCachedEntry(nameId);
    }
    if (entry == null) {
      entry = cacheData(computeName.compute(nameId), nameId, stripe);
    }
    ourArrayCache[l1] = entry;
    return entry.value;
  }

  @NotNull
  public CharSequence getVFileName(int nameId) {
    try {
      return getVFileName(nameId, FSRecords.getInstance()::getNameByNameId);
    }
    catch (IOException e) {
      throw new RuntimeException(e); // actually will be caught in getNameByNameId
    }
  }
}
